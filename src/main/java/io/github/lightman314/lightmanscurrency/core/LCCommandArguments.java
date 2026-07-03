package io.github.lightman314.lightmanscurrency.core;

import com.mojang.brigadier.arguments.ArgumentType;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.command.arguments.MoneyValueArgument;
import io.github.lightman314.lightmanscurrency.api.command.arguments.TraderArgument;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

public final class LCCommandArguments {
    private LCCommandArguments() {}

    public static final DeferredRegister<ArgumentTypeInfo<?,?>> REGISTER = DeferredRegister.create(BuiltInRegistries.COMMAND_ARGUMENT_TYPE,LCApi.MODID);

    public static final DeferredHolder<ArgumentTypeInfo<?,?>,ArgumentTypeInfo<TraderArgument,TraderArgument.Info.Template>> TRADER_ARGUMENT = register("trader_argument",TraderArgument.class,TraderArgument.Info::new);
    public static final DeferredHolder<ArgumentTypeInfo<?,?>,SingletonArgumentInfo<MoneyValueArgument>> MONEY_ARGUMENT = registerContextAware("money_value",MoneyValueArgument.class,MoneyValueArgument::argument);

    public static <A extends ArgumentType<?>> DeferredHolder<ArgumentTypeInfo<?,?>,SingletonArgumentInfo<A>> registerContextAware(String id, Class<A> infoClass,Function<CommandBuildContext,A> factory) {
        return REGISTER.register(id,() -> ArgumentTypeInfos.registerByClass(infoClass,SingletonArgumentInfo.contextAware(factory)));
    }
    public static <A extends ArgumentType<?>> DeferredHolder<ArgumentTypeInfo<?,?>,SingletonArgumentInfo<A>> registerContextFree(String id, Class<A> infoClass,Supplier<A> factory) {
        return REGISTER.register(id,() -> ArgumentTypeInfos.registerByClass(infoClass,SingletonArgumentInfo.contextFree(factory)));
    }
    public static <A extends ArgumentType<?>,T extends ArgumentTypeInfo.Template<A>,I extends ArgumentTypeInfo<A,T>> DeferredHolder<ArgumentTypeInfo<?,?>,I> register(String id, Class<A> infoClass, Supplier<I> factory) {
        return REGISTER.register(id,() -> ArgumentTypeInfos.registerByClass(infoClass,factory.get()));
    }

}