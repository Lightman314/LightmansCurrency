package io.github.lightman314.lightmanscurrency.network.message.enchantments;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraftforge.network.NetworkEvent.Context;

public class SPacketMoneyMendingClink {

	private static long lastClink = 0;
	private static final long CLINK_DELAY = 1000;
	
	public static void handle(SPacketMoneyMendingClink message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//Ignore if not enough time has passed since the last sound
			if(System.currentTimeMillis() - lastClink < CLINK_DELAY || !Config.CLIENT.moneyMendingClink.get())
				return;
			lastClink = System.currentTimeMillis();
			//Play a coin clinking sound
			LightmansCurrency.PROXY.playCoinSound();
		});
		supplier.get().setPacketHandled(true);
	}
	
}
