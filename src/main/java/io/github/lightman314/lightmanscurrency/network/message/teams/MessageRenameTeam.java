package io.github.lightman314.lightmanscurrency.network.message.teams;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageRenameTeam {
	
	long teamID;
	String newName;
	
	public MessageRenameTeam(long teamID, String newName)
	{
		this.teamID = teamID;
		this.newName = newName;
	}
	
	public static void encode(MessageRenameTeam message, FriendlyByteBuf buffer) {
		buffer.writeLong(message.teamID);
		buffer.writeUtf(message.newName, Team.MAX_NAME_LENGTH);
	}

	public static MessageRenameTeam decode(FriendlyByteBuf buffer) {
		return new MessageRenameTeam(buffer.readLong(), buffer.readUtf(Team.MAX_NAME_LENGTH));
	}

	public static void handle(MessageRenameTeam message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> {
			Team team = TeamSaveData.GetTeam(false, message.teamID);
			if(team != null)
				team.changeName(supplier.get().getSender(), message.newName);
		});
		supplier.get().setPacketHandled(true);
	}

}