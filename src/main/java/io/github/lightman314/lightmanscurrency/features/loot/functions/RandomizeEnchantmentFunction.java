package io.github.lightman314.lightmanscurrency.features.loot.functions;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public record RandomizeEnchantmentFunction(HolderSet<Enchantment> holderSet) implements LootItemFunction {

    public static final MapCodec<RandomizeEnchantmentFunction> MAP_CODEC = RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).fieldOf("enchantments").xmap(RandomizeEnchantmentFunction::new,RandomizeEnchantmentFunction::holderSet);

    @Override
    public MapCodec<? extends LootItemFunction> codec() { return MAP_CODEC; }

    @Override
    public ItemStack apply(ItemStack stack,LootContext context) {
        this.holderSet.getRandomElement(context.getRandom())
                .ifPresent(e -> EnchantmentHelper.updateEnchantments(stack,
                        enchantments -> enchantments.upgrade(e,1)));
        return stack;
    }
}
