package io.github.lightman314.lightmanscurrency.api.capability.variant;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IDeepBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.ITallBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IWideBlock;
import io.github.lightman314.lightmanscurrency.api.variants.block.block_entity.IVariantDataStorage;
import io.github.lightman314.lightmanscurrency.api.variants.block.builtin.VariantChunkDataStorageAttachment;
import io.github.lightman314.lightmanscurrency.api.variants.block.builtin.VariantDataStorageAttachment;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

@Mod.EventBusSubscriber
public class CapabilityVariantData {

    //TODO finish 1.20.1 exclusive one

    public static final Capability<IVariantDataStorage> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    private static final Set<BlockEntityType<?>> AUTO_BLOCK_ENTITIES = new ObjectArraySet<>();
    private static final Map<ResourceLocation,BiFunction<BlockState,BlockPos,BlockPos>> AUTO_BLOCKS = new HashMap<>();

    /**
     * Registers a built-in method of attaching and obtaining an `IVariantDataStorage` instance with a {@link VariantDataStorageAttachment}<br>
     * Since data attachments can only be applied to block entities, alternative methods will be needed if your block does not have one
     * @param types A selection of {@link BlockEntityType}(s) that this capability should be implemented for
     * @see CapabilityVariantData#registerNormalBlock(Block...)
     * @see CapabilityVariantData#registerMultiBlock(BiFunction, Block...)
     */
    public static void registerBlockEntity(BlockEntityType<?>... types)
    {
        AUTO_BLOCK_ENTITIES.addAll(ImmutableList.copyOf(types));
    }

    /**
     * Registeres a built-in method of attaching and obtaining an `IVariantDataStorage` instance that uses a {@link VariantChunkDataStorageAttachment} to store the data without a block entity.<br>
     * This version will not function properly for multi-block structures, please use {@link #registerMultiBlock(BiFunction, Block...)} to register a structure composed of multiple blocks (such as beds, doors, etc.)
     * @param blocks The blocks that you wish to register the built-in external data holder for
     */
    public static void registerNormalBlock(Block... blocks) { registerMultiBlock((s, p) -> p,blocks); }

    /**
     * Registeres a built-in method of attaching and obtaining an `IVariantDataStorage` instance that uses a {@link VariantChunkDataStorageAttachment} to store the data without a block entity.<br>
     * Assumes that your multi-block structure implements {@link ITallBlock}, {@link IWideBlock}, or {@link IDeepBlock}
     * @param blocks The blocks that you wish to register the built-in external data holder for
     */
    public static void registerLCMultiBlock(Block... blocks)
    {
        registerMultiBlock((state,pos) -> {
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
     * @param getMultiblockCorner A Function that takes the position and block state, and then calculates the corner of the multi-block structure that you wish to actually store the data in
     * @param blocks The blocks that you wish to register the built-in external data holder for
     */
    public static void registerMultiBlock(BiFunction<BlockState, BlockPos,BlockPos> getMultiblockCorner, Block... blocks)
    {
        for(Block b : blocks)
            AUTO_BLOCKS.put(ForgeRegistries.BLOCKS.getKey(b),getMultiblockCorner);
    }

    @ApiStatus.Internal
    @Nullable
    public static BlockPos getBlockCorner(BlockState state,BlockPos pos)
    {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if(AUTO_BLOCKS.containsKey(id))
            return AUTO_BLOCKS.get(id).apply(state,pos);
        return null;
    }

    @ApiStatus.Internal
    @SubscribeEvent
    public static void onBlockEntityCaps(AttachCapabilitiesEvent<BlockEntity> event)
    {
        if(AUTO_BLOCK_ENTITIES.contains(event.getObject().getType()))
            event.addCapability(VersionUtil.lcResource("variant_data"),new VariantDataStorageAttachment(event.getObject()));
    }

    @ApiStatus.Internal
    @SubscribeEvent
    public static void onChunkCaps(AttachCapabilitiesEvent<LevelChunk> event)
    {
        event.addCapability(VersionUtil.lcResource("variant_chunk_data"),new VariantChunkDataStorageAttachment(event.getObject()));
    }

    @ApiStatus.Internal
    @SubscribeEvent
    public static void onChunkLoad(ChunkWatchEvent.Watch event)
    {
        //Send sync packet to newly watching players
        event.getChunk().getBlockEntities().forEach((pos,be) ->
            be.getCapability(CAPABILITY).ifPresent(data -> {
                if(data instanceof VariantDataStorageAttachment attachment)
                    attachment.syncWith(event.getPlayer());
            }));
        event.getChunk().getCapability(VariantChunkDataStorageAttachment.CAP).ifPresent(data ->
            data.syncWith(event.getPlayer()));
    }

}
