package io.github.lightman314.lightmanscurrency.common.gamerule;

import java.lang.reflect.Method;
import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class ModGameRules {

	private static final List<RuleData<?>> GAME_RULES = Lists.newArrayList();
	
	public static final GameRules.Key<GameRules.BooleanValue> KEEP_WALLET = register("keepWallet", GameRules.Category.PLAYER, createBoolean(false));
	public static final GameRules.Key<GameRules.IntegerValue> COIN_DROP_PERCENT = register("coinDropPercent", GameRules.Category.PLAYER, createInteger(0));
	
	
	@SuppressWarnings("unchecked")
	private static GameRules.Type<GameRules.IntegerValue> createInteger(int defaultVal)
	{
		try {
			//1.16 obfuscated 'func_223562_a'
			//1.17 & 1.18 obfuscated 'm_46312_'
			Method m = ObfuscationReflectionHelper.findMethod(GameRules.IntegerValue.class, "m_46312_", int.class);
			m.setAccessible(true);
			return (GameRules.Type<GameRules.IntegerValue>) m.invoke(null, defaultVal);
		} catch(Exception e) {
			try {
				Method m2 = ObfuscationReflectionHelper.findMethod(GameRules.IntegerValue.class, "create", int.class);
				m2.setAccessible(true);
				return (GameRules.Type<GameRules.IntegerValue>) m2.invoke(null, defaultVal);
			} catch(Exception e2) {
				LightmansCurrency.LogError("Create gamerule error", e);
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static GameRules.Type<GameRules.BooleanValue> createBoolean(boolean defaultVal)
	{
		try {
			//1.16 obfuscated 'func_223571_a'
			//1.17 & 1.18 obfuscated 'm_46250_'
			Method m = ObfuscationReflectionHelper.findMethod(GameRules.BooleanValue.class, "m_46250_", boolean.class);
			m.setAccessible(true);
			return (GameRules.Type<GameRules.BooleanValue>) m.invoke(null, defaultVal);
		}
		catch(Exception e) {
			try {
				Method m2 = ObfuscationReflectionHelper.findMethod(GameRules.BooleanValue.class, "create", boolean.class);
				m2.setAccessible(true);
				return (GameRules.Type<GameRules.BooleanValue>) m2.invoke(null, defaultVal);
			} catch(Exception e2) {
				LightmansCurrency.LogError("Create gamerule error", e);
			}
		}
		return null;
	}
	
	private static <T extends GameRules.Value<T>> GameRules.Key<T> register(String name, GameRules.Category category, GameRules.Type<T> ruleType)
	{
		if(ruleType == null)
			return null;
		GameRules.Key<T> ruleKey = new GameRules.Key<>(name, category);
		RuleData<T> ruleData = new RuleData<>(name, category, ruleType);
		GAME_RULES.add(ruleData);
		return ruleKey;
	}
	
	public static <T extends GameRules.Value<T>> T getCustomValue(Level level, GameRules.Key<T> ruleKey)
	{
		if(ruleKey == null)
			return null;
		return level.getGameRules().getRule(ruleKey);
	}
	
	public static void registerRules()
	{
		GAME_RULES.forEach(rule -> GameRules.register(rule.name, rule.category, rule.ruleType));
		GAME_RULES.clear();
	}

	private record RuleData<T extends GameRules.Value<T>>(String name, GameRules.Category category, GameRules.Type<T> ruleType) { }
	
	
}
