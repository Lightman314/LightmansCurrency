package io.github.lightman314.lightmanscurrency.common.enchantments;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.common.core.ModEnchantments;
import io.github.lightman314.lightmanscurrency.common.util.LookupHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import javax.annotation.Nonnull;

public abstract class WalletEnchantment {

	private static final List<Pair<ResourceKey<Enchantment>,IWalletTooltipProvider>> TOOLTIP_PROVIDERS;
	public static void registerWalletTooltipProvider(@Nonnull ResourceKey<Enchantment> enchantment, @Nonnull IWalletTooltipProvider provider)
	{
		TOOLTIP_PROVIDERS.add(Pair.of(enchantment,provider));
	}

	static {
		TOOLTIP_PROVIDERS = new ArrayList<>();
		registerWalletTooltipProvider(ModEnchantments.COIN_MAGNET, CoinMagnetEnchantment::addWalletTooltips);
	}

	public interface IWalletTooltipProvider {
		void addWalletTooltips(List<Component> tooltips, int enchantLevel, ItemStack item);
	}

	public static void addWalletEnchantmentTooltips(@Nonnull List<Component> tooltip, @Nonnull ItemStack item, @Nonnull Item.TooltipContext context) {
		if(context.registries() == null)
			return;
		context.registries().lookup(Registries.ENCHANTMENT).ifPresent(enchantmentRegistry -> {
			ItemEnchantments enchantments = item.getAllEnchantments(enchantmentRegistry);
			for(Pair<ResourceKey<Enchantment>,IWalletTooltipProvider> pair : TOOLTIP_PROVIDERS)
			{
				Holder<Enchantment> holder = LookupHelper.lookupEnchantment(context.registries(), pair.getFirst());
				if(holder != null)
				{
					int level = enchantments.getLevel(holder);
					if(level > 0)
						pair.getSecond().addWalletTooltips(tooltip, level, item);
				}
			}
		});
	}



	
}
