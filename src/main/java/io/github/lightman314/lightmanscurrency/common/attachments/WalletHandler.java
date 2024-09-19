package io.github.lightman314.lightmanscurrency.common.attachments;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.capability.money.MoneyHandler;
import io.github.lightman314.lightmanscurrency.api.misc.IEasyTickable;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.CoinCurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.coins.CoinContainerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.common.core.ModAttachmentTypes;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.items.data.WalletDataWrapper;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import io.github.lightman314.lightmanscurrency.network.message.walletslot.SPacketSyncWallet;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class WalletHandler extends MoneyHandler implements INBTSerializable<CompoundTag>, IClientTracker, IEasyTickable
{

    @Nonnull
    public static WalletHandler get(@Nonnull LivingEntity entity) { return entity.getData(ModAttachmentTypes.WALLET_HANDLER); }

    public static WalletHandler create(@Nonnull IAttachmentHolder holder) {
        if(holder instanceof LivingEntity entity)
            return new WalletHandler(entity);
        else
            return null;
    }

    final LivingEntity entity;
    //Wallet
    ItemStack walletItem;
    ItemStack backupWallet;
    WalletDataWrapper walletDataWrapper;

    //Visibility
    boolean visible;
    boolean wasVisible;
    //Money Holder
    ItemStack moneyCacheWallet;

    @Override
    public boolean isClient() { return this.entity.level().isClientSide; }

    private WalletHandler(@Nonnull LivingEntity entity) {
        this.entity = entity;
        this.backupWallet = ItemStack.EMPTY;
        this.walletItem = ItemStack.EMPTY;
        this.moneyCacheWallet = ItemStack.EMPTY;
        this.visible = true;
        this.wasVisible = true;
    }

    private void setChanged() {
        this.entity.setData(ModAttachmentTypes.WALLET_HANDLER.get(), this);
        if(this.isServer())
        {
            //Send update packet when changed
            new SPacketSyncWallet(this.entity.getId(), this.walletItem.copy(), this.visible).sendToPlayersTrackingEntityAndSelf(this.entity);
        }
    }

    private void handleOverflow(@Nonnull ItemStack overflow)
    {
        if(this.entity instanceof Player player)
            ItemHandlerHelper.giveItemToPlayer(player, overflow);
        else if(this.entity != null)
        {
            IItemHandler handler = this.entity.getCapability(Capabilities.ItemHandler.ENTITY,null);
            if(handler != null)
            {
                for(int i = 0; i < handler.getSlots() && !overflow.isEmpty(); ++i)
                    overflow = handler.insertItem(i,overflow, false);
            }
            if(!overflow.isEmpty())
                InventoryUtil.dumpContents(this.entity.level(), this.entity.blockPosition(), overflow);
        }
    }

    @Nonnull
    private WalletDataWrapper getWalletWrapper()
    {
        if(this.walletDataWrapper == null || !this.walletDataWrapper.isForStack(this.getWallet()))
            this.walletDataWrapper = WalletItem.getDataWrapper(this.getWallet());
        return this.walletDataWrapper;
    }

    public ItemStack getWallet() {
        if(LCCurios.isLoaded())
            return LCCurios.getCuriosWalletItem(this.entity);
        return this.walletItem;
    }

    public void setWallet(ItemStack walletStack) {
        if(LCCurios.hasWalletSlot(this.entity))
        {
            LCCurios.setCuriosWalletItem(this.entity,walletStack);
            return;
        }
        this.walletItem = walletStack;
        if(!(walletStack.getItem() instanceof WalletItem) && !walletStack.isEmpty())
            LightmansCurrency.LogWarning("Equipped a non-wallet to the players wallet slot.");

        this.setChanged();

    }

    public void syncWallet(ItemStack walletStack) { this.walletItem = walletStack; this.setChanged(); }

    public boolean visible() {
        if(LCCurios.hasWalletSlot(this.entity))
            return LCCurios.getCuriosWalletVisiblity(this.entity);
        return this.visible;
    }

    public void setVisible(boolean visible) { this.visible = visible; this.setChanged(); }

    public LivingEntity entity() { return this.entity; }

    @Override
    public CompoundTag serializeNBT(@Nonnull HolderLookup.Provider lookup) {
        CompoundTag compound = new CompoundTag();
        compound.put("Wallet", InventoryUtil.saveItemNoLimits(this.walletItem,lookup));
        compound.putBoolean("Visible", this.visible);
        return compound;
    }

    @Override
    public void deserializeNBT(@Nonnull HolderLookup.Provider lookup, CompoundTag tag) {
        this.walletItem = InventoryUtil.loadItemNoLimits(tag.getCompound("Wallet"),lookup);
        if(tag.contains("Visible"))
            this.visible = tag.getBoolean("Visible");
    }

    @Override
    public void tick() {
        if(!this.walletItem.isEmpty() && LCCurios.hasWalletSlot(this.entity))
        {
            LightmansCurrency.LogInfo("Curios detected. Moving wallet from Lightman's Currency wallet slot into the curios wallet slot.");
            LCCurios.setCuriosWalletItem(this.entity, this.walletItem);
            this.walletItem = ItemStack.EMPTY;
            this.setChanged();
        }
    }

    @Nonnull
    @Override
    public MoneyValue insertMoney(@Nonnull MoneyValue insertAmount, boolean simulation) {
        WalletDataWrapper wrapper = this.getWalletWrapper();
        Container contents = wrapper.getContents();
        IMoneyHandler handler = CoinCurrencyType.INSTANCE.createMoneyHandlerForContainer(contents,this::handleOverflow,this);
        MoneyValue result = handler.insertMoney(insertAmount, simulation);
        //If changed, update wallet menus
        if(!InventoryUtil.ContainerMatches(contents,wrapper.getContents()))
            this.updateWalletContents(wrapper, contents);
        return result;
    }

    @Nonnull
    @Override
    public MoneyValue extractMoney(@Nonnull MoneyValue extractAmount, boolean simulation) {
        WalletDataWrapper wrapper = this.getWalletWrapper();
        Container contents = wrapper.getContents();
        IMoneyHandler handler = CoinCurrencyType.INSTANCE.createMoneyHandlerForContainer(contents,this::handleOverflow,this);
        MoneyValue result = handler.extractMoney(extractAmount,simulation);
        if(!InventoryUtil.ContainerMatches(contents,wrapper.getContents()))
            this.updateWalletContents(wrapper,contents);
        return result;
    }

    private void updateWalletContents(@Nonnull WalletDataWrapper wrapper, @Nonnull Container contents)
    {
        ItemStack w = this.getWallet();
        if(WalletItem.getAutoExchange(w))
        {
            CoinAPI.API.CoinExchangeAllUp(contents);
            CoinAPI.API.SortCoinsByValue(contents);
        }
        wrapper.setContents(contents, this.entity);
    }

    @Override
    public boolean isMoneyTypeValid(@Nonnull MoneyValue value) {
        if(WalletItem.isWallet(this.getWallet()))
            return value instanceof CoinValue;
        return false;
    }

    @Override
    protected void collectStoredMoney(@Nonnull MoneyView.Builder builder) {
        this.moneyCacheWallet = this.getWallet().copy();
        if(WalletItem.isWallet(this.moneyCacheWallet))
            CoinContainerMoneyHandler.queryContainerContents(WalletItem.getDataWrapper(this.moneyCacheWallet).getContents(),builder);
    }

    @Nonnull
    public ItemStack PickupCoins(@Nonnull ItemStack stack)
    {
        ItemStack result = WalletItem.PickupCoin(this.walletItem,stack);
        if(!InventoryUtil.ItemsFullyMatch(stack,result))
            this.setChanged();
        return result;
    }

}