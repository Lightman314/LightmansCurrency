package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.commands.arguments.CoinValueArgument;
import io.github.lightman314.lightmanscurrency.common.commands.arguments.ColorArgument;
import io.github.lightman314.lightmanscurrency.common.commands.arguments.TradeIDArgument;
import io.github.lightman314.lightmanscurrency.common.commands.arguments.TraderArgument;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;

public class ModCommandArguments {

	public static void init() {}
	
	static {
		
		ArgumentTypes.register("lightmanscurrency:trader_argument", TraderArgument.class, new TraderArgument.Info());
		ArgumentTypes.register("lightmanscurrency:trade_id_argument", TradeIDArgument.class, new EmptyArgumentSerializer<>(TradeIDArgument::argument));
		ArgumentTypes.register("lightmanscurrency:color_argument", ColorArgument.class, new EmptyArgumentSerializer<>(ColorArgument::argument));
		ArgumentTypes.register("lightmanscurrency:coinvalue_argument", CoinValueArgument.class, new EmptyArgumentSerializer<>(CoinValueArgument::argument));

	}
	
}