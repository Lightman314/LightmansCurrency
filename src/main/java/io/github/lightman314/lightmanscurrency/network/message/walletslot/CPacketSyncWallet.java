package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import io.github.lightman314.lightmanscurrency.common.capability.wallet.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.WalletCapability;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CPacketSyncWallet extends ClientToServerPacket {

    public static final Handler<CPacketSyncWallet> HANDLER = new H();

    private final ItemStack wallet;
    public CPacketSyncWallet(@Nonnull ItemStack wallet) { this.wallet = wallet; }

    @Override
    public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeItem(this.wallet); }

    private static class H extends Handler<CPacketSyncWallet>
    {
        @Nonnull
        @Override
        public CPacketSyncWallet decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketSyncWallet(buffer.readItem()); }
        @Override
        protected void handle(@Nonnull CPacketSyncWallet message, @Nullable ServerPlayer sender) {
            IWalletHandler handler = WalletCapability.lazyGetWalletHandler(sender);
            if(handler != null && sender.isCreative())
                handler.setWallet(message.wallet);
        }
    }

}
