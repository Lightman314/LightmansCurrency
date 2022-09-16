package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.commands.arguments.*;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraftforge.registries.RegistryObject;

public class ModCommandArguments {

	public static void init() {}
	
	static {
		
		TRADER_ARGUMENT = ModRegistries.COMMAND_ARGUMENT_TYPES.register("trader_argument", () -> ArgumentTypeInfos.registerByClass(TraderArgument.class, new TraderArgument.Info()));
		
	}
	
	public static final RegistryObject<ArgumentTypeInfo<TraderArgument, ?>> TRADER_ARGUMENT;
	
}
