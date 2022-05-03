package io.github.lightman314.lightmanscurrency.core;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.enchantments.CoinMagnetEnchantment;
import io.github.lightman314.lightmanscurrency.enchantments.MoneyMendingEnchantment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantment.Rarity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModEnchantments {

	private static final List<Enchantment> ENCHANTMENTS = new ArrayList<>();
	
	public static final MoneyMendingEnchantment MONEY_MENDING = register("money_mending", new MoneyMendingEnchantment(Rarity.RARE, EquipmentSlot.values()));
	
	public static final CoinMagnetEnchantment COIN_MAGNET = register("coin_magnet", new CoinMagnetEnchantment(Rarity.COMMON, EquipmentSlot.values()));
	
	private static <T extends Enchantment> T register(String name, T enchantment) {
		enchantment.setRegistryName(name);
		ENCHANTMENTS.add(enchantment);
		return enchantment;
	}
	
	@SubscribeEvent
	public static void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
		ENCHANTMENTS.forEach(enchantment -> event.getRegistry().register(enchantment));
		ENCHANTMENTS.clear();
	}
	
}
