package io.github.lightman314.lightmanscurrency.network.message.teams;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageCreateTeamResponse {
	
	long teamID;
	
	public MessageCreateTeamResponse(long teamID)
	{
		this.teamID = teamID;
	}
	
	public static void encode(MessageCreateTeamResponse message, FriendlyByteBuf buffer) {
		buffer.writeLong(message.teamID);
	}

	public static MessageCreateTeamResponse decode(FriendlyByteBuf buffer) {
		return new MessageCreateTeamResponse(buffer.readLong());
	}

	public static void handle(MessageCreateTeamResponse message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> {
			LightmansCurrency.PROXY.createTeamResponse(message.teamID);
		});
		supplier.get().setPacketHandled(true);
	}

}
