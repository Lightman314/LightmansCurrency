package io.github.lightman314.lightmanscurrency.network.message.command;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SPacketDebugTrader extends ServerToClientPacket {

	private static final Type<SPacketDebugTrader> TYPE = sType("debug_trader_data");
    private static final StreamCodec<ByteBuf,SPacketDebugTrader> STREAM_CODEC = ByteBufCodecs.VAR_LONG
            .map(SPacketDebugTrader::new,p -> p.traderID);
	public static final Handler<SPacketDebugTrader> HANDLER = new H();

	final long traderID;
	
	public SPacketDebugTrader(long traderID) { super(TYPE); this.traderID = traderID; }

	private static class H extends Handler<SPacketDebugTrader>
	{
		protected H() { super(TYPE,STREAM_CODEC); }
		@Override
		protected void handle(SPacketDebugTrader message, IPayloadContext context, Player player) {
			TraderData trader = TraderAPI.getApi().GetTrader(true, message.traderID);
			if(trader == null)
				LightmansCurrency.LogInfo("Client is missing trader with id " + message.traderID + "!");
			else
				LightmansCurrency.LogInfo("Client Trader NBT for trader " + message.traderID + ":\n" + trader.save(DataContext.createNBT(player.registryAccess())));
		}
	}
	
}
