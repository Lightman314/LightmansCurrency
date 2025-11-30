package io.github.lightman314.lightmanscurrency.api.capability.variant;

import io.github.lightman314.lightmanscurrency.api.misc.blocks.IDeepBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.ITallBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IWideBlock;
import io.github.lightman314.lightmanscurrency.api.variants.block.block_entity.IVariantDataStorage;
import io.github.lightman314.lightmanscurrency.api.variants.block.builtin.VariantChunkDataStorageAttachment;
import io.github.lightman314.lightmanscurrency.api.variants.block.builtin.VariantDataStorageAttachment;
import io.github.lightman314.lightmanscurrency.common.core.ModAttachmentTypes;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.function.BiFunction;

public class CapabilityVariantData {

    public static final BlockCapability<IVariantDataStorage,Void> CAPABILITY = BlockCapability.createVoid(VersionUtil.lcResource("variant_data"),IVariantDataStorage.class);

    /**
     * Registers a built-in method of attaching and obtaining an `IVariantDataStorage` instance with a {@link VariantDataStorageAttachment}<br>
     * Since data attachments can only be applied to block entities, alternative methods will be needed if your block does not have one
     * @param event The {@link RegisterCapabilitiesEvent} event needed to register the capability
     * @param types A selection of {@link BlockEntityType}(s) that this capability should be implemented for
     * @see CapabilityVariantData#registerNormalBlock(RegisterCapabilitiesEvent, Block...)
     * @see CapabilityVariantData#registerMultiBlock(RegisterCapabilitiesEvent, BiFunction, Block...)
     */
    public static void registerBlockEntity(RegisterCapabilitiesEvent event, BlockEntityType<?>... types)
    {
        for(BlockEntityType<?> type : types)
            event.registerBlockEntity(CAPABILITY,type,(be,c) -> be.getData(VariantDataStorageAttachment.TYPE));
    }


    /**
     * Registeres a built-in method of attaching and obtaining an `IVariantDataStorage` instance that uses a {@link VariantChunkDataStorageAttachment} to store the data without a block entity.<br>
     * This version will not function properly for multi-block structures, please use {@link #registerMultiBlock(RegisterCapabilitiesEvent, BiFunction, Block...)} to register a structure composed of multiple blocks (such as beds, doors, etc.)
     * @param event The {@link RegisterCapabilitiesEvent} event needed to register the capability
     * @param blocks The blocks that you wish to register the built-in external data holder for
     */
    public static void registerNormalBlock(RegisterCapabilitiesEvent event, Block... blocks) { registerMultiBlock(event,(s,p) -> p,blocks); }

    /**
     * Registeres a built-in method of attaching and obtaining an `IVariantDataStorage` instance that uses a {@link VariantChunkDataStorageAttachment} to store the data without a block entity.<br>
     * Assumes that your multi-block structure implements {@link ITallBlock}, {@link IWideBlock}, or {@link IDeepBlock}
     * @param event The {@link RegisterCapabilitiesEvent} event needed to register the capability
     * @param blocks The blocks that you wish to register the built-in external data holder for
     */
    public static void registerLCMultiBlock(RegisterCapabilitiesEvent event, Block... blocks)
    {
        registerMultiBlock(event,(state,pos) -> {
            Block b = state.getBlock();
            if(b instanceof ITallBlock tall && tall.getIsTop(state))
                pos = tall.getOtherHeight(pos,state);
            if(b instanceof IWideBlock wide && wide.getIsRight(state))
                pos = wide.getOtherSide(pos,state);
            if(b instanceof IDeepBlock deep && deep.getIsBack(state))
                pos = deep.getOtherDepth(pos,state);
            return pos;
        },blocks);
    }
    /**
     * Registeres a built-in method of attaching and obtaining an `IVariantDataStorage` instance that uses a {@link VariantChunkDataStorageAttachment} to store the data without a block entity.<br>
     * @param event The {@link RegisterCapabilitiesEvent} event needed to register the capability
     * @param getMultiblockCorner A Function that takes the position and block state, and then calculates the corner of the multi-block structure that you wish to actually store the data in
     * @param blocks The blocks that you wish to register the built-in external data holder for
     */
    public static void registerMultiBlock(RegisterCapabilitiesEvent event, BiFunction<BlockState, BlockPos,BlockPos> getMultiblockCorner, Block... blocks)
    {
        event.registerBlock(CAPABILITY,(level,pos,state,be,c) -> {
            //Calculate the actual position to check
            pos = getMultiblockCorner.apply(state,pos);
            LevelChunk chunk = level.getChunkAt(pos);
            VariantChunkDataStorageAttachment data = chunk.getData(ModAttachmentTypes.VARIANT_CHUNK_DATA);
            return data.getData(pos);
        },blocks);
    }


}
