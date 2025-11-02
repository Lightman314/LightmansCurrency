package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketAddOrRemoveTrade extends ClientToServerPacket {

	public static final Handler<CPacketAddOrRemoveTrade> HANDLER = new H();

	long traderID;
	boolean isTradeAdd;
	
	public CPacketAddOrRemoveTrade(long traderID, boolean isTradeAdd)
	{
		this.traderID = traderID;
		this.isTradeAdd = isTradeAdd;
	}
	
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeLong(this.traderID);
		buffer.writeBoolean(this.isTradeAdd);
	}

	private static class H extends Handler<CPacketAddOrRemoveTrade>
	{
		@Override
		public CPacketAddOrRemoveTrade decode(FriendlyByteBuf buffer) { return new CPacketAddOrRemoveTrade(buffer.readLong(), buffer.readBoolean()); }
		@Override
		protected void handle(CPacketAddOrRemoveTrade message, Player player) {
            TraderData trader = TraderAPI.getApi().GetTrader(false, message.traderID);
            if(trader != null)
            {
                if(message.isTradeAdd)
                    trader.addTrade(player);
                else
                    trader.removeTrade(player);
            }
		}
	}

}
