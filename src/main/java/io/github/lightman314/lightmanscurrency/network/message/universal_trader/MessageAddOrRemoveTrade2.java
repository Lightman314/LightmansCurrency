package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageAddOrRemoveTrade2 implements IMessage<MessageAddOrRemoveTrade2> {
	
	UUID traderID;
	boolean isTradeAdd;
	
	public MessageAddOrRemoveTrade2()
	{
		
	}
	
	public MessageAddOrRemoveTrade2(UUID traderID, boolean isTradeAdd)
	{
		this.traderID = traderID;
		this.isTradeAdd = isTradeAdd;
	}
	
	
	@Override
	public void encode(MessageAddOrRemoveTrade2 message, PacketBuffer buffer) {
		buffer.writeUniqueId(message.traderID);
		buffer.writeBoolean(message.isTradeAdd);
	}

	@Override
	public MessageAddOrRemoveTrade2 decode(PacketBuffer buffer) {
		return new MessageAddOrRemoveTrade2(buffer.readUniqueId(), buffer.readBoolean());
	}

	@Override
	public void handle(MessageAddOrRemoveTrade2 message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				UniversalTraderData trader = TradingOffice.getData(message.traderID);
				if(message.isTradeAdd)
					trader.addTrade(entity);
				else
					trader.removeTrade(entity);
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
