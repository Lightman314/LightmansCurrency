package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketSetVisible extends ClientToServerPacket {

	private static final Type<CPacketSetVisible> TYPE = cType("wallet_set_visible");
    private static final StreamCodec<ByteBuf,CPacketSetVisible> STREAM_CODEC = ByteBufCodecs.BOOL
            .map(CPacketSetVisible::new,p -> p.visible);
	public static final Handler<CPacketSetVisible> HANDLER = new H();

	boolean visible;
	
	public CPacketSetVisible(boolean visible) { super(TYPE); this.visible = visible; }

	private static class H extends Handler<CPacketSetVisible>
	{
		protected H() { super(TYPE,STREAM_CODEC); }
		@Override
		protected void handle(CPacketSetVisible message, IPayloadContext context, Player player) {
			WalletHandler walletHandler = WalletHandler.get(player);
			if(walletHandler != null)
				walletHandler.setVisible(message.visible);
		}
	}

}
