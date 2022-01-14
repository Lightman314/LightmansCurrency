package io.github.lightman314.lightmanscurrency.network.message.teams;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageRemoveClientTeam {
	
	UUID traderId;
	
	public MessageRemoveClientTeam(UUID traderId)
	{
		this.traderId = traderId;
	}
	
	public static void encode(MessageRemoveClientTeam message, FriendlyByteBuf buffer) {
		buffer.writeUUID(message.traderId);
	}

	public static MessageRemoveClientTeam decode(FriendlyByteBuf buffer) {
		return new MessageRemoveClientTeam(buffer.readUUID());
	}

	public static void handle(MessageRemoveClientTeam message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.removeTeam(message.traderId));
		supplier.get().setPacketHandled(true);
	}

}
