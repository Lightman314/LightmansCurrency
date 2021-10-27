package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageRemoveClientTrader implements IMessage<MessageRemoveClientTrader> {
	
	UUID traderId;
	
	public MessageRemoveClientTrader()
	{
		
	}
	
	public MessageRemoveClientTrader(UUID traderId)
	{
		this.traderId = traderId;
	}
	
	@Override
	public void encode(MessageRemoveClientTrader message, PacketBuffer buffer) {
		buffer.writeUniqueId(message.traderId);
	}

	@Override
	public MessageRemoveClientTrader decode(PacketBuffer buffer) {
		return new MessageRemoveClientTrader(buffer.readUniqueId());
	}

	@Override
	public void handle(MessageRemoveClientTrader message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.removeTrader(message.traderId));
		supplier.get().setPacketHandled(true);
	}

}
