package io.github.lightman314.lightmanscurrency.network.message.teams;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageDisbandTeam {
	
	long teamID;
	
	public MessageDisbandTeam(long teamID)
	{
		this.teamID = teamID;
	}
	
	public static void encode(MessageDisbandTeam message, PacketBuffer buffer) {
		buffer.writeLong(message.teamID);
	}

	public static MessageDisbandTeam decode(PacketBuffer buffer) {
		return new MessageDisbandTeam(buffer.readLong());
	}

	public static void handle(MessageDisbandTeam message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> {
			Team team = TeamSaveData.GetTeam(false, message.teamID);
			if(team != null)
			{
				if(team.isOwner(supplier.get().getSender()))
				{
					TeamSaveData.RemoveTeam(team.getID());
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}