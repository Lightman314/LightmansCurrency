package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.containers.UniversalContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageUpdateContainerData implements IMessage<MessageUpdateContainerData> {
	
	UUID playerID;
	CompoundNBT traderData;
	
	public MessageUpdateContainerData()
	{
		
	}
	
	public MessageUpdateContainerData(UUID playerID, CompoundNBT traderData)
	{
		this.playerID = playerID;
		this.traderData = traderData;
	}
	
	@Override
	public void encode(MessageUpdateContainerData message, PacketBuffer buffer) {
		buffer.writeUniqueId(message.playerID);
		buffer.writeCompoundTag(message.traderData);
	}

	@Override
	public MessageUpdateContainerData decode(PacketBuffer buffer) {
		return new MessageUpdateContainerData(buffer.readUniqueId(), buffer.readCompoundTag());
	}

	@Override
	public void handle(MessageUpdateContainerData message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> {
			Minecraft minecraft = Minecraft.getInstance();
			if(minecraft != null)
			{
				PlayerEntity player = minecraft.world.getPlayerByUuid(message.playerID);
				if(player != null)
				{
					if(player.openContainer instanceof UniversalContainer)
					{
						UniversalContainer container = (UniversalContainer)player.openContainer;
						container.onDataUpdated(message.traderData);
						LightmansCurrency.LogDebug("Data synced on client-side.");
					}
					else
						LightmansCurrency.LogWarning("Open container is not a UniversalContainer?");
				}
				else
					LightmansCurrency.LogWarning("Player not found?");
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
