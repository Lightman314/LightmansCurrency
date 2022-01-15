package io.github.lightman314.lightmanscurrency.network.message.teams;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageRemoveClientTeam {
	
	UUID traderId;
	
	public MessageRemoveClientTeam(UUID traderId)
	{
		this.traderId = traderId;
	}
	
	public static void encode(MessageRemoveClientTeam message, PacketBuffer buffer) {
		buffer.writeUniqueId(message.traderId);
	}

	public static MessageRemoveClientTeam decode(PacketBuffer buffer) {
		return new MessageRemoveClientTeam(buffer.readUniqueId());
	}

	public static void handle(MessageRemoveClientTeam message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.removeTeam(message.traderId));
		supplier.get().setPacketHandled(true);
	}

}
