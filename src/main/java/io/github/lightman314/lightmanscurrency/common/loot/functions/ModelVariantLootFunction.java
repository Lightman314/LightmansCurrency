package io.github.lightman314.lightmanscurrency.common.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.github.lightman314.lightmanscurrency.common.blockentity.variant.IVariantSupportingBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModLootFunctionTypes;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ModelVariantLootFunction implements LootItemFunction {

    public static final ModelVariantLootFunction INSTANCE = new ModelVariantLootFunction();
    public static final Serializer<ModelVariantLootFunction> CODEC = new Serializer<>() {
        @Override
        public void serialize(JsonObject jsonObject, ModelVariantLootFunction modelVariantLootFunction, JsonSerializationContext jsonSerializationContext) {}
        @Override
        public ModelVariantLootFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) { return INSTANCE; }
    };

    private ModelVariantLootFunction() {}

    @Override
    public LootItemFunctionType getType() { return ModLootFunctionTypes.MODEL_VARIANT.get(); }

    @Override
    public ItemStack apply(ItemStack stack, LootContext lootContext) {
        Level level = lootContext.getLevel();
        if(lootContext.getParam(LootContextParams.BLOCK_ENTITY) instanceof IVariantSupportingBlockEntity be)
        {
            ResourceLocation variantID = be.getCurrentVariant();
            if(variantID != null)
                IVariantBlock.setItemVariant(stack,variantID);
        }
        return stack;
    }

    public static Builder builder() { return () -> INSTANCE; }

}