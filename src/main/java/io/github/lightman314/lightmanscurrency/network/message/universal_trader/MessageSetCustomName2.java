package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageSetCustomName2 {
	
	UUID traderID;
	String customName;
	
	public MessageSetCustomName2(UUID traderID, String customName)
	{
		this.traderID = traderID;
		this.customName = customName;
	}
	
	public static void encode(MessageSetCustomName2 message, FriendlyByteBuf buffer) {
		buffer.writeUUID(message.traderID);
		buffer.writeUtf(message.customName);
	}

	public static MessageSetCustomName2 decode(FriendlyByteBuf buffer) {
		return new MessageSetCustomName2(buffer.readUUID(), buffer.readUtf());
	}

	public static void handle(MessageSetCustomName2 message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			UniversalTraderData data = TradingOffice.getData(message.traderID);
			if(data != null)
			{
				data.setName(message.customName);
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
