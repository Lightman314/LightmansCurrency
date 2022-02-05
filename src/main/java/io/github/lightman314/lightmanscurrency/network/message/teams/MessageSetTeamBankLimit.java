package io.github.lightman314.lightmanscurrency.network.message.teams;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageSetTeamBankLimit {
	
	UUID teamID;
	int newLimit;
	
	public MessageSetTeamBankLimit(UUID teamID, int newLimit)
	{
		this.teamID = teamID;
		this.newLimit = newLimit;
	}
	
	public static void encode(MessageSetTeamBankLimit message, PacketBuffer buffer) {
		buffer.writeUniqueId(message.teamID);
		buffer.writeInt(message.newLimit);
	}

	public static MessageSetTeamBankLimit decode(PacketBuffer buffer) {
		return new MessageSetTeamBankLimit(buffer.readUniqueId(), buffer.readInt());
	}

	public static void handle(MessageSetTeamBankLimit message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> {
			Team team = TradingOffice.getTeam(message.teamID);
			if(team != null)
			{
				team.changeBankLimit(supplier.get().getSender(), message.newLimit);
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
