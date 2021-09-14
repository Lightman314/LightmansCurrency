package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

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
	public void encode(MessageOpenStorage2 message, PacketBuffer buffer) {
		buffer.writeUniqueId(message.traderID);
	}

	@Override
	public MessageOpenStorage2 decode(PacketBuffer buffer) {
		return new MessageOpenStorage2(buffer.readUniqueId());
	}

	@Override
	public void handle(MessageOpenStorage2 message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
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
