package io.github.lightman314.lightmanscurrency.common.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyHandler;
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
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import io.github.lightman314.lightmanscurrency.util.ItemHandlerUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.attachment.*;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class WalletHandler extends EasyAttachment<WalletHandler> implements IMoneyHandler, ICommonTicker
{

    public static final Codec<WalletHandler> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(builder -> builder.group(
                    ItemStack.OPTIONAL_CODEC.fieldOf("wallet").forGetter(WalletHandler::getWalletInternal),
                    Codec.BOOL.fieldOf("visible").forGetter(WalletHandler::visibleInternal)
            ).apply(builder,WalletHandler::new)),
            RecordCodecBuilder.create(builder -> builder.group(
                    ItemStack.OPTIONAL_CODEC.fieldOf("Wallet").forGetter(WalletHandler::getWalletInternal),
                    Codec.BOOL.fieldOf("Visible").forGetter(WalletHandler::visibleInternal)
            ).apply(builder,WalletHandler::new)));
    public static final StreamCodec<RegistryFriendlyByteBuf,WalletHandler> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC,WalletHandler::getWalletInternal,
            ByteBufCodecs.BOOL,WalletHandler::visibleInternal,
            WalletHandler::new);
    public static final UnaryOperator<WalletHandler> COPIER = data -> {
        WalletHandler h = new WalletHandler();
        h.walletItem = data.walletItem.copy();
        h.visible = data.visible;
        return h;
    };

    public static WalletHandler get(LivingEntity entity) { return entity.getData(ModAttachmentTypes.WALLET_HANDLER); }

    private LivingEntity entity;

    @Override
    protected void afterHolderAssigned() {
        if(this.getHolder() instanceof LivingEntity e)
            this.entity = e;
        else
            throw new IllegalStateException("Wallet Handlers can only be attached to living entities!");
    }

    @Override
    protected AttachmentType<WalletHandler> getType() { return ModAttachmentTypes.WALLET_HANDLER.get(); }

    //Wallet
    ItemStack walletItem;
    ItemStack backupWallet;

    //Visibility
    boolean visible;
    boolean wasVisible;

    public WalletHandler() {
        this.backupWallet = ItemStack.EMPTY;
        this.walletItem = ItemStack.EMPTY;
        this.visible = this.wasVisible = true;
    }
    private WalletHandler(ItemStack wallet,boolean visible)
    {
        this.backupWallet = wallet.copy();
        this.walletItem = wallet;
        this.visible = this.wasVisible = visible;
    }

    private Consumer<ItemStack> overflowHandler(boolean simulation)
    {
        if(simulation)
            return i -> {};
        return overflow -> {
            IAttachmentHolder holder = this.getHolder();
            if(holder instanceof Player player)
                ItemHandlerHelper.giveItemToPlayer(player, overflow);
            else if(holder instanceof Entity e)
            {
                IItemHandler handler = e.getCapability(Capabilities.ItemHandler.ENTITY,null);
                if(handler != null)
                    overflow = ItemHandlerHelper.insertItem(handler,overflow,false);
                if(!overflow.isEmpty())
                    ItemHandlerUtil.dropContents(e.level(), e.blockPosition(),overflow);
            }
            else if(holder instanceof BlockEntity be)
            {
                IItemHandler handler = be.getLevel().getCapability(Capabilities.ItemHandler.BLOCK,be.getBlockPos(),null);
                if(handler != null)
                    overflow = ItemHandlerHelper.insertItem(handler,overflow,false);
                if(!overflow.isEmpty())
                    ItemHandlerUtil.dropContents(be.getLevel(),be.getBlockPos(),overflow);
            }
        };
    }

    public ItemStack getWallet() {
        if(LCCurios.isLoaded())
            return LCCurios.getCuriosWalletItem(this.entity);
        return this.walletItem;
    }

    private ItemStack getWalletInternal() { return this.walletItem; }

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
    }

    public boolean visible() {
        if(LCCurios.hasWalletSlot(this.entity))
            return LCCurios.getCuriosWalletVisiblity(this.entity);
        return this.visible;
    }

    private boolean visibleInternal() { return this.visible; }

    public void setVisible(boolean visible) { this.visible = visible; }

    public LivingEntity entity() { return this.entity; }

    @Override
    public void tick() {
        if(!this.walletItem.isEmpty() && LCCurios.hasWalletSlot(this.entity))
        {
            LightmansCurrency.LogInfo("Curios detected. Moving wallet from Lightman's Currency wallet slot into the curios wallet slot.");
            LCCurios.setCuriosWalletItem(this.entity, this.walletItem);
            this.walletItem = ItemStack.EMPTY;
        }
        if(!ItemStack.isSameItemSameComponents(this.backupWallet,this.walletItem) || this.visible != this.wasVisible)
        {
            this.wasVisible = this.visible;
            this.backupWallet = this.walletItem.copy();
            //Tell the entity to sync the wallet data
            this.entity.syncData(ModAttachmentTypes.WALLET_HANDLER);
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
    }

    @Override
    public boolean isMoneyTypeValid(MoneyValue value) {
        if(WalletItem.isWallet(this.getWallet()))
            return value instanceof CoinValue;
        return false;
    }

    @Override
    public MoneyView getStoredMoney() {
        if(!WalletItem.isWallet(this.walletItem))
            return MoneyView.empty();
        WalletInventory contents = WalletItem.getWalletInventory(this.walletItem);
        IMoneyViewer viewer = MoneyViewWrapper.forInventory(contents,this);
        return viewer.getStoredMoney();
    }
    
    public ItemStack PickupCoins(ItemStack stack) { return WalletItem.PickupCoin(this.getWallet(),stack); }

}