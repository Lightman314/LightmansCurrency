package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageClearClientTraders implements IMessage<MessageClearClientTraders> {
	
	public MessageClearClientTraders() { }
	
	@Override
	public void encode(MessageClearClientTraders message, PacketBuffer buffer) { }

	@Override
	public MessageClearClientTraders decode(PacketBuffer buffer) {
		return new MessageClearClientTraders();
	}

	@Override
	public void handle(MessageClearClientTraders message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.clearClientTraders());
		supplier.get().setPacketHandled(true);
	}

}
