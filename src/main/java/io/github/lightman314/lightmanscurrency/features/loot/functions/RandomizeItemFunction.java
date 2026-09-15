package io.github.lightman314.lightmanscurrency.features.loot.functions;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.Optional;

public record RandomizeItemFunction(HolderSet<Item> holderSet) implements LootItemFunction {

    public static final MapCodec<RandomizeItemFunction> MAP_CODEC = RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items").xmap(RandomizeItemFunction::new,RandomizeItemFunction::holderSet);

    @Override
    public MapCodec<? extends LootItemFunction> codec() { return MAP_CODEC; }
    @Override
    public ItemStack apply(ItemStack stack, LootContext context) {
        Optional<Holder<Item>> random = this.holderSet.getRandomElement(context.getRandom());
        if(random.isPresent())
            return stack.transmuteCopy(random.get().value());
        return stack;
    }

}