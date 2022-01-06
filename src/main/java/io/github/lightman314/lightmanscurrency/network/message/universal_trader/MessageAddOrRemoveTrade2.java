package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageAddOrRemoveTrade2 {
	
	UUID traderID;
	boolean isTradeAdd;
	
	public MessageAddOrRemoveTrade2(UUID traderID, boolean isTradeAdd)
	{
		this.traderID = traderID;
		this.isTradeAdd = isTradeAdd;
	}
	
	public static void encode(MessageAddOrRemoveTrade2 message, FriendlyByteBuf buffer) {
		buffer.writeUUID(message.traderID);
		buffer.writeBoolean(message.isTradeAdd);
	}

	public static MessageAddOrRemoveTrade2 decode(FriendlyByteBuf buffer) {
		return new MessageAddOrRemoveTrade2(buffer.readUUID(), buffer.readBoolean());
	}

	public static void handle(MessageAddOrRemoveTrade2 message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				UniversalTraderData data = TradingOffice.getData(message.traderID);
				if(data != null)
				{
					if(message.isTradeAdd)
						data.addTrade(player);
					else
						data.removeTrade(player);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
