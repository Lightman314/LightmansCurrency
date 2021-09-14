package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class MessageOpenStorage2 implements IMessage<MessageOpenStorage2> {
	
	UUID traderID;
	
	public MessageOpenStorage2()
	{
		
	}
	
	public MessageOpenStorage2(UUID traderID)
	{
		this.traderID = traderID;
	}
	
	
	@Override
	public void encode(MessageOpenStorage2 message, FriendlyByteBuf buffer) {
		buffer.writeUUID(message.traderID);
	}

	@Override
	public MessageOpenStorage2 decode(FriendlyByteBuf buffer) {
		return new MessageOpenStorage2(buffer.readUUID());
	}

	@Override
	public void handle(MessageOpenStorage2 message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				UniversalTraderData data = TradingOffice.getData(message.traderID);
				if(data != null)
				{
					data.openStorageMenu(player);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
