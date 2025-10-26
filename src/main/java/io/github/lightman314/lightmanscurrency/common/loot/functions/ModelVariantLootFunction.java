package io.github.lightman314.lightmanscurrency.common.loot.functions;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.common.blockentity.variant.IVariantSupportingBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModLootFunctionTypes;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

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
    public ItemStack apply(ItemStack stack, LootContext lootContext) {
        if(lootContext.hasParam(LootContextParams.BLOCK_ENTITY) && lootContext.getParam(LootContextParams.BLOCK_ENTITY) instanceof IVariantSupportingBlockEntity be)
            IVariantSupportingBlockEntity.copyDataToItem(be,stack);
        return stack;
    }

    public static Builder builder() { return () -> INSTANCE; }

}
