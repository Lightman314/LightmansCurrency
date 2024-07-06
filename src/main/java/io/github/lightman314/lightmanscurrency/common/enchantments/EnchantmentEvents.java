package io.github.lightman314.lightmanscurrency.common.enchantments;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.enchantments.EnchantmentUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public final class EnchantmentEvents {

	private EnchantmentEvents() {}

	private static int ticker = 0;

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if(event.phase == TickEvent.Phase.START)
		{
			ticker++;
			//Confirm we have time, and that the tick count matches the tick delay
			if(ticker >= LCConfig.SERVER.enchantmentTickDelay.get())
			{
				ticker = 0;
				//Since we're only running the tick on players now, we might as well do it from the server tick where we can confirm
				//that we have time to spare on this process
				for(ServerPlayer player : event.getServer().getPlayerList().getPlayers())
				{
					if(!player.isSpectator())
						EnchantmentUtil.tickAllEnchantments(player,null);
				}
			}
		}
	}
	
}
