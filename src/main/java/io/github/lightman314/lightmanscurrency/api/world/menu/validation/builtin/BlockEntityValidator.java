package io.github.lightman314.lightmanscurrency.api.world.menu.validation.builtin;

import io.github.lightman314.lightmanscurrency.api.world.menu.validation.MenuValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import javax.annotation.Nullable;

public class BlockEntityValidator implements MenuValidator {

    public static final StreamCodec<RegistryFriendlyByteBuf,BlockEntityValidator> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,BlockEntityValidator::getBEPos,
            ByteBufCodecs.registry(Registries.BLOCK_ENTITY_TYPE),BlockEntityValidator::getBEType,
            BlockEntityValidator::new);

    @Nullable
    private BlockEntity be = null;
    @Nullable
    PendingBlockEntity pending = null;

    @Nullable
    private BlockEntity getBlockEntity(Player player) {
        if(this.pending != null)
        {
            Level level = player.level();
            BlockEntity be = level.getBlockEntity(this.pending.pos);
            if(be != null && be.getType() == this.pending.type)
                this.be = be;
            this.pending = null;
        }
        return this.be;
    }
    private BlockPos getBEPos() { return this.be == null ? (this.pending == null ? BlockPos.ZERO : this.pending.pos) : this.be.getBlockPos(); }
    private BlockEntityType<?> getBEType() { return this.be == null ? (this.pending == null ? BlockEntityType.CHEST : this.pending.type) : this.be.getType(); }

    public BlockEntityValidator(BlockEntity be) { this.be = be; }
    private BlockEntityValidator(BlockPos pos,BlockEntityType<?> type) { this.pending = new PendingBlockEntity(pos,type); }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf,BlockEntityValidator> getType() { return STREAM_CODEC; }

    @Override
    public boolean stillValid(Player player) {
        BlockEntity be = this.getBlockEntity(player);
        return be != null && !be.isRemoved();
    }

    private record PendingBlockEntity(BlockPos pos, BlockEntityType<?> type) {}

}