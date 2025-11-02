package io.github.lightman314.lightmanscurrency.network.message.command;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SPacketDebugTrader extends ServerToClientPacket {

	public static final Handler<SPacketDebugTrader> HANDLER = new H();

	final long traderID;
	
	public SPacketDebugTrader(long traderID) { this.traderID = traderID; }
	
	public void encode(FriendlyByteBuf buffer) { buffer.writeLong(this.traderID); }

	private static class H extends Handler<SPacketDebugTrader>
	{
		@Override
		public SPacketDebugTrader decode(FriendlyByteBuf buffer) { return new SPacketDebugTrader(buffer.readLong());}
		@Override
		protected void handle(SPacketDebugTrader message, Player player) {
			TraderData trader = TraderAPI.getApi().GetTrader(true, message.traderID);
			if(trader == null)
				LightmansCurrency.LogInfo("Client is missing trader with id " + message.traderID + "!");
			else
				LightmansCurrency.LogInfo("Client Trader NBT for trader " + message.traderID + ":\n" + trader.save());
		}
	}
	
}
