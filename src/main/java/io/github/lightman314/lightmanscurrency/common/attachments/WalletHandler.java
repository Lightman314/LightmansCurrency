package io.github.lightman314.lightmanscurrency.common.attachments;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.capability.MoneyHandler;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.misc.ticker.ICommonTicker;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyViewer;
import io.github.lightman314.lightmanscurrency.api.money.capability.implementations.MoneyViewWrapper;
import io.github.lightman314.lightmanscurrency.common.core.ModAttachmentTypes;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.items.data.WalletInventory;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import io.github.lightman314.lightmanscurrency.util.ItemHandlerUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.*;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public class WalletHandler extends MoneyHandler implements IClientTracker, ICommonTicker
{

    public static WalletHandler get(LivingEntity entity) { return entity.getData(ModAttachmentTypes.WALLET_HANDLER); }

    private static WalletHandler create(IAttachmentHolder holder) {
        if(holder instanceof LivingEntity entity)
            return new WalletHandler(entity);
        else
            throw new IllegalStateException("Cannot create a WalletHandler for a " + holder.getClass().getName() + "!");
    }

    final LivingEntity entity;
    //Wallet
    ItemStack walletItem;
    ItemStack backupWallet;

    //Visibility
    boolean visible;

    @Override
    public boolean isClient() { return this.entity.level().isClientSide; }

    private WalletHandler(LivingEntity entity) {
        this.entity = entity;
        this.backupWallet = ItemStack.EMPTY;
        this.walletItem = ItemStack.EMPTY;
        this.visible = true;
    }

    private void setChanged() {
        this.backupWallet = this.walletItem.copy();
        this.entity.setData(ModAttachmentTypes.WALLET_HANDLER.get(), this);
    }

    private Consumer<ItemStack> overflowHandler(boolean simulation)
    {
        if(simulation)
            return i -> {};
        return overflow -> {
            if(this.entity instanceof Player player)
                ItemHandlerHelper.giveItemToPlayer(player, overflow);
            else if(this.entity != null)
            {
                IItemHandler handler = this.entity.getCapability(Capabilities.ItemHandler.ENTITY,null);
                if(handler != null)
                    overflow = ItemHandlerHelper.insertItem(handler,overflow,false);
                if(!overflow.isEmpty())
                    ItemHandlerUtil.dropContents(this.entity.level(), this.entity.blockPosition(),overflow);
            }
        };
    }

    public ItemStack getWallet() {
        if(LCCurios.isLoaded())
            return LCCurios.getCuriosWalletItem(this.entity);
        return this.walletItem;
    }

    public ItemStack getVisibleWallet() {
        if(LCCurios.isLoaded())
            return LCCurios.getVisibleCuriosWalletItem(this.entity);
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

    protected CompoundTag write(DataContext<Tag> context)
    {
        CompoundTag tag = new CompoundTag();
        tag.put("wallet",context.write(this.walletItem,ItemStack.OPTIONAL_CODEC));
        tag.putBoolean("visible",this.visible);
        return tag;
    }

    private void read(CompoundTag tag,DataContext<Tag> context)
    {
        if(tag.contains("Wallet"))
            this.walletItem = context.readOrDefault(tag.get("Wallet"),ItemStack.OPTIONAL_CODEC,ItemStack.EMPTY);
        else
            this.walletItem = context.readOrDefault(tag.get("wallet"),ItemStack.OPTIONAL_CODEC,ItemStack.EMPTY);
        if(tag.contains("Visible"))
            this.visible = tag.getBoolean("Visible");
        else
            this.visible = tag.getBoolean("visible");
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

    
    @Override
    public MoneyValue insertMoney(MoneyValue insertAmount, boolean simulation) {
        if(!WalletItem.isWallet(this.walletItem))
            return insertAmount;
        WalletInventory contents = WalletItem.getWalletInventory(this.walletItem);
        WalletInventory copy = contents.copy();
        IMoneyHandler handler = MoneyAPI.getApi().GetContainersMoneyHandler(contents,this.overflowHandler(simulation),this);
        MoneyValue result = handler.insertMoney(insertAmount, simulation);
        //If changed, update wallet menus
        if(!simulation && !ItemHandlerUtil.equals(contents,copy))
            this.updateWalletContents(contents);
        return result;
    }

    
    @Override
    public MoneyValue extractMoney(MoneyValue extractAmount, boolean simulation) {
        if(!WalletItem.isWallet(this.walletItem))
            return extractAmount;
        WalletInventory contents = WalletItem.getWalletInventory(this.walletItem);
        WalletInventory copy = contents.copy();
        IMoneyHandler handler = MoneyAPI.getApi().GetContainersMoneyHandler(contents,this.overflowHandler(simulation),this);
        MoneyValue result = handler.extractMoney(extractAmount,simulation);
        if(!simulation && !ItemHandlerUtil.equals(contents,copy))
            this.updateWalletContents(contents);
        return result;
    }

    private void updateWalletContents(WalletInventory contents)
    {
        ItemStack w = this.getWallet();
        if(WalletItem.getAutoExchange(w))
        {
            CoinAPI.getApi().CoinExchangeAllUp(contents);
            CoinAPI.getApi().SortCoinsByValue(contents);
        }
        WalletItem.putWalletInventory(this.walletItem,contents);
        this.setChanged();
    }

    @Override
    public boolean isMoneyTypeValid(MoneyValue value) {
        if(WalletItem.isWallet(this.getWallet()))
            return value instanceof CoinValue;
        return false;
    }

    @Override
    protected void collectStoredMoney(MoneyView.Builder builder) {
        if(!WalletItem.isWallet(this.walletItem))
            return;
        WalletInventory contents = WalletItem.getWalletInventory(this.walletItem);
        IMoneyViewer viewer = MoneyViewWrapper.forInventory(contents,this);
        builder.merge(viewer.getStoredMoney());
    }

    
    public ItemStack PickupCoins(ItemStack stack)
    {
        if(LCCurios.isLoaded()) //Don't need to check for data change if the wallet is in a curios slot
            return WalletItem.PickupCoin(this.getWallet(),stack);
        ItemStack result = WalletItem.PickupCoin(this.walletItem,stack);
        if(!ItemStack.isSameItemSameComponents(stack,result) || stack.getCount() != result.getCount())
            this.setChanged();
        return result;
    }

    public static AttachmentType.Builder<WalletHandler> buildType() {
        return AttachmentType.builder(WalletHandler::create)
                .serialize(new Serializer())
                .sync(new Syncer())
                .copyHandler(new CopyHandler())
                .copyOnDeath();
    }

    private static class Serializer implements IAttachmentSerializer<CompoundTag,WalletHandler>
    {
        @Override
        public WalletHandler read(IAttachmentHolder holder,CompoundTag tag, HolderLookup.Provider lookup) {
            WalletHandler handler = WalletHandler.create(holder);
            handler.read(tag,DataContext.createNBT(lookup));
            return handler;
        }
        @Override
        @Nullable
        public CompoundTag write(WalletHandler handler,HolderLookup.Provider lookup) { return handler.write(DataContext.createNBT(lookup)); }
    }

    private static class Syncer implements AttachmentSyncHandler<WalletHandler>
    {
        @Override
        public void write(RegistryFriendlyByteBuf buf, WalletHandler handler, boolean initialSync) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf,handler.walletItem);
            buf.writeBoolean(handler.visible);
        }

        @Override
        public @Nullable WalletHandler read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable WalletHandler previousValue) {
            WalletHandler handler = Objects.requireNonNullElseGet(previousValue,() -> create(holder));
            handler.walletItem = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            handler.visible = buf.readBoolean();
            return handler;
        }
    }

    private static class CopyHandler implements IAttachmentCopyHandler<WalletHandler>
    {
        @Override
        public @Nullable WalletHandler copy(WalletHandler oldHandler, IAttachmentHolder holder, HolderLookup.Provider lookup) {
            WalletHandler newHandler = create(holder);
            newHandler.walletItem = oldHandler.walletItem.copy();
            newHandler.visible = oldHandler.visible;
            return newHandler;
        }
    }

}