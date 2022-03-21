package io.github.lightman314.lightmanscurrency.discord;

import io.github.lightman314.lightmansconsole.events.JDAInitializedEvent;
import io.github.lightman314.lightmanscurrency.discord.listeners.CurrencyListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DiscordListenerRegistration {

	@SubscribeEvent
	@OnlyIn(Dist.DEDICATED_SERVER)
	public static void onJDAInit(JDAInitializedEvent event)
	{
		CurrencyListener cl = new CurrencyListener(LCDiscordConfig.DISCORD.currencyChannel::get);
		MinecraftForge.EVENT_BUS.register(cl);
		event.getProxy().addListener(cl);
	}
	
}
