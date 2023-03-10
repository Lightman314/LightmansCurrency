package io.github.lightman314.lightmanscurrency.common.enchantments;

import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import net.minecraft.enchantment.EnchantmentType;

public class LCEnchantmentCategories {
	
	public static final EnchantmentType WALLET_CATEGORY = EnchantmentType.create("WALLET",
			item -> item instanceof WalletItem);
	
	public static final EnchantmentType WALLET_PICKUP_CATEGORY = EnchantmentType.create("WALLET_PICKUP",
			item -> item instanceof WalletItem && WalletItem.CanPickup((WalletItem)item)
		);
	
}
