package io.github.lightman314.lightmanscurrency.network.message.teams;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageDisbandTeam {
	
	UUID teamID;
	
	public MessageDisbandTeam(UUID teamID)
	{
		this.teamID = teamID;
	}
	
	public static void encode(MessageDisbandTeam message, FriendlyByteBuf buffer) {
		buffer.writeUUID(message.teamID);
	}

	public static MessageDisbandTeam decode(FriendlyByteBuf buffer) {
		return new MessageDisbandTeam(buffer.readUUID());
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
