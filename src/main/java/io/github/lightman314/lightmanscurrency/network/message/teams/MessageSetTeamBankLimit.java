package io.github.lightman314.lightmanscurrency.network.message.teams;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageSetTeamBankLimit {
	
	long teamID;
	int newLimit;
	
	public MessageSetTeamBankLimit(long teamID, int newLimit) {
		this.teamID = teamID;
		this.newLimit = newLimit;
	}
	
	public static void encode(MessageSetTeamBankLimit message, FriendlyByteBuf buffer) {
		buffer.writeLong(message.teamID);
		buffer.writeInt(message.newLimit);
	}

	public static MessageSetTeamBankLimit decode(FriendlyByteBuf buffer) {
		return new MessageSetTeamBankLimit(buffer.readLong(), buffer.readInt());
	}

	public static void handle(MessageSetTeamBankLimit message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> {
			Team team = TeamSaveData.GetTeam(false, message.teamID);
			if(team != null)
				team.changeBankLimit(supplier.get().getSender(), message.newLimit);
		});
		supplier.get().setPacketHandled(true);
	}

}