package io.github.lightman314.lightmanscurrency.common.core;

import com.mojang.brigadier.arguments.ArgumentType;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.commands.arguments.MoneyValueArgument;
import io.github.lightman314.lightmanscurrency.common.commands.arguments.ColorArgument;
import io.github.lightman314.lightmanscurrency.common.commands.arguments.TradeIDArgument;
import io.github.lightman314.lightmanscurrency.common.commands.arguments.TraderArgument;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

public class ModCommandArguments {

    public static final DeferredRegister<ArgumentTypeInfo<?,?>> REGISTER = DeferredRegister.create(BuiltInRegistries.COMMAND_ARGUMENT_TYPE, LightmansCurrency.MODID);
	
	public static final Supplier<ArgumentTypeInfo<TraderArgument,TraderArgument.Info.Template>> TRADER_ARGUMENT = register("trader_argument",TraderArgument.class,TraderArgument.Info::new);
	public static final Supplier<SingletonArgumentInfo<TradeIDArgument>> TRADE_ID_ARGUMENT = registerContextFree("trade_id_argument",TradeIDArgument.class,TradeIDArgument::argument);
	public static final Supplier<SingletonArgumentInfo<ColorArgument>> COLOR_ARGUMENT = registerContextFree("color_argument",ColorArgument.class,ColorArgument::argument);
	public static final Supplier<SingletonArgumentInfo<MoneyValueArgument>> COIN_VALUE_ARGUMENT = registerContextAware("coin_value_argument",MoneyValueArgument.class,MoneyValueArgument::argument);

    public static <A extends ArgumentType<?>> DeferredHolder<ArgumentTypeInfo<?,?>,SingletonArgumentInfo<A>> registerContextAware(String id, Class<A> infoClass, Function<CommandBuildContext,A> factory) {
        return REGISTER.register(id,() -> SingletonArgumentInfo.contextAware(factory));
    }
    public static <A extends ArgumentType<?>> DeferredHolder<ArgumentTypeInfo<?,?>,SingletonArgumentInfo<A>> registerContextFree(String id,Class<A> infoClass,Supplier<A> factory) {
        return REGISTER.register(id,() -> SingletonArgumentInfo.contextFree(factory));
    }
    public static <A extends ArgumentType<?>,T extends ArgumentTypeInfo.Template<A>,I extends ArgumentTypeInfo<A,T>> DeferredHolder<ArgumentTypeInfo<?,?>,I> register(String id, Class<A> infoClass, Supplier<I> factory) {
        return REGISTER.register(id,() -> ArgumentTypeInfos.registerByClass(infoClass,factory.get()));
    }

}
