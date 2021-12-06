package io.github.lightman314.lightmanscurrency.network.message.logger;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.api.ILoggerSupport;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageClearUniversalLogger {

	private UUID traderID;
	
	public MessageClearUniversalLogger(UUID traderID)
	{
		this.traderID = traderID;
	}
	
	public static void encode(MessageClearUniversalLogger message, FriendlyByteBuf buffer) {
		buffer.writeUUID(message.traderID);
	}

	public static MessageClearUniversalLogger decode(FriendlyByteBuf buffer) {
		return new MessageClearUniversalLogger(buffer.readUUID());
	}

	public static void handle(MessageClearUniversalLogger message, Supplier<Context> supplier) {
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
