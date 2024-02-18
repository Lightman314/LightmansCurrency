package io.github.lightman314.lightmanscurrency.common.capability.wallet;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.capability.money.MoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.CoinCurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.coins.CoinContainerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenu;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

public class WalletHandler extends MoneyHandler implements IWalletHandler
{

    final LivingEntity entity;
    //Wallet
    ItemStack walletItem;
    ItemStack backupWallet;

    //Visibility
    boolean visible;
    boolean wasVisible;
    //Money Holder
    ItemStack moneyCacheWallet;

    public WalletHandler(LivingEntity entity) {
        this.entity = entity;
        this.backupWallet = ItemStack.EMPTY;
        this.walletItem = ItemStack.EMPTY;
        this.moneyCacheWallet = ItemStack.EMPTY;
        this.visible = true;
        this.wasVisible = true;
    }

    private void handleOverflow(@Nonnull ItemStack overflow)
    {
        if(this.entity instanceof Player player)
            ItemHandlerHelper.giveItemToPlayer(player, overflow);
        else
        {
            LazyOptional<IItemHandler> optional = this.entity.getCapability(ForgeCapabilities.ITEM_HANDLER);
            AtomicReference<ItemStack> reference = new AtomicReference<>(overflow);
            optional.ifPresent((handler) ->{
                for(int i = 0; i < handler.getSlots() && !reference.get().isEmpty(); ++i)
                    reference.set(handler.insertItem(i,reference.get(), false));
            });
            if(!reference.get().isEmpty())
                InventoryUtil.dumpContents(this.entity.level(), this.entity.blockPosition(), reference.get());
        }
    }

    @Nullable
    private Container getWalletContainer()
    {
        if(WalletItem.isWallet(this.walletItem))
            return WalletItem.getWalletInventory(this.walletItem);
        return null;
    }

    @Override
    public ItemStack getWallet() {

        //Curios hook for consistent access
        if(LightmansCurrency.isCuriosValid(this.entity))
            return LCCurios.getCuriosWalletContents(this.entity);

        return this.walletItem;
    }

    @Override
    public void setWallet(ItemStack walletStack) {

        if(LightmansCurrency.isCuriosValid(this.entity))
        {
            LCCurios.setCuriosWalletContents(this.entity, walletStack);
            return;
        }

        this.walletItem = walletStack;
        if(!(walletStack.getItem() instanceof WalletItem) && !walletStack.isEmpty())
            LightmansCurrency.LogWarning("Equipped a non-wallet to the players wallet slot.");

    }

    @Override
    public void syncWallet(ItemStack walletStack) { this.walletItem = walletStack; }

    @Override
    public boolean visible() {
        if(LightmansCurrency.isCuriosValid(this.entity))
            return LCCurios.getCuriosWalletVisibility(this.entity);
        return this.visible;
    }

    @Override
    public void setVisible(boolean visible) { this.visible = visible; }

    @Override
    public LivingEntity entity() { return this.entity; }

    @Override
    public boolean isDirty() {
        return !InventoryUtil.ItemsFullyMatch(this.backupWallet, this.getWallet()) || this.wasVisible != this.visible;
    }

    @Override
    public void clean() {
        this.backupWallet = this.walletItem.copy();
        this.wasVisible = this.visible;
    }

    @Override
    public CompoundTag save() {
        CompoundTag compound = new CompoundTag();
        CompoundTag walletItem = this.walletItem.save(new CompoundTag());
        compound.put("Wallet", walletItem);
        compound.putBoolean("Visible", this.visible);
        return compound;
    }

    @Override
    public void load(CompoundTag compound)
    {
        this.walletItem = ItemStack.of(compound.getCompound("Wallet"));
        if(compound.contains("Visible"))
            this.visible = compound.getBoolean("Visible");

        this.clean();
    }

    @Override
    public void tick() {
        if(LightmansCurrency.isCuriosValid(this.entity) && !this.walletItem.isEmpty())
        {
            LightmansCurrency.LogInfo("Curios detected. Moving wallet from Lightman's Currency wallet slot into the curios wallet slot.");
            LCCurios.setCuriosWalletContents(this.entity, this.walletItem);
            this.walletItem = ItemStack.EMPTY;
        }
    }

    @Nonnull
    @Override
    public MoneyValue insertMoney(@Nonnull MoneyValue insertAmount, boolean simulation) {
        Container container = this.getWalletContainer();
        if(container != null)
        {
            Container cache = InventoryUtil.copyInventory(container);
            IMoneyHandler handler = CoinCurrencyType.INSTANCE.createMoneyHandlerForContainer(container,this::handleOverflow);
            MoneyValue result = handler.insertMoney(insertAmount, simulation);
            //If changed, update wallet menus
            if(!InventoryUtil.ContainerMatches(container,cache))
                this.updateWalletContents(container);
            return result;
        }
        else if(insertAmount instanceof CoinValue coinValue)
        {
            //Manually give to the player/entity
            if(!simulation)
            {
                for(ItemStack stack : coinValue.getAsSeperatedItemList())
                    this.handleOverflow(stack);
            }
            return MoneyValue.empty();
        }
        return insertAmount;
    }

    @Nonnull
    @Override
    public MoneyValue extractMoney(@Nonnull MoneyValue extractAmount, boolean simulation) {
        Container container = this.getWalletContainer();
        if (container != null)
        {
            Container cache = InventoryUtil.copyInventory(container);
            IMoneyHandler handler = CoinCurrencyType.INSTANCE.createMoneyHandlerForContainer(container, this::handleOverflow);
            MoneyValue result = handler.extractMoney(extractAmount,simulation);
            if(!InventoryUtil.ContainerMatches(container,cache))
                this.updateWalletContents(container);
            return result;
        }
        return extractAmount;
    }

    private void updateWalletContents(@Nonnull Container updatedContainer)
    {
        if(WalletItem.getAutoExchange(this.walletItem))
        {
            CoinAPI.API.CoinExchangeAllUp(updatedContainer);
            CoinAPI.API.SortCoinsByValue(updatedContainer);
        }
        WalletItem.putWalletInventory(this.walletItem, updatedContainer);
        WalletMenu.OnWalletUpdated(this.entity);
    }

    @Override
    public boolean isMoneyTypeValid(@Nonnull MoneyValue value) {
        if(WalletItem.isWallet(this.walletItem))
            return value instanceof CoinValue;
        return false;
    }

    @Override
    protected void collectStoredMoney(@Nonnull MoneyView.Builder builder) {
        this.moneyCacheWallet = this.getWallet().copy();
        if(WalletItem.isWallet(this.moneyCacheWallet))
            CoinContainerMoneyHandler.queryContainerContents(WalletItem.getWalletInventory(this.moneyCacheWallet),builder);
    }

    @Override
    protected boolean hasStoredMoneyChanged() { return !InventoryUtil.ItemsFullyMatch(this.moneyCacheWallet, this.getWallet()); }

}