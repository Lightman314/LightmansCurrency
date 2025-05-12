package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.enchantments.CoinMagnetEnchantment;
import io.github.lightman314.lightmanscurrency.common.enchantments.MoneyMendingEnchantment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment.Rarity;

import java.util.function.Supplier;

public class ModEnchantments {
	
	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	static {
		
		MONEY_MENDING = ModRegistries.ENCHANTMENTS.register("money_mending", () -> new MoneyMendingEnchantment(Rarity.RARE, EquipmentSlot.values()));
		
		COIN_MAGNET = ModRegistries.ENCHANTMENTS.register("coin_magnet", () -> new CoinMagnetEnchantment(Rarity.COMMON, EquipmentSlot.values()));
		
	}
	
	public static final Supplier<MoneyMendingEnchantment> MONEY_MENDING;
	public static final Supplier<CoinMagnetEnchantment> COIN_MAGNET;
	
}
