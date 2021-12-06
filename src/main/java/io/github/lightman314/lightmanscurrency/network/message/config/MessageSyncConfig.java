package io.github.lightman314.lightmanscurrency.network.message.config;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.Config;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class MessageSyncConfig {
	
	CompoundTag data;
	
	public MessageSyncConfig(CompoundTag data)
	{
		this.data = data;
	}
	
	public static void encode(MessageSyncConfig message, FriendlyByteBuf buffer) {
		buffer.writeNbt(message.data);
	}

	public static MessageSyncConfig decode(FriendlyByteBuf buffer) {
		return new MessageSyncConfig(buffer.readNbt());
	}

	public static void handle(MessageSyncConfig message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			Config.syncConfig(message.data);
		});
		supplier.get().setPacketHandled(true);
	}

}
