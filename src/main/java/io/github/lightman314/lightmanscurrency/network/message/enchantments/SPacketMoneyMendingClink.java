package io.github.lightman314.lightmanscurrency.network.message.enchantments;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraftforge.network.NetworkEvent.Context;

public class SPacketMoneyMendingClink {

	public static void handle(SPacketMoneyMendingClink ignored, Supplier<Context> supplier) {
		supplier.get().enqueueWork(LightmansCurrency.PROXY::playCoinSound);
		supplier.get().setPacketHandled(true);
	}
	
}
