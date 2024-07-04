package io.github.lightman314.lightmanscurrency.common.loot.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.common.core.ModLootPoolEntryTypes;
import io.github.lightman314.lightmanscurrency.common.loot.ConfigItemTier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class ConfigLoot extends LootPoolSingletonContainer {

    public static final MapCodec<ConfigLoot> CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(Codec.INT.fieldOf("tier").forGetter(l -> l.tier.tier))
                    .and(singletonFields(builder))
                    .apply(builder,ConfigLoot::new)
    );

    private final ConfigItemTier tier;

    protected ConfigLoot(int tier, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions)
    {
        this(ConfigItemTier.get(tier), weight,quality,conditions,functions);
    }
    protected ConfigLoot(ConfigItemTier tier, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions)
    {
        super(weight, quality, conditions, functions);
        this.tier = tier;
    }

    @Override
    protected void createItemStack(@Nonnull Consumer<ItemStack> consumer, @Nonnull LootContext context) { consumer.accept(new ItemStack(tier.getItem())); }

    @Nonnull
    @Override
    public LootPoolEntryType getType() { return ModLootPoolEntryTypes.LOOT_TIER_TYPE.get(); }

    public static LootPoolSingletonContainer.Builder<?> lootTableTier(@Nonnull ConfigItemTier tier) {
        return simpleBuilder((p_79583_, p_79584_, p_79585_, p_79586_) -> new ConfigLoot(tier, p_79583_, p_79584_, p_79585_, p_79586_));
    }

}
