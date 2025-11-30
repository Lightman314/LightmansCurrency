package io.github.lightman314.lightmanscurrency.common.loot.functions;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.variants.block.IVariantBlock;
import io.github.lightman314.lightmanscurrency.api.variants.block.block_entity.IVariantDataStorage;
import io.github.lightman314.lightmanscurrency.common.core.ModLootFunctionTypes;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ModelVariantLootFunction implements LootItemFunction {

    public static final ModelVariantLootFunction INSTANCE = new ModelVariantLootFunction();
    public static final MapCodec<ModelVariantLootFunction> CODEC = MapCodec.unit(INSTANCE);

    private ModelVariantLootFunction() {}

    @Override
    public LootItemFunctionType<? extends LootItemFunction> getType() { return ModLootFunctionTypes.MODEL_VARIANT.get(); }

    @Override
    public ItemStack apply(ItemStack stack, LootContext context) {
        IVariantBlock.copyDataToItem(IVariantDataStorage.get(context),stack);
        return stack;
    }

    public static Builder builder() { return () -> INSTANCE; }

}
