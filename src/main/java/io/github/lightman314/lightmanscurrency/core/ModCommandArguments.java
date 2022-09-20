package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.commands.arguments.*;
import net.minecraft.commands.synchronization.ArgumentTypes;

public class ModCommandArguments {

	public static void init() {}
	
	static {
		
		ArgumentTypes.register("lightmanscurrency:trader", TraderArgument.class, new TraderArgument.Info());
		
	}
	
}