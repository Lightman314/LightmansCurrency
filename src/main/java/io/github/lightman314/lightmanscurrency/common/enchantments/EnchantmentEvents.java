package io.github.lightman314.lightmanscurrency.common.enchantments;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.enchantments.EnchantmentUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber
public final class EnchantmentEvents {

	private EnchantmentEvents() {}

	private static int ticker = 0;

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Pre event) {
		ticker++;
		//Confirm we have time, and that the tick count matches the tick delay
		if(ticker >= LCConfig.SERVER.enchantmentTickDelay.get())
		{
			ProfilerFiller filler = event.getServer().getProfiler();
			filler.push("Lightman's Currency Enchantment Ticks");
			ticker = 0;
			for(ServerPlayer player : event.getServer().getPlayerList().getPlayers())
			{
				if(!player.isSpectator())
					EnchantmentUtil.tickAllEnchantments(player,null);
			}
			filler.pop();
		}
	}
	
}
