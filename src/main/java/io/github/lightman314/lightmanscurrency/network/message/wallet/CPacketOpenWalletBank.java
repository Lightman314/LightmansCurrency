package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CPacketOpenWalletBank extends ClientToServerPacket {

	public static final Handler<CPacketOpenWalletBank> HANDLER = new H();

	private final int walletStackIndex;
	
	public CPacketOpenWalletBank(int walletStackIndex) { this.walletStackIndex = walletStackIndex;  }
	
	public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeInt(this.walletStackIndex); }

	private static class H extends Handler<CPacketOpenWalletBank>
	{
		@Nonnull
		@Override
		public CPacketOpenWalletBank decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketOpenWalletBank(buffer.readInt()); }
		@Override
		protected void handle(@Nonnull CPacketOpenWalletBank message, @Nullable ServerPlayer sender) {
			if(sender != null)
				WalletMenuBase.SafeOpenWalletBankMenu(sender, message.walletStackIndex);
		}
	}

}
