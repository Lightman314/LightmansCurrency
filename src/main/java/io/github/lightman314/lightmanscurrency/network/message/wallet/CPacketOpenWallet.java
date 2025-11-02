package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketOpenWallet extends ClientToServerPacket {

	public static final Handler<CPacketOpenWallet> HANDLER = new H();

	private final int walletStackIndex;
	
	public CPacketOpenWallet(int walletStackIndex) { this.walletStackIndex = walletStackIndex;  }

	public static void sendEquippedPacket() { new CPacketOpenWallet(-1).send(); }

	public void encode(FriendlyByteBuf buffer) { buffer.writeInt(this.walletStackIndex); }

	private static class H extends Handler<CPacketOpenWallet>
	{
		@Override
		public CPacketOpenWallet decode(FriendlyByteBuf buffer) { return new CPacketOpenWallet(buffer.readInt()); }
		@Override
		protected void handle(CPacketOpenWallet message, Player player) {
            WalletMenuBase.SafeOpenWalletMenu(player, message.walletStackIndex);
		}
	}

}
