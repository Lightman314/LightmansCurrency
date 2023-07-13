package io.github.lightman314.lightmanscurrency.network.message.teams;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageCreateTeamBankAccount {
	
	long teamID;
	
	public MessageCreateTeamBankAccount(long teamID) {
		this.teamID = teamID;
	}
	
	public static void encode(MessageCreateTeamBankAccount message, FriendlyByteBuf buffer) {
		buffer.writeLong(message.teamID);
	}

	public static MessageCreateTeamBankAccount decode(FriendlyByteBuf buffer) {
		return new MessageCreateTeamBankAccount(buffer.readLong());
	}

	public static void handle(MessageCreateTeamBankAccount message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> {
			Team team = TeamSaveData.GetTeam(false, message.teamID);
			if(team != null)
				team.createBankAccount(supplier.get().getSender());
		});
		supplier.get().setPacketHandled(true);
	}

}
