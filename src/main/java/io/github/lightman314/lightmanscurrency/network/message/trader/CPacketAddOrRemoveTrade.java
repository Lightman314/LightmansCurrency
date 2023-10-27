package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CPacketAddOrRemoveTrade extends ClientToServerPacket {

	public static final Handler<CPacketAddOrRemoveTrade> HANDLER = new H();

	long traderID;
	boolean isTradeAdd;
	
	public CPacketAddOrRemoveTrade(long traderID, boolean isTradeAdd)
	{
		this.traderID = traderID;
		this.isTradeAdd = isTradeAdd;
	}
	
	public void encode(@Nonnull FriendlyByteBuf buffer) {
		buffer.writeLong(this.traderID);
		buffer.writeBoolean(this.isTradeAdd);
	}

	private static class H extends Handler<CPacketAddOrRemoveTrade>
	{
		@Nonnull
		@Override
		public CPacketAddOrRemoveTrade decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketAddOrRemoveTrade(buffer.readLong(), buffer.readBoolean()); }
		@Override
		protected void handle(@Nonnull CPacketAddOrRemoveTrade message, @Nullable ServerPlayer sender) {
			if(sender != null)
			{
				TraderData trader = TraderSaveData.GetTrader(false, message.traderID);
				if(trader != null)
				{
					if(message.isTradeAdd)
						trader.addTrade(sender);
					else
						trader.removeTrade(sender);
				}
			}
		}
	}

}
