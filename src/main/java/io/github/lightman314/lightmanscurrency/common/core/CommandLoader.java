package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.commands.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber
public class CommandLoader {

	@SubscribeEvent
	public static void onCommandLoading(RegisterCommandsEvent event)
	{
		CommandLCAdmin.register(event.getDispatcher(), event.getBuildContext());
		CommandBalTop.register(event.getDispatcher());
		CommandPlayerTrading.register(event.getDispatcher());
		CommandTicket.register(event.getDispatcher());
		CommandBank.register(event.getDispatcher(), event.getBuildContext());
		CommandConfig.register(event.getDispatcher());
		CommandTerminal.register(event.getDispatcher());
	}
	
}
