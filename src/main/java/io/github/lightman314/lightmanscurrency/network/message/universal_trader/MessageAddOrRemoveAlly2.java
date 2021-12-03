package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageAddOrRemoveAlly2 implements IMessage<MessageAddOrRemoveAlly2> {
	
	UUID traderID;
	boolean isAllyAdd;
	String ally;
	
	public MessageAddOrRemoveAlly2()
	{
		
	}
	
	public MessageAddOrRemoveAlly2(UUID traderID, boolean isAllyAdd, String ally)
	{
		this.traderID = traderID;
		this.isAllyAdd = isAllyAdd;
		this.ally = ally;
	}
	
	
	@Override
	public void encode(MessageAddOrRemoveAlly2 message, PacketBuffer buffer) {
		buffer.writeUniqueId(message.traderID);
		buffer.writeBoolean(message.isAllyAdd);
		buffer.writeString(message.ally, 32);
	}

	@Override
	public MessageAddOrRemoveAlly2 decode(PacketBuffer buffer) {
		return new MessageAddOrRemoveAlly2(buffer.readUniqueId(), buffer.readBoolean(), buffer.readString(32));
	}

	@Override
	public void handle(MessageAddOrRemoveAlly2 message, Supplier<Context> supplier) {
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
