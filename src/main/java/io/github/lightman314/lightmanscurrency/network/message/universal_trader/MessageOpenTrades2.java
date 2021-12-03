package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageOpenTrades2 implements IMessage<MessageOpenTrades2> {
	
	UUID traderID;
	
	public MessageOpenTrades2()
	{
		
	}
	
	public MessageOpenTrades2(UUID traderID)
	{
		this.traderID = traderID;
	}
	
	
	@Override
	public void encode(MessageOpenTrades2 message, PacketBuffer buffer) {
		buffer.writeUniqueId(message.traderID);
	}

	@Override
	public MessageOpenTrades2 decode(PacketBuffer buffer) {
		return new MessageOpenTrades2(buffer.readUniqueId());
	}

	@Override
	public void handle(MessageOpenTrades2 message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null)
			{
				UniversalTraderData data = TradingOffice.getData(message.traderID);
				if(data != null)
				{
					data.openTradeMenu(player);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
