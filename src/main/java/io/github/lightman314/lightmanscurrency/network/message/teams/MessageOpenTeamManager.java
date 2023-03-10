package io.github.lightman314.lightmanscurrency.network.message.teams;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageOpenTeamManager {

	public static void handle(MessageOpenTeamManager message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(LightmansCurrency.PROXY::openTeamManager);
		supplier.get().setPacketHandled(true);
	}

}
