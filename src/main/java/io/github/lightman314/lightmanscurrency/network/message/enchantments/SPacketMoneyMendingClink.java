package io.github.lightman314.lightmanscurrency.network.message.enchantments;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.CurrencySoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
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
			Minecraft minecraft = Minecraft.getInstance();
			if(minecraft != null)
				minecraft.getSoundManager().play(SimpleSoundInstance.forUI(CurrencySoundEvents.COINS_CLINKING, 1f, 0.4f));
		});
		supplier.get().setPacketHandled(true);
	}
	
}
