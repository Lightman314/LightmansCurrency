package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.commands.arguments.*;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;

public class ModCommandArguments {

	public static void init() {}
	
	static {
		
		TRADER_ARGUMENT = ArgumentTypeInfos.registerByClass(TraderArgument.class, new TraderArgument.Info());
		
	}
	
	public static final ArgumentTypeInfo<TraderArgument, ?> TRADER_ARGUMENT;
	
}
