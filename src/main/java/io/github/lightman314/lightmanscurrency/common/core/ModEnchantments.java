package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.enchantments.CoinMagnetEnchantment;
import io.github.lightman314.lightmanscurrency.common.enchantments.MoneyMendingEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraftforge.fml.RegistryObject;

public class ModEnchantments {
	
	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	static {
		
		MONEY_MENDING = ModRegistries.ENCHANTMENTS.register("money_mending", () -> new MoneyMendingEnchantment(Enchantment.Rarity.RARE, EquipmentSlotType.values()));
		
		COIN_MAGNET = ModRegistries.ENCHANTMENTS.register("coin_magnet", () -> new CoinMagnetEnchantment(Enchantment.Rarity.COMMON, EquipmentSlotType.values()));
		
	}
	
	public static final RegistryObject<MoneyMendingEnchantment> MONEY_MENDING;
	public static final RegistryObject<CoinMagnetEnchantment> COIN_MAGNET;
	
}
