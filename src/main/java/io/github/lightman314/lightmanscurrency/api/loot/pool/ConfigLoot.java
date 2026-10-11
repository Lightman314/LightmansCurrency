package io.github.lightman314.lightmanscurrency.api.loot.pool;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.loot.ConfigItemTier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.function.Consumer;

public class ConfigLoot extends LootPoolSingletonContainer {

    public static final MapCodec<ConfigLoot> CODEC = RecordCodecBuilder.mapCodec(builder -> builder
            .group(ConfigItemTier.CODEC.fieldOf("tier").forGetter(l -> l.tier))
            .and(singletonFields(builder))
            .apply(builder,ConfigLoot::new));

    private final ConfigItemTier tier;
    protected ConfigLoot(ConfigItemTier tier,int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
        super(weight, quality, conditions, functions);
        this.tier = tier;
    }

    @Override
    public MapCodec<ConfigLoot> codec() { return CODEC; }

    @Override
    protected void createItemStack(Consumer<ItemStack> output,LootContext context) {
        output.accept(new ItemStack(this.tier.getItem()));
    }

    public static LootPoolSingletonContainer.Builder<?> lootTableTier(ConfigItemTier tier) { return simpleBuilder((w,q,c,f) -> new ConfigLoot(tier,w,q,c,f)); }

}
