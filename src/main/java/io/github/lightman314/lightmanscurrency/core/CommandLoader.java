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
	}
	
	public static String test(String number)
	{
		String temp = number;
		String output = "";
		while(temp.length() > 3)
		{
			if(output.isEmpty())
				output = temp.substring(temp.length() - 3);
			else
				output = temp.substring(temp.length() - 3) + "," + output;
			temp = temp.substring(0,temp.length() - 3);
		}
		output = temp.substring(temp.length() - 3) + "," + output;
		return output;
	}
	
}
