package io.github.lightman314.lightmanscurrency.features.wallet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.data.attachment.AttachmentWithContext;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.builtin.EmptyMoneyResource;
import io.github.lightman314.lightmanscurrency.core.neoforge.LCDataAttachments;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCuriosHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.function.BiConsumer;

@EventBusSubscriber
public final class WalletAttachment extends SnapshotJournal<ItemStack> implements ItemAccess, AttachmentWithContext {

    public static final MapCodec<WalletAttachment> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ItemStack.OPTIONAL_CODEC.fieldOf("wallet").forGetter(WalletAttachment::getWallet),
            Codec.BOOL.fieldOf("visible").forGetter(WalletAttachment::isVisible)
    ).apply(builder,WalletAttachment::new));
    public static final StreamCodec<RegistryFriendlyByteBuf,WalletAttachment> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC,WalletAttachment::getWallet,
            ByteBufCodecs.BOOL,WalletAttachment::isVisible,
            WalletAttachment::new);

    private boolean firstTick = true;

    private IAttachmentHolder holder;
    private LivingEntity entity;
    @Override
    public void attachHolder(IAttachmentHolder holder) {
        this.holder = holder;
        if(this.holder instanceof LivingEntity e)
            this.entity = e;
    }
    @Override
    public IAttachmentHolder getHolder() { return this.holder; }

    private boolean useCuriosData() { return this.entity != null && LCCuriosHelper.get().hasWalletSlot(this.entity); }

    private ItemStack wallet = ItemStack.EMPTY;
    private ItemStack oldWallet = ItemStack.EMPTY;
    public void setWallet(ItemStack stack) {
        if(this.useCuriosData())
            LCCuriosHelper.get().setWallet(this.entity,stack.copyWithCount(1));
        else
            this.wallet = stack.copyWithCount(1);
    }
    public ItemStack getWallet() {
        if(this.useCuriosData())
            return LCCuriosHelper.get().getWallet(this.entity);
        return this.wallet;
    }
    public ItemStack getVisibleWallet() {
        if(this.useCuriosData())
            return LCCuriosHelper.get().getVisibleWallet(this.entity);
        return this.wallet;
    }

    private boolean visible = true;
    private boolean wasVisible = this.visible;
    public boolean isVisible() {
        if(this.useCuriosData())
            return LCCuriosHelper.get().getWalletVisibility(this.entity);
        return this.visible;
    }
    public void setVisible(boolean visible) { this.visible = visible; }

    public WalletAttachment() {}

    private WalletAttachment(ItemStack wallet,boolean visible) {
        this.wallet = wallet.copyWithCount(1);
        this.oldWallet = this.wallet.copy();
        this.visible = this.wasVisible = visible;
    }

    public boolean wasChanged() { return !ItemStack.isSameItemSameComponents(this.wallet,this.oldWallet) || this.visible != this.wasVisible; }
    public void clean() {
        this.oldWallet = this.wallet.copy();
        this.wasVisible = this.visible;
    }

    public void onLoad() {
        //If curios is loaded, attempt to move the wallet to curios
        if(this.useCuriosData() && !this.wallet.isEmpty() && LCCuriosHelper.get().getWallet(this.entity).isEmpty()) {
            LCCuriosHelper.get().setWallet(this.entity,this.wallet.copy());
            this.wallet = ItemStack.EMPTY;
        }
    }

    public MoneyResourceHandler getContentViewer(ISidedContext context) {
        if(WalletItem.isWallet(this.getWallet()))
        {
            WalletStorage storage = new WalletStorage(this);
            //This is a viewer, so no need for a proper listener
            return LCApi.getMoneyAPI().getContainersMoneyViewer(storage,context,false);
        }
        return EmptyMoneyResource.INSTANCE;
    }

    public MoneyResourceHandler getContentHandler(BiConsumer<ItemStack,TransactionContext> overflowHandler, ISidedContext context) {
        if(WalletItem.isWallet(this.wallet))
        {
            WalletStorage storage = new WalletStorage(this);
            return LCApi.getMoneyAPI().getContainersMoneyHandler(storage,overflowHandler,context,false);
        }
        return EmptyMoneyResource.INSTANCE;
    }
    public MoneyResourceHandler getContentHandler(Player player,ISidedContext context) {
        if(WalletItem.isWallet(this.wallet))
        {
            WalletStorage storage = new WalletStorage(this);
            return LCApi.getMoneyAPI().getContainersMoneyHandler(storage,player,false);
        }
        return EmptyMoneyResource.INSTANCE;
    }

    @SubscribeEvent
    private static void entityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if(!entity.level().isClientSide() && entity.hasData(LCDataAttachments.WALLET))
        {
            WalletAttachment attachment = entity.getData(LCDataAttachments.WALLET);
            if(attachment.firstTick) {
                attachment.onLoad();
                attachment.firstTick = false;
            }
            if(attachment.wasChanged())
            {
                //Automatically sync the data if it was changed this tick
                entity.syncData(LCDataAttachments.WALLET);
                attachment.clean();
            }
        }
    }

    @Override
    public ItemResource getResource() { return ItemResource.of(this.getWallet()); }
    @Override
    public int getAmount() { return this.getWallet().getCount(); }
    @Override
    public int insert(ItemResource resource,int amount,TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        if(this.getWallet().isEmpty() && WalletItem.isWallet(resource))
        {
            this.updateSnapshots(transaction);
            this.setWallet(resource.toStack());
            return 1;
        }
        return 0;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        ItemStack stack = this.getWallet().copy();
        if(!stack.isEmpty() && resource.matches(stack))
        {
            this.updateSnapshots(transaction);
            int taken = Math.min(amount,stack.getCount());
            stack.shrink(taken);
            this.setWallet(stack.isEmpty() ? ItemStack.EMPTY : stack);
            return taken;
        }
        return 0;
    }

    @Override
    protected ItemStack createSnapshot() { return this.getWallet().copy(); }
    @Override
    protected void revertToSnapshot(ItemStack snapshot) { this.setWallet(snapshot); }

}