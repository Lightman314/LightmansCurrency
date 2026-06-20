package io.github.lightman314.lightmanscurrency.api.world.menu.validation.builtin;

import io.github.lightman314.lightmanscurrency.api.world.menu.validation.MenuValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class BlockValidator implements MenuValidator {

    public static final StreamCodec<RegistryFriendlyByteBuf,BlockValidator> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.BLOCK),v -> v.block,
            BlockPos.STREAM_CODEC,v -> v.pos,
            BlockValidator::new);

    private final Block block;
    private final BlockPos pos;
    private final float range;
    public BlockValidator(Block block,BlockPos pos) { this(block,pos,4f); }
    public BlockValidator(Block block,BlockPos pos,float range)
    {
        this.block = block;
        this.pos = pos.immutable();
        this.range = range;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf,BlockValidator> getType() { return STREAM_CODEC; }

    @Override
    public boolean stillValid(Player player) {
        Level level = player.level();
        return level.isLoaded(this.pos) && level.getBlockState(this.pos).getBlock() == this.block && player.isWithinBlockInteractionRange(this.pos,this.range);
    }
}
