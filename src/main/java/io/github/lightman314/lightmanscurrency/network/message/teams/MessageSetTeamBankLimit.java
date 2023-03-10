package io.github.lightman314.lightmanscurrency.network.message.teams;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageSetTeamBankLimit {
	
	long teamID;
	int newLimit;
	
	public MessageSetTeamBankLimit(long teamID, int newLimit) {
		this.teamID = teamID;
		this.newLimit = newLimit;
	}
	
	public static void encode(MessageSetTeamBankLimit message, PacketBuffer buffer) {
		buffer.writeLong(message.teamID);
		buffer.writeInt(message.newLimit);
	}

	public static MessageSetTeamBankLimit decode(PacketBuffer buffer) {
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