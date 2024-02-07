package io.github.lightman314.lightmanscurrency.integration.discord;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.integration.discord.listeners.CurrencyListener;
import io.github.lightman314.lightmansdiscord.events.JDAInitializedEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DiscordListenerRegistration {

	@SubscribeEvent
	@OnlyIn(Dist.DEDICATED_SERVER)
	public static void onJDAInit(JDAInitializedEvent event) { event.addListener(new CurrencyListener(LCConfig.SERVER.ldiCurrencyChannel::get), true); }
	
}
