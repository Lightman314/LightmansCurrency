package io.github.lightman314.lightmanscurrency.core;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class LCGameRules {
    private LCGameRules() {}

    public static final DeferredRegister<GameRule<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.GAME_RULE, LCApi.MODID);

    public static final DeferredHolder<GameRule<?>,GameRule<Boolean>> KEEP_WALLET = registerBoolean("keep_wallet",GameRuleCategory.PLAYER,false);

    public static DeferredHolder<GameRule<?>,GameRule<Boolean>> registerBoolean(String name,GameRuleCategory category,boolean defaultValue) {
        return register(name,() -> new GameRule<>(category,GameRuleType.BOOL,BoolArgumentType.bool(),GameRuleTypeVisitor::visitBoolean,Codec.BOOL,b -> b ? 1 : 0,defaultValue, FeatureFlagSet.of()));
    }
    public static <T> DeferredHolder<GameRule<?>,GameRule<T>> register(String name, Supplier<GameRule<T>> rule) {
        return REGISTER.register(name,rule);
    }

}