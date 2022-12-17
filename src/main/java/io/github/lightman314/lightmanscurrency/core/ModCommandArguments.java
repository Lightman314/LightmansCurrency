package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.commands.arguments.*;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;

public class ModCommandArguments {

	public static void init() {}
	
	static {
		
		ArgumentTypes.register("lightmanscurrency:trader", TraderArgument.class, new TraderArgument.Info());
		ArgumentTypes.register("lightmanscurrency_trade_id_argument", TradeIDArgument.class, new EmptyArgumentSerializer<>(TradeIDArgument::argument));
		
	}
	
}