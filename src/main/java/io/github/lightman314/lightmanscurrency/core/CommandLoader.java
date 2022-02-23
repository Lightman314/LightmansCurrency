package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.commands.*;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CommandLoader {

	@SubscribeEvent
	public static void onCommandLoading(RegisterCommandsEvent event)
	{
		//lcadmin
		CommandLCAdmin.register(event.getDispatcher());
		CommandTeamManager.register(event.getDispatcher());
		CommandReloadTraders.register(event.getDispatcher());
		
	}
	
}
