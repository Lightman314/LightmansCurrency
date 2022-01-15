package io.github.lightman314.lightmanscurrency.network.message.teams;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageCreateTeamResponse {
	
	UUID teamID;
	
	public MessageCreateTeamResponse(UUID teamID)
	{
		this.teamID = teamID;
	}
	
	public static void encode(MessageCreateTeamResponse message, PacketBuffer buffer) {
		buffer.writeUniqueId(message.teamID);
	}

	public static MessageCreateTeamResponse decode(PacketBuffer buffer) {
		return new MessageCreateTeamResponse(buffer.readUniqueId());
	}

	public static void handle(MessageCreateTeamResponse message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> {
			LightmansCurrency.PROXY.createTeamResponse(message.teamID);
		});
		supplier.get().setPacketHandled(true);
	}

}
