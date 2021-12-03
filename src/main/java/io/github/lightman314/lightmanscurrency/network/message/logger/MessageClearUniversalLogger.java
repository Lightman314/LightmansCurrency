package io.github.lightman314.lightmanscurrency.network.message.logger;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.api.ILoggerSupport;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageClearUniversalLogger implements IMessage<MessageClearUniversalLogger> {

	private UUID traderID;
	
	public MessageClearUniversalLogger()
	{
		
	}
	
	public MessageClearUniversalLogger(UUID traderID)
	{
		this.traderID = traderID;
	}
	
	
	@Override
	public void encode(MessageClearUniversalLogger message, PacketBuffer buffer) {
		buffer.writeUniqueId(message.traderID);
	}

	@Override
	public MessageClearUniversalLogger decode(PacketBuffer buffer) {
		return new MessageClearUniversalLogger(buffer.readUniqueId());
	}

	@Override
	public void handle(MessageClearUniversalLogger message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			UniversalTraderData data = TradingOffice.getData(message.traderID);
			if(data instanceof ILoggerSupport<?>)
			{
				ILoggerSupport<?> dataLogger = (ILoggerSupport<?>)data;
				dataLogger.clearLogger();
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
