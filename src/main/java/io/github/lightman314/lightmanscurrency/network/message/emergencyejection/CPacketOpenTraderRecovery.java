package io.github.lightman314.lightmanscurrency.network.message.emergencyejection;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.menus.TraderRecoveryMenu;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.NetworkHooks;

public class CPacketOpenTraderRecovery {
	
	public static void handle(CPacketOpenTraderRecovery message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> {
			ServerPlayerEntity player = supplier.get().getSender();
			NetworkHooks.openGui(player, TraderRecoveryMenu.PROVIDER);
		});
		supplier.get().setPacketHandled(true);
	}
	
}
