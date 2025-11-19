package io.github.lightman314.lightmanscurrency.common.blockentity.variant;

import io.github.lightman314.lightmanscurrency.api.variants.item.IVariantItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IVariantSupportingBlockEntity {

    @Nonnull
    BlockState getBlockState();

    @Nullable
    Level getLevel();

    @Nonnull
    BlockPos getBlockPos();

    @Nullable
    ResourceLocation getCurrentVariant();

    default void setVariant(@Nullable ResourceLocation variant) { this.setVariant(variant,this.isVariantLocked()); }
    void setVariant(@Nullable ResourceLocation variant, boolean locked);

    boolean isVariantLocked();

    static void copyDataToItem(IVariantSupportingBlockEntity be, ItemStack item) { copyDataToItem(be.getCurrentVariant(),be.isVariantLocked(),item); }
    static void copyDataToItem(@Nullable ResourceLocation variant, boolean variantLocked, ItemStack item)
    {
        if(variant != null)
            IVariantItem.setItemVariant(item,variant);
        if(variantLocked)
            IVariantItem.setLocked(item,variantLocked);
    }

}