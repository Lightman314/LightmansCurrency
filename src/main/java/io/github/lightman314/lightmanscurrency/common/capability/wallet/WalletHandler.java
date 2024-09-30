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

    @Override
    public boolean isClient() { return this.entity.level().isClientSide; }

    public WalletHandler(LivingEntity entity) {
        this.entity = entity;
        this.backupWallet = ItemStack.EMPTY;
        this.walletItem = ItemStack.EMPTY;
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
        ItemStack w = this.getWallet();
        if(WalletItem.isWallet(w))
            return WalletItem.getWalletInventory(w);
        return null;
    }

    @Override
    public ItemStack getWallet() {
        //Curios hook for consistent access
        if(LCCurios.isLoaded())
            return LCCurios.getCuriosWalletItem(this.entity);
        return this.walletItem;
    }

    @Override
    public void setWallet(ItemStack walletStack) {

        if(LCCurios.hasWalletSlot(this.entity))
        {
            LCCurios.setCuriosWalletItem(this.entity,walletStack);
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
        if(LCCurios.hasWalletSlot(this.entity))
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
        if(!this.walletItem.isEmpty() && LCCurios.hasWalletSlot(this.entity))
        {
            LightmansCurrency.LogInfo("Curios detected. Moving wallet from Lightman's Currency wallet slot into the curios wallet slot.");
            LCCurios.setCuriosWalletItem(this.entity, this.walletItem);
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
            IMoneyHandler handler = CoinCurrencyType.INSTANCE.createMoneyHandlerForContainer(container,this::handleOverflow,this);
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
            IMoneyHandler handler = CoinCurrencyType.INSTANCE.createMoneyHandlerForContainer(container, this::handleOverflow, this);
            MoneyValue result = handler.extractMoney(extractAmount,simulation);
            if(!InventoryUtil.ContainerMatches(container,cache))
                this.updateWalletContents(container);
            return result;
        }
        return extractAmount;
    }

    private void updateWalletContents(@Nonnull Container updatedContainer)
    {
        ItemStack w = this.getWallet();
        if(WalletItem.getAutoExchange(w))
        {
            CoinAPI.API.CoinExchangeAllUp(updatedContainer);
            CoinAPI.API.SortCoinsByValue(updatedContainer);
        }
        WalletItem.putWalletInventory(w, updatedContainer);
        WalletMenu.OnWalletUpdated(this.entity);
    }

    @Override
    public boolean isMoneyTypeValid(@Nonnull MoneyValue value) {
        if(WalletItem.isWallet(this.getWallet()))
            return value instanceof CoinValue;
        return false;
    }

    @Override
    protected void collectStoredMoney(@Nonnull MoneyView.Builder builder) {
        ItemStack wallet = this.getWallet();
        if(WalletItem.isWallet(wallet))
            CoinContainerMoneyHandler.queryContainerContents(WalletItem.getWalletInventory(wallet),builder);
    }

}