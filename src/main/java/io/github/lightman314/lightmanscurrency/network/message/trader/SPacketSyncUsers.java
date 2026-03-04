package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SPacketSyncUsers extends ServerToClientPacket {

	private static final Type<SPacketSyncUsers> TYPE = sType("trader_sync_users");
    private static final StreamCodec<ByteBuf,SPacketSyncUsers> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,p -> p.traderID,
            ByteBufCodecs.INT,p -> p.userCount,
            SPacketSyncUsers::new);
	public static final Handler<SPacketSyncUsers> HANDLER = new H();

	long traderID;
	int userCount;
	
	public SPacketSyncUsers(long traderID, int userCount)
	{
		super(TYPE);
		this.traderID = traderID;
		this.userCount = userCount;
	}

	private static class H extends Handler<SPacketSyncUsers>
	{
		protected H() { super(TYPE,STREAM_CODEC);
        }
		@Override
		protected void handle(SPacketSyncUsers message, IPayloadContext context, Player player) {
			TraderData trader = TraderAPI.getApi().GetTrader(true, message.traderID);
			if(trader != null)
				trader.updateUserCount(message.userCount);
		}
	}

}
