package io.github.lightman314.lightmanscurrency.network.message.teams;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageDisbandTeam {
	
	UUID teamID;
	
	public MessageDisbandTeam(UUID teamID)
	{
		this.teamID = teamID;
	}
	
	public static void encode(MessageDisbandTeam message, PacketBuffer buffer) {
		buffer.writeUniqueId(message.teamID);
	}

	public static MessageDisbandTeam decode(PacketBuffer buffer) {
		return new MessageDisbandTeam(buffer.readUniqueId());
	}

	public static void handle(MessageDisbandTeam message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> {
			Team team = TradingOffice.getTeam(message.teamID);
			if(team != null)
			{
				if(team.isOwner(supplier.get().getSender()))
				{
					TradingOffice.removeTeam(team.getID());
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
