package io.github.lightman314.lightmanscurrency.common.gamerule;

import java.util.List;
import java.util.function.BiConsumer;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModGameRules {

	private static final List<RuleData<?>> GAME_RULES = Lists.newArrayList();
	
	public static final GameRules.Key<GameRules.BooleanValue> KEEP_WALLET = register("keepWallet", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
	public static final GameRules.Key<GameRules.IntegerValue> COIN_DROP_PERCENT = register("coinDropPercent", GameRules.Category.PLAYER, GameRules.IntegerValue.create(0, keepWithinLimits(0, 100)));
	

	private static GameRules.Type<GameRules.IntegerValue> createInteger(int defaultVal) { return GameRules.IntegerValue.create(defaultVal); }


	private static GameRules.Type<GameRules.IntegerValue> createInteger(int defaultVal, BiConsumer<MinecraftServer, GameRules.IntegerValue> callback) { return GameRules.IntegerValue.create(defaultVal, callback); }
	
	private static <T extends GameRules.Value<T>> GameRules.Key<T> register(String name, GameRules.Category category, GameRules.Type<T> ruleType)
	{
		if(ruleType == null)
			return null;
		GameRules.Key<T> ruleKey = new GameRules.Key<>(name, category);
		GAME_RULES.add(new RuleData<>(name, category, ruleType));
		return ruleKey;
	}
	
	public static <T extends GameRules.Value<T>> T getCustomValue(@Nonnull Level level, @Nullable GameRules.Key<T> ruleKey)
	{
		if(ruleKey == null)
			return null;
		return level.getGameRules().getRule(ruleKey);
	}

	public static boolean safeGetCustomBool(@Nonnull Level level, @Nullable GameRules.Key<GameRules.BooleanValue> ruleKey, boolean defaultValue)
	{
		GameRules.BooleanValue ruleVal = getCustomValue(level, ruleKey);
		if(ruleVal != null)
			return defaultValue;
		return ruleVal.get();
	}

	public static int safeGetCustomInt(@Nonnull Level level, @Nullable GameRules.Key<GameRules.IntegerValue> ruleKey, int defaultValue)
	{
		GameRules.IntegerValue ruleVal = getCustomValue(level, ruleKey);
		if(ruleVal != null)
			return defaultValue;
		return ruleVal.get();
	}
	
	public static void registerRules()
	{
		GAME_RULES.forEach(rule -> GameRules.register(rule.name, rule.category, rule.ruleType));
		GAME_RULES.clear();
	}

	private record RuleData<T extends GameRules.Value<T>>(String name, GameRules.Category category, GameRules.Type<T> ruleType) { }

	public static BiConsumer<MinecraftServer,GameRules.IntegerValue> keepWithinLimits(int lowerLimit, int upperLimit)
	{
		return (s,v) -> {
			int oldValue = v.get();
			int newValue = MathUtil.clamp(oldValue, lowerLimit, upperLimit);
			if(oldValue != newValue)
				v.set(newValue, null);
		};
	}


}
