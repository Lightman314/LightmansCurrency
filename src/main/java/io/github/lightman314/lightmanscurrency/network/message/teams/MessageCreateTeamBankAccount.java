package io.github.lightman314.lightmanscurrency.network.message.teams;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageCreateTeamBankAccount {
	
	UUID teamID;
	
	public MessageCreateTeamBankAccount(UUID teamID)
	{
		this.teamID = teamID;
	}
	
	public static void encode(MessageCreateTeamBankAccount message, PacketBuffer buffer) {
		buffer.writeUniqueId(message.teamID);
	}

	public static MessageCreateTeamBankAccount decode(PacketBuffer buffer) {
		return new MessageCreateTeamBankAccount(buffer.readUniqueId());
	}

	public static void handle(MessageCreateTeamBankAccount message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> {
			Team team = TradingOffice.getTeam(message.teamID);
			if(team != null)
			{
				team.createBankAccount(supplier.get().getSender());
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
