package io.github.lightman314.lightmanscurrency.network.message.teams;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageRenameTeam {
	
	UUID teamID;
	String newName;
	
	public MessageRenameTeam(UUID teamID, String newName)
	{
		this.teamID = teamID;
		this.newName = newName;
	}
	
	public static void encode(MessageRenameTeam message, PacketBuffer buffer) {
		buffer.writeUniqueId(message.teamID);
		buffer.writeString(message.newName, Team.MAX_NAME_LENGTH);
	}

	public static MessageRenameTeam decode(PacketBuffer buffer) {
		return new MessageRenameTeam(buffer.readUniqueId(), buffer.readString(Team.MAX_NAME_LENGTH));
	}

	public static void handle(MessageRenameTeam message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> {
			Team team = TradingOffice.getTeam(message.teamID);
			if(team != null)
			{
				team.changeName(supplier.get().getSender(), message.newName);
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
