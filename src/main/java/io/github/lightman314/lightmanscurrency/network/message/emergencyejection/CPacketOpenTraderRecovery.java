package io.github.lightman314.lightmanscurrency.network.message.emergencyejection;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.menus.TraderRecoveryMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.NetworkHooks;

public class CPacketOpenTraderRecovery {
	
	public static void handle(CPacketOpenTraderRecovery message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> {
			ServerPlayer player = supplier.get().getSender();
			NetworkHooks.openScreen(player, TraderRecoveryMenu.PROVIDER);
		});
		supplier.get().setPacketHandled(true);
	}
	
}
