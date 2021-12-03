package io.github.lightman314.lightmanscurrency.network.message.time;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageSyncClientTime implements IMessage<MessageSyncClientTime> {
	
	private long time = TimeUtil.getCurrentTime();
	
	public MessageSyncClientTime()
	{
		
	}
	
	private MessageSyncClientTime(long time)
	{
		this.time = time;
	}
	
	@Override
	public void encode(MessageSyncClientTime message, PacketBuffer buffer) {
		buffer.writeLong(TimeUtil.getCurrentTime());
	}

	@Override
	public MessageSyncClientTime decode(PacketBuffer buffer) {
		return new MessageSyncClientTime(buffer.readLong());
	}

	@Override
	public void handle(MessageSyncClientTime message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			LightmansCurrency.PROXY.setTimeDesync(message.time);
		});
		supplier.get().setPacketHandled(true);
	}

}
