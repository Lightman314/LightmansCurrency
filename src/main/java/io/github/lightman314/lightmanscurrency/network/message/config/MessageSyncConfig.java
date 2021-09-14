package io.github.lightman314.lightmanscurrency.network.message.config;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.Config;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;

public class MessageSyncConfig implements IMessage<MessageSyncConfig> {
	
	CompoundTag data;
	
	public MessageSyncConfig()
	{
		
	}
	
	public MessageSyncConfig(CompoundTag data)
	{
		this.data = data;
	}
	
	@Override
	public void encode(MessageSyncConfig message, FriendlyByteBuf buffer) {
		buffer.writeNbt(message.data);
	}

	@Override
	public MessageSyncConfig decode(FriendlyByteBuf buffer) {
		return new MessageSyncConfig(buffer.readNbt());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(MessageSyncConfig message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			Config.syncConfig(message.data);
		});
		supplier.get().setPacketHandled(true);
	}

}
