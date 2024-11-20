package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CPacketOpenWallet extends ClientToServerPacket {

	public static final Handler<CPacketOpenWallet> HANDLER = new H();

	private final int walletStackIndex;
	
	public CPacketOpenWallet(int walletStackIndex) { this.walletStackIndex = walletStackIndex;  }

	public static void sendEquippedPacket() { new CPacketOpenWallet(-1).send(); }

	public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeInt(this.walletStackIndex); }

	private static class H extends Handler<CPacketOpenWallet>
	{
		@Nonnull
		@Override
		public CPacketOpenWallet decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketOpenWallet(buffer.readInt()); }
		@Override
		protected void handle(@Nonnull CPacketOpenWallet message, @Nullable ServerPlayer sender) {
			if(sender != null)
				WalletMenuBase.SafeOpenWalletMenu(sender, message.walletStackIndex);
		}
	}

}
