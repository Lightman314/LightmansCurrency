package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketOpenWalletBank extends ClientToServerPacket {

	public static final Handler<CPacketOpenWalletBank> HANDLER = new H();

	private final int walletStackIndex;
	
	public CPacketOpenWalletBank(int walletStackIndex) { this.walletStackIndex = walletStackIndex;  }
	
	public void encode(FriendlyByteBuf buffer) { buffer.writeInt(this.walletStackIndex); }

	private static class H extends Handler<CPacketOpenWalletBank>
	{
		@Override
		public CPacketOpenWalletBank decode(FriendlyByteBuf buffer) { return new CPacketOpenWalletBank(buffer.readInt()); }
		@Override
		protected void handle(CPacketOpenWalletBank message, Player player) {
            WalletMenuBase.SafeOpenWalletBankMenu(player, message.walletStackIndex);
		}
	}

}
