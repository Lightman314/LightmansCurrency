package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.commands.arguments.*;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.registries.RegistryObject;

public class ModCommandArguments {

	public static void init() {}
	
	static {
		
		TRADER_ARGUMENT = ModRegistries.COMMAND_ARGUMENT_TYPES.register("trader_argument", () -> ArgumentTypeInfos.registerByClass(TraderArgument.class, new TraderArgument.Info()));
		TRADE_ID_ARGUMENT = ModRegistries.COMMAND_ARGUMENT_TYPES.register("trade_id_argument", () -> ArgumentTypeInfos.registerByClass(TradeIDArgument.class, SingletonArgumentInfo.contextFree(TradeIDArgument::argument)));
		COLOR_ARGUMENT = ModRegistries.COMMAND_ARGUMENT_TYPES.register("color_argument", () -> ArgumentTypeInfos.registerByClass(ColorArgument.class, SingletonArgumentInfo.contextFree(ColorArgument::argument)));
		COIN_VALUE_ARGUMENT = ModRegistries.COMMAND_ARGUMENT_TYPES.register("coin_value_argument", () -> ArgumentTypeInfos.registerByClass(CoinValueArgument.class, SingletonArgumentInfo.contextAware(CoinValueArgument::argument)));

	}
	
	public static final RegistryObject<ArgumentTypeInfo<TraderArgument, ?>> TRADER_ARGUMENT;
	public static final RegistryObject<ArgumentTypeInfo<TradeIDArgument, ?>> TRADE_ID_ARGUMENT;
	public static final RegistryObject<ArgumentTypeInfo<ColorArgument, ?>> COLOR_ARGUMENT;
	public static final RegistryObject<ArgumentTypeInfo<CoinValueArgument,?>> COIN_VALUE_ARGUMENT;

}
