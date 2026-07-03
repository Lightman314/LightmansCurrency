package io.github.lightman314.lightmanscurrency.features.wallet.enchantments;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

public final class WalletEnchantments {

    public static final ResourceKey<Enchantment> COIN_MAGNET = ResourceKey.create(Registries.ENCHANTMENT,LCApi.id("coin_magnet"));

}