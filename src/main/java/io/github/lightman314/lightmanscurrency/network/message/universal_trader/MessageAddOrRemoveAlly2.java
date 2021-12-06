package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageAddOrRemoveAlly2 {
	
	UUID traderID;
	boolean isAllyAdd;
	String ally;
	
	public MessageAddOrRemoveAlly2(UUID traderID, boolean isAllyAdd, String ally)
	{
		this.traderID = traderID;
		this.isAllyAdd = isAllyAdd;
		this.ally = ally;
	}
	
	public static void encode(MessageAddOrRemoveAlly2 message, FriendlyByteBuf buffer) {
		buffer.writeUUID(message.traderID);
		buffer.writeBoolean(message.isAllyAdd);
		buffer.writeUtf(message.ally, 32);
	}

	public static MessageAddOrRemoveAlly2 decode(FriendlyByteBuf buffer) {
		return new MessageAddOrRemoveAlly2(buffer.readUUID(), buffer.readBoolean(), buffer.readUtf(32));
	}

	public static void handle(MessageAddOrRemoveAlly2 message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			UniversalTraderData data = TradingOffice.getData(message.traderID);
			if(data != null)
			{
				if(message.isAllyAdd)
				{
					if(!data.getAllies().contains(message.ally))
					{
						data.getAllies().add(message.ally);
						data.markAlliesDirty();
					}
				}
				else
				{
					if(data.getAllies().contains(message.ally))
					{
						data.getAllies().remove(message.ally);
						data.markAlliesDirty();
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
