package io.github.lightman314.lightmanscurrency.network.message.time;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageSyncClientTime {
	
	private long time = TimeUtil.getCurrentTime();
	
	public MessageSyncClientTime() { }
	
	private MessageSyncClientTime(long time)
	{
		this.time = time;
	}
	
	public static void encode(MessageSyncClientTime message, FriendlyByteBuf buffer) {
		buffer.writeLong(TimeUtil.getCurrentTime());
	}

	public static MessageSyncClientTime decode(FriendlyByteBuf buffer) {
		return new MessageSyncClientTime(buffer.readLong());
	}

	public static void handle(MessageSyncClientTime message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.setTimeDesync(message.time));
		supplier.get().setPacketHandled(true);
	}

}
