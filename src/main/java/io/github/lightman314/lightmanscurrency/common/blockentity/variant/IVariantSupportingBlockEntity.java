package io.github.lightman314.lightmanscurrency.common.blockentity.variant;

import io.github.lightman314.lightmanscurrency.api.variants.block.block_entity.IVariantDataStorage;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

@Deprecated(since = "2.3.0.2",forRemoval = true)
public interface IVariantSupportingBlockEntity extends IVariantDataStorage {

    @Deprecated(since = "2.3.0.2",forRemoval = true)
    static void copyDataToItem(IVariantSupportingBlockEntity be, ItemStack item) { copyDataToItem(be.getCurrentVariant(),be.isVariantLocked(),item); }
    @Deprecated(since = "2.3.0.2",forRemoval = true)
    static void copyDataToItem(@Nullable ResourceLocation variant, boolean variantLocked, ItemStack item)
    {
        if(variant != null)
            item.set(ModDataComponents.MODEL_VARIANT,variant);
        if(variantLocked)
            item.set(ModDataComponents.VARIANT_LOCK,Unit.INSTANCE);
    }

}
