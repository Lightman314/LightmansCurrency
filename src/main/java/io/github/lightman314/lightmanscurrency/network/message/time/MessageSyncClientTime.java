package io.github.lightman314.lightmanscurrency.network.message.time;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageSyncClientTime {
	
	private long time = TimeUtil.getCurrentTime();
	
	public MessageSyncClientTime() { }
	
	private MessageSyncClientTime(long time)
	{
		this.time = time;
	}
	
	public static void encode(MessageSyncClientTime message, PacketBuffer buffer) {
		buffer.writeLong(TimeUtil.getCurrentTime());
	}

	public static MessageSyncClientTime decode(PacketBuffer buffer) {
		return new MessageSyncClientTime(buffer.readLong());
	}

	public static void handle(MessageSyncClientTime message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.setTimeDesync(message.time));
		supplier.get().setPacketHandled(true);
	}

}
