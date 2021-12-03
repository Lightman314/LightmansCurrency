package io.github.lightman314.lightmanscurrency.network.message.config;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.Config;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageSyncConfig implements IMessage<MessageSyncConfig> {
	
	CompoundNBT data;
	
	public MessageSyncConfig()
	{
		
	}
	
	public MessageSyncConfig(CompoundNBT data)
	{
		this.data = data;
	}
	
	@Override
	public void encode(MessageSyncConfig message, PacketBuffer buffer) {
		buffer.writeCompoundTag(message.data);
	}

	@Override
	public MessageSyncConfig decode(PacketBuffer buffer) {
		return new MessageSyncConfig(buffer.readCompoundTag());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(MessageSyncConfig message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			Config.syncConfig(message.data);
		});
		supplier.get().setPacketHandled(true);
	}

}
