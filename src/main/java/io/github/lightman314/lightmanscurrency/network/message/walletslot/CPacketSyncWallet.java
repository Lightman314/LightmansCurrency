package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import io.github.lightman314.lightmanscurrency.common.capability.wallet.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.WalletCapability;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class CPacketSyncWallet extends ClientToServerPacket {

    public static final Handler<CPacketSyncWallet> HANDLER = new H();

    private final UUID playerID;
    private final ItemStack wallet;
    public CPacketSyncWallet(@Nonnull UUID playerID, @Nonnull ItemStack wallet) { this.playerID = playerID; this.wallet = wallet; }

    @Override
    public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeUUID(this.playerID); buffer.writeItem(this.wallet); }

    private static class H extends Handler<CPacketSyncWallet>
    {
        @Nonnull
        @Override
        public CPacketSyncWallet decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketSyncWallet(buffer.readUUID(),buffer.readItem()); }
        @Override
        protected void handle(@Nonnull CPacketSyncWallet message, @Nullable ServerPlayer sender) {
            IWalletHandler handler = WalletCapability.lazyGetWalletHandler(sender);
            if(handler != null && sender.isCreative() && sender.getUUID().equals(message.playerID))
                handler.setWallet(message.wallet);
        }
    }

}
