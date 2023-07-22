package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.commands.*;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CommandLoader {

	@SubscribeEvent
	public static void onCommandLoading(RegisterCommandsEvent event)
	{
		CommandLCAdmin.register(event.getDispatcher());
		CommandReloadData.register(event.getDispatcher());
		CommandBalTop.register(event.getDispatcher());
		CommandPlayerTrading.register(event.getDispatcher());
		CommandTicket.register(event.getDispatcher());
	}
	
}
