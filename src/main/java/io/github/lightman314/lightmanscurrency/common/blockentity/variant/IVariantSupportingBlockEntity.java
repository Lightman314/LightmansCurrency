package io.github.lightman314.lightmanscurrency.common.blockentity.variant;

import io.github.lightman314.lightmanscurrency.api.variants.block.IVariantBlock;
import io.github.lightman314.lightmanscurrency.api.variants.block.block_entity.IVariantDataStorage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

@Deprecated(since = "2.3.0.2",forRemoval = true)
public interface IVariantSupportingBlockEntity extends IVariantDataStorage {

    @Deprecated(since = "2.3.0.2",forRemoval = true)
    static void copyDataToItem(IVariantSupportingBlockEntity be, ItemStack item) { copyDataToItem(be.getCurrentVariant(),be.isVariantLocked(),item); }
    @Deprecated(since = "2.3.0.2",forRemoval = true)
    static void copyDataToItem(@Nullable ResourceLocation variant, boolean variantLocked, ItemStack item)
    {
        IVariantBlock.copyDataToItem(variant,variantLocked,item);
    }

}