package io.github.lightman314.lightmanscurrency.features.enchantments;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

public final class LCEnchantments {

    private LCEnchantments() {}

    public static final ResourceKey<Enchantment> MONEY_MENDING = key("money_mending");
    public static final ResourceKey<Enchantment> COIN_MAGNET = key("coin_magnet");

    private static ResourceKey<Enchantment> key(String name) { return ResourceKey.create(Registries.ENCHANTMENT, LCApi.id(name)); }

}