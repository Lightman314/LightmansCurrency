package io.github.lightman314.lightmanscurrency.common.loot.entries;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.common.core.ModLootPoolEntryTypes;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class AncientCoinLoot extends LootPoolSingletonContainer {

    public static final MapCodec<AncientCoinLoot> CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(AncientCoinType.CODEC.fieldOf("coin").forGetter(l -> l.type))
                    .and(singletonFields(builder))
                    .apply(builder,AncientCoinLoot::new)
    );

    private final AncientCoinType type;
    protected AncientCoinLoot(AncientCoinType type, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions)
    {
        super(weight, quality, conditions, functions);
        this.type = type;
    }

    @Override
    protected void createItemStack(@Nonnull Consumer<ItemStack> consumer, @Nonnull LootContext context) { consumer.accept(this.type.asItem()); }

    @Nonnull
    @Override
    public LootPoolEntryType getType() { return ModLootPoolEntryTypes.ANCIENT_COIN_TYPE.get(); }

    public static LootPoolSingletonContainer.Builder<?> ancientCoin(@Nonnull AncientCoinType type) {
        return simpleBuilder((p_79583_, p_79584_, p_79585_, p_79586_) -> new AncientCoinLoot(type, p_79583_, p_79584_, p_79585_, p_79586_));
    }

}
