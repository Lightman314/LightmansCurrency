package io.github.lightman314.lightmanscurrency.api.variants.block.block_entity;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.capability.variant.CapabilityVariantData;
import io.github.lightman314.lightmanscurrency.api.variants.block.builtin.VariantChunkDataStorageAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

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
        //If a block entity is present, check the "normal" way that prioritizes the block entity cap
        if(be != null)
            return get(be);
        //Even if the block entity is null, still attempt to get the data from a capability
        if(level instanceof Level l)
        {
            //Try to get the cap from the chunk
            AtomicReference<IVariantDataStorage> result = new AtomicReference<>(null);
            LevelChunk chunk = l.getChunkAt(pos);
            LazyOptional<VariantChunkDataStorageAttachment> optional2 = chunk.getCapability(VariantChunkDataStorageAttachment.CAP);
            optional2.ifPresent(data -> result.set(data.getData(CapabilityVariantData.getBlockCorner(level.getBlockState(pos),pos))));
            return result.get();
        }
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
        //See if the block entity has a capability
        AtomicReference<IVariantDataStorage> result = new AtomicReference<>(null);
        LazyOptional<IVariantDataStorage> optional = be.getCapability(CapabilityVariantData.CAPABILITY);
        if(optional.isPresent())
            optional.ifPresent(result::set);
        else
        {
            //Try to get the cap from the chunk
            LevelChunk chunk = be.getLevel().getChunkAt(be.getBlockPos());
            LazyOptional<VariantChunkDataStorageAttachment> optional2 = chunk.getCapability(VariantChunkDataStorageAttachment.CAP);
            optional2.ifPresent(data -> result.set(data.getData(be.getBlockPos())));
        }
        //Get from the capability from the chunk instead
        return result.get();
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