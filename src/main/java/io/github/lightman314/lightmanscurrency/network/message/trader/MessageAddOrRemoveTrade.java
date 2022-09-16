package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageAddOrRemoveTrade {
	
	long traderID;
	boolean isTradeAdd;
	
	public MessageAddOrRemoveTrade(long traderID, boolean isTradeAdd)
	{
		this.traderID = traderID;
		this.isTradeAdd = isTradeAdd;
	}
	
	public static void encode(MessageAddOrRemoveTrade message, FriendlyByteBuf buffer) {
		buffer.writeLong(message.traderID);
		buffer.writeBoolean(message.isTradeAdd);
	}

	public static MessageAddOrRemoveTrade decode(FriendlyByteBuf buffer) {
		return new MessageAddOrRemoveTrade(buffer.readLong(), buffer.readBoolean());
	}

	public static void handle(MessageAddOrRemoveTrade message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				TraderData trader = TraderSaveData.GetTrader(false, message.traderID);
				if(trader != null)
				{
					if(message.isTradeAdd)
						trader.addTrade(player);
					else
						trader.removeTrade(player);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
