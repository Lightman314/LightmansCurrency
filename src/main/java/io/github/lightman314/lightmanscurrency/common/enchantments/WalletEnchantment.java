package io.github.lightman314.lightmanscurrency.common.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public abstract class WalletEnchantment extends Enchantment {

	protected WalletEnchantment(Rarity rarity, EnchantmentType cat, EquipmentSlotType[] slot) { super(rarity, cat, slot); }

	public abstract void addWalletTooltips(List<ITextComponent> tooltips, int enchantLevel, ItemStack item);
	
	public static void addWalletEnchantmentTooltips(List<ITextComponent> tooltip, ItemStack item) {
		EnchantmentHelper.getEnchantments(item).forEach((e, l) -> {
			if(e instanceof WalletEnchantment && l > 0)
				((WalletEnchantment)e).addWalletTooltips(tooltip, l, item);
		});
	}
	
}
