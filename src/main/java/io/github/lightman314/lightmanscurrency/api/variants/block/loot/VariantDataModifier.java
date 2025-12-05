package io.github.lightman314.lightmanscurrency.api.variants.block.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.variants.VariantProvider;
import io.github.lightman314.lightmanscurrency.api.variants.block.IVariantBlock;
import io.github.lightman314.lightmanscurrency.api.variants.block.block_entity.IVariantDataStorage;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

/**
 * Loot Modifier used to try and attach `IVariantDataStorage` data from a blocks loot table to the item spawned
 */
public class VariantDataModifier extends LootModifier {

    public static final Codec<VariantDataModifier> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(IGlobalLootModifier.LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(m -> m.conditions))
                    .apply(builder,VariantDataModifier::new));

    public VariantDataModifier(LootItemCondition[] conditions) { super(conditions); }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        IVariantDataStorage data = IVariantDataStorage.get(context);
        //Copy the variant data to the given item(s)
        if(data != null)
        {
            for(ItemStack stack : generatedLoot)
            {
                //Only attach the data to valid variant items (just in case it also drops other items)
                if(VariantProvider.getVariantItem(stack) != null)
                    IVariantBlock.copyDataToItem(data,stack);
            }
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() { return CODEC; }
}