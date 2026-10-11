package io.github.lightman314.lightmanscurrency.api.world.blockentity;

import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.IRegistryAccess;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.network.message.block_entity.CPacketRequestBlockEntityPacket;
import io.github.lightman314.lightmanscurrency.network.message.block_entity.SPacketSyncBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

public abstract class EasyBlockEntity extends BlockEntity implements ISidedContext, IRegistryAccess {

    public EasyBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    @Override
    public final boolean isClient() { return this.level == null || this.level.isClientSide(); }
    @Override
    public final HolderLookup.Provider registryAccess() { return this.level.registryAccess(); }

    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return this.saveWithoutMetadata(registries); }
    @Override
    public void handleUpdateTag(ValueInput input) { this.loadAdditional(input); }

    public abstract static class Synced extends EasyBlockEntity {

        public Synced(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) { super(type, worldPosition, blockState); }

        @Override
        @OverridingMethodsMustInvokeSuper
        public void onLoad() {
            super.onLoad();
            if(this.isClient())
                new CPacketRequestBlockEntityPacket(this.getBlockPos()).send();
        }

        public abstract void writeInitialPacket(FancyPacketMap.Mutable packet, Player player);

        public abstract void handleSyncPacket(FancyPacketMap packet);

        protected final void sendPacket(FancyPacketMap packet) {
            if(this.level instanceof ServerLevel sl)
                new SPacketSyncBlockEntity(this.worldPosition,packet).sendToPlayersTrackingChunk(sl,ChunkPos.containing(this.worldPosition));
        }
    }

}