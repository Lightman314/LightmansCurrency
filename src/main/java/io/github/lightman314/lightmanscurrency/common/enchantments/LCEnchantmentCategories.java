package io.github.lightman314.lightmanscurrency.common.enchantments;

import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class LCEnchantmentCategories {
	
	public static final EnchantmentCategory WALLET_CATEGORY = EnchantmentCategory.create("WALLET",
			item -> item instanceof WalletItem);
	
	public static final EnchantmentCategory WALLET_PICKUP_CATEGORY = EnchantmentCategory.create("WALLET_PICKUP",
			item -> item instanceof WalletItem walletItem && WalletItem.CanPickup(walletItem)
		);
	
}
