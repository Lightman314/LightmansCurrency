package io.github.lightman314.lightmanscurrency.network.message.teams;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageOpenTeamManager {
	
	public MessageOpenTeamManager()
	{
		
	}
	
	public static void encode(MessageOpenTeamManager message, FriendlyByteBuf buffer) { }

	public static MessageOpenTeamManager decode(FriendlyByteBuf buffer) {
		return new MessageOpenTeamManager();
	}

	public static void handle(MessageOpenTeamManager message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> {
			LightmansCurrency.PROXY.openTeamManager();
		});
		supplier.get().setPacketHandled(true);
	}

}
