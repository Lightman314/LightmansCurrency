package io.github.lightman314.lightmanscurrency.network.message.teams;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageEditTeam {
	
	UUID teamID;
	String playerName;
	String category;
	
	public MessageEditTeam(UUID teamID, String playerName, String category)
	{
		this.teamID = teamID;
		this.playerName = playerName;
		this.category = category;
	}
	
	public static void encode(MessageEditTeam message, PacketBuffer buffer) {
		buffer.writeUniqueId(message.teamID);
		buffer.writeString(message.playerName, 16);
		buffer.writeString(message.category);
	}

	public static MessageEditTeam decode(PacketBuffer buffer) {
		return new MessageEditTeam(buffer.readUniqueId(), buffer.readString(16), buffer.readString(255));
	}

	public static void handle(MessageEditTeam message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> {
			Team team = TradingOffice.getTeam(message.teamID);
			if(team != null)
			{
				team.changeAny(supplier.get().getSender(), message.playerName, message.category);
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
