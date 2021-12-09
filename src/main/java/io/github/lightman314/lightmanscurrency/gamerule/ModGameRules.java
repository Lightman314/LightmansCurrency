package io.github.lightman314.lightmanscurrency.gamerule;

import java.lang.reflect.Method;
import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ModGameRules {

	private static final List<RuleData<?>> GAME_RULES = Lists.newArrayList();
	
	public static final GameRules.RuleKey<GameRules.BooleanValue> KEEP_WALLET = register("keepWallet", GameRules.Category.PLAYER, createBoolean(false));
	public static final GameRules.RuleKey<GameRules.IntegerValue> COIN_DROP_PERCENT = register("coinDropPercent", GameRules.Category.PLAYER, createInteger(0));
	
	
	@SuppressWarnings("unchecked")
	private static GameRules.RuleType<GameRules.IntegerValue> createInteger(int defaultVal)
	{
		try {
			Method m = ObfuscationReflectionHelper.findMethod(GameRules.IntegerValue.class, "func_223562_a", int.class);
			m.setAccessible(true);
			return (GameRules.RuleType<GameRules.IntegerValue>) m.invoke(null, defaultVal);
		} catch(Exception e) {
			try {
				Method m2 = ObfuscationReflectionHelper.findMethod(GameRules.IntegerValue.class, "create", int.class);
				m2.setAccessible(true);
				return (GameRules.RuleType<GameRules.IntegerValue>) m2.invoke(null, defaultVal);
			} catch(Exception e2) {
				LightmansCurrency.LogError("Create gamerule error", e);
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static GameRules.RuleType<GameRules.BooleanValue> createBoolean(boolean defaultVal)
	{
		try {
			Method m = ObfuscationReflectionHelper.findMethod(GameRules.BooleanValue.class, "func_223571_a", boolean.class);
			m.setAccessible(true);
			return (GameRules.RuleType<GameRules.BooleanValue>) m.invoke(null, defaultVal);
		}
		catch(Exception e) {
			try {
				Method m2 = ObfuscationReflectionHelper.findMethod(GameRules.BooleanValue.class, "create", boolean.class);
				m2.setAccessible(true);
				return (GameRules.RuleType<GameRules.BooleanValue>) m2.invoke(null, defaultVal);
			} catch(Exception e2) {
				LightmansCurrency.LogError("Create gamerule error", e);
			}
		}
		return null;
	}
	
	private static <T extends GameRules.RuleValue<T>> GameRules.RuleKey<T> register(String name, GameRules.Category category, GameRules.RuleType<T> ruleType)
	{
		if(ruleType == null)
			return null;
		GameRules.RuleKey<T> ruleKey = new GameRules.RuleKey<>(name, category);
		RuleData<T> ruleData = new RuleData<T>(name, category, ruleType);
		GAME_RULES.add(ruleData);
		return ruleKey;
	}
	
	public static <T extends GameRules.RuleValue<T>> T getCustomValue(World world, GameRules.RuleKey<T> ruleKey)
	{
		if(ruleKey == null)
			return null;
		return world.getGameRules().get(ruleKey);
	}
	
	public static void registerRules()
	{
		GAME_RULES.forEach(rule -> GameRules.register(rule.name, rule.category, rule.ruleType));
		GAME_RULES.clear();
	}
	
	private static class RuleData<T extends GameRules.RuleValue<T>>
	{
		public final String name;
		public final GameRules.Category category;
		public final GameRules.RuleType<T> ruleType;
		
		public RuleData(String name, GameRules.Category category, GameRules.RuleType<T> ruleType)
		{
			this.name = name;
			this.category = category;
			this.ruleType = ruleType;
		}
		
	}
	
	
}
