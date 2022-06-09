package io.github.lightman314.lightmanscurrency.enchantments;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public abstract class WalletEnchantment extends Enchantment {

	protected WalletEnchantment(Rarity rarity, EnchantmentCategory cat, EquipmentSlot[] slot) { super(rarity, cat, slot); }

	public abstract void addWalletTooltips(List<Component> tooltips, int enchantLevel, ItemStack item);
	
	public static void addWalletEnchantmentTooltips(List<Component> tooltip, ItemStack item) {
		EnchantmentHelper.getEnchantments(item).forEach((e,l) -> {
			if(e instanceof WalletEnchantment && l > 0)
				((WalletEnchantment)e).addWalletTooltips(tooltip, l, item);
		});
	}
	
}
