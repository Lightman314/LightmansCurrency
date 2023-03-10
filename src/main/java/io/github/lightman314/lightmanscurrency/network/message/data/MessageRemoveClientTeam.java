package io.github.lightman314.lightmanscurrency.network.message.data;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageRemoveClientTeam {
	
	long teamID;
	
	public MessageRemoveClientTeam(long teamID)
	{
		this.teamID = teamID;
	}
	
	public static void encode(MessageRemoveClientTeam message, PacketBuffer buffer) {
		buffer.writeLong(message.teamID);
	}

	public static MessageRemoveClientTeam decode(PacketBuffer buffer) {
		return new MessageRemoveClientTeam(buffer.readLong());
	}

	public static void handle(MessageRemoveClientTeam message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.removeTeam(message.teamID));
		supplier.get().setPacketHandled(true);
	}

}