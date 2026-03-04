package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketOpenWallet extends ClientToServerPacket {

	private static final Type<CPacketOpenWallet> TYPE = cType("wallet_open");
    private static final CPacketOpenWallet INSTANCE = new CPacketOpenWallet();
	public static final Handler<CPacketOpenWallet> HANDLER = new H();
	
	private CPacketOpenWallet() { super(TYPE); }

	public static void send() { INSTANCE.sendToServer(); }

	private static class H extends SimpleHandler<CPacketOpenWallet>
	{
		protected H() { super(TYPE,INSTANCE); }
		@Override
		protected void handle(CPacketOpenWallet message, IPayloadContext context, Player player) {
			if(player instanceof ServerPlayer sp)
				WalletMenuBase.SafeOpenWalletMenu(sp,-1);
		}
	}

}
