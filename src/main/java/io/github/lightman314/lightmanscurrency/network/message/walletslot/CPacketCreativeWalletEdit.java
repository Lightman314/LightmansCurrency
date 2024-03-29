package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.WalletCapability;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CPacketCreativeWalletEdit extends ClientToServerPacket {

    public static final Handler<CPacketCreativeWalletEdit> HANDLER = new H();

    private final ItemStack newWallet;
    public CPacketCreativeWalletEdit(@Nonnull ItemStack wallet) { this.newWallet = wallet; }

    @Override
    public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeItemStack(this.newWallet, false); }

    private static class H extends Handler<CPacketCreativeWalletEdit>
    {
        @Nonnull
        @Override
        public CPacketCreativeWalletEdit decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketCreativeWalletEdit(buffer.readItem()); }

        @Override
        protected void handle(@Nonnull CPacketCreativeWalletEdit message, @Nullable ServerPlayer sender) {
            if(sender != null)
            {
                IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(sender);
                LightmansCurrency.LogDebug("Updated wallet stack on server from client-side interaction.");
                walletHandler.setWallet(message.newWallet);
            }
        }
    }

}