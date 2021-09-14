package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.containers.UniversalContainer;
//import io.github.lightman314.lightmanscurrency.containers.UniversalContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class MessageUpdateContainerData implements IMessage<MessageUpdateContainerData> {
	
	UUID playerID;
	CompoundTag traderData;
	
	public MessageUpdateContainerData()
	{
		
	}
	
	public MessageUpdateContainerData(UUID playerID, CompoundTag traderData)
	{
		this.playerID = playerID;
		this.traderData = traderData;
	}
	
	@Override
	public void encode(MessageUpdateContainerData message, FriendlyByteBuf buffer) {
		buffer.writeUUID(message.playerID);
		buffer.writeNbt(message.traderData);
	}

	@Override
	public MessageUpdateContainerData decode(FriendlyByteBuf buffer) {
		return new MessageUpdateContainerData(buffer.readUUID(), buffer.readNbt());
	}

	@Override
	public void handle(MessageUpdateContainerData message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() -> {
			Minecraft minecraft = Minecraft.getInstance();
			if(minecraft != null)
			{
				Player player = minecraft.level.getPlayerByUUID(message.playerID);
				if(player != null)
				{
					if(player.containerMenu instanceof UniversalContainer)
					{
						UniversalContainer container = (UniversalContainer)player.containerMenu;
						container.onDataUpdated(message.traderData);
						LightmansCurrency.LogInfo("Data synced on client-side.");
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
