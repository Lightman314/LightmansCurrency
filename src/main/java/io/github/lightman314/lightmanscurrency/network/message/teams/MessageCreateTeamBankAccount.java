package io.github.lightman314.lightmanscurrency.network.message.teams;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageCreateTeamBankAccount {
	
	UUID teamID;
	
	public MessageCreateTeamBankAccount(UUID teamID) {
		this.teamID = teamID;
	}
	
	public static void encode(MessageCreateTeamBankAccount message, FriendlyByteBuf buffer) {
		buffer.writeUUID(message.teamID);
	}

	public static MessageCreateTeamBankAccount decode(FriendlyByteBuf buffer) {
		return new MessageCreateTeamBankAccount(buffer.readUUID());
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
