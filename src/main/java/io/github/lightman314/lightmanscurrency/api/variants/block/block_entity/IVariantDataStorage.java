package io.github.lightman314.lightmanscurrency.api.variants.block.block_entity;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.capability.variant.CapabilityVariantData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.common.extensions.ILevelExtension;

import javax.annotation.Nullable;

/**
 * An interface for accessing the variant id and locked status of a block in the world<br>
 * Unfortunately cannot be a capability in 1.21+ as the loot function does not have a level/world parameter from which to obtain the capability<br>
 * Therefore the two primary options are to
 */
public interface IVariantDataStorage {

    @Nullable
    ResourceLocation getCurrentVariant();
    boolean isVariantLocked();

    default void setVariant(@Nullable ResourceLocation variant) { this.setVariant(variant,this.isVariantLocked()); }
    void setVariant(@Nullable ResourceLocation variant, boolean locked);

    @Nullable
    static IVariantDataStorage get(BlockGetter level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        //If a block entity is present, check the "normal" way that prioritizes
        if(be != null)
            return get(be);
        //Even if the block entity is null, still attempt to get the data from a capability
        if(level instanceof ILevelExtension l)
            return l.getCapability(CapabilityVariantData.CAPABILITY,pos);
        else
            LightmansCurrency.LogWarning("Unable to obtain the IVariantDataStorage capability as the level " + level.getClass().getName() + " is not a proper level!");
        return null;
    }
    @Nullable
    @Deprecated
    static IVariantDataStorage get(@Nullable BlockEntity be)
    {
        if(be == null)
            return null;
        //Get from the block entity first
        if(be instanceof IVariantDataStorage result)
            return result;
        if(!be.hasLevel())
            return null;
        //Get from the capability if the block entity does not inherently contain it
        return be.getLevel().getCapability(CapabilityVariantData.CAPABILITY,be.getBlockPos(),null,be);
    }
    @Nullable
    static IVariantDataStorage get(LootContext context)
    {
        //Try and obtain from the data from the block entity parameter
        if(context.hasParam(LootContextParams.BLOCK_ENTITY))
            return get(context.getParam(LootContextParams.BLOCK_ENTITY));
        //Otherwise try and obtain it from the level and block position
        else if(context.hasParam(LootContextParams.ORIGIN))
        {
            BlockPos pos = BlockPos.containing(context.getParam(LootContextParams.ORIGIN));
            return get(context.getLevel(),pos);
        }
        return null;
    }

}
