package io.github.lightman314.lightmanscurrency.api.world.blockentity;

import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.IRegistryAccess;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import javax.annotation.Nullable;

public class EasyBlockEntity extends BlockEntity implements ISidedContext, IRegistryAccess {

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

    protected void sendUpdate() {
        if(this.level != null)
            this.level.sendBlockUpdated(this.getBlockPos(),this.getBlockState(),this.getBlockState(),Block.UPDATE_CLIENTS);
    }

}