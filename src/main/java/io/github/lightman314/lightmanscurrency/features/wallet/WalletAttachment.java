package io.github.lightman314.lightmanscurrency.features.wallet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.builtin.EmptyMoneyResource;
import io.github.lightman314.lightmanscurrency.core.neoforge.LCDataAttachments;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.function.BiConsumer;

@EventBusSubscriber
public final class WalletAttachment extends SnapshotJournal<ItemStack> implements ItemAccess {

    public static final MapCodec<WalletAttachment> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ItemStack.OPTIONAL_CODEC.fieldOf("wallet").forGetter(WalletAttachment::getWallet),
            Codec.BOOL.fieldOf("visible").forGetter(WalletAttachment::isVisible)
    ).apply(builder,WalletAttachment::new));
    public static final StreamCodec<RegistryFriendlyByteBuf,WalletAttachment> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC,WalletAttachment::getWallet,
            ByteBufCodecs.BOOL,WalletAttachment::isVisible,
            WalletAttachment::new);

    private ItemStack wallet = ItemStack.EMPTY;
    private ItemStack oldWallet = ItemStack.EMPTY;
    public void setWallet(ItemStack stack) { this.wallet = stack.copyWithCount(1); }
    public ItemStack getWallet() { return this.wallet; }

    private boolean visible = true;
    private boolean wasVisible = this.visible;
    public boolean isVisible() { return this.visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public WalletAttachment() {}
    private WalletAttachment(ItemStack wallet,boolean visible) {
        this.wallet = wallet.copyWithCount(1);
        this.oldWallet = this.wallet.copy();
        this.visible = this.wasVisible = visible;
    }

    public boolean wasChanged() {
        return !ItemStack.isSameItemSameComponents(this.wallet,this.oldWallet) || this.visible != this.wasVisible;
    }
    public void clean() {
        this.oldWallet = this.wallet.copy();
        this.wasVisible = this.visible;
    }

    @Override
    public ItemResource getResource() { return ItemResource.of(this.wallet); }
    @Override
    public int getAmount() { return this.wallet.getCount(); }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        if(this.wallet.isEmpty() && WalletItem.isWallet(resource))
        {
            this.updateSnapshots(transaction);
            //Define the wallet, and return 1 since we can only ever hold 1 item
            this.wallet = resource.toStack();
            return 1;
        }
        return 0;
    }

    @Override
    public int extract(ItemResource resource,int amount,TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        if(ItemStack.isSameItemSameComponents(this.wallet,resource.toStack()))
        {
            int taken = Math.min(this.wallet.getCount(),amount);
            this.updateSnapshots(transaction);
            this.wallet.shrink(taken);
            return taken;
        }
        return 0;
    }

    @Override
    protected ItemStack createSnapshot() { return this.wallet.copy(); }
    @Override
    protected void revertToSnapshot(ItemStack snapshot) { this.wallet = snapshot.copy(); }

    public MoneyResourceHandler getContentViewer(ISidedContext context) {
        if(WalletItem.isWallet(this.wallet))
        {
            WalletStorage storage = new WalletStorage(this);
            return LCApi.getMoneyAPI().getContainersMoneyViewer(storage,context);
        }
        return EmptyMoneyResource.INSTANCE;
    }

    public MoneyResourceHandler getContentHandler(BiConsumer<ItemStack,TransactionContext> overflowHandler, ISidedContext context) {
        if(WalletItem.isWallet(this.wallet))
        {
            WalletStorage storage = new WalletStorage(this);
            return LCApi.getMoneyAPI().getContainersMoneyHandler(storage,overflowHandler,context);
        }
        return EmptyMoneyResource.INSTANCE;
    }
    public MoneyResourceHandler getContentHandler(Player player,ISidedContext context) {
        if(WalletItem.isWallet(this.wallet))
        {
            WalletStorage storage = new WalletStorage(this);
            return LCApi.getMoneyAPI().getContainersMoneyHandler(storage,player);
        }
        return EmptyMoneyResource.INSTANCE;
    }

    @SubscribeEvent
    private static void entityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if(entity.hasData(LCDataAttachments.WALLET))
        {
            WalletAttachment attachment = entity.getData(LCDataAttachments.WALLET);
            if(attachment.wasChanged())
            {
                //Automatically sync the data if it was changed this tick
                entity.syncData(LCDataAttachments.WALLET);
                attachment.clean();
            }
        }
    }

}