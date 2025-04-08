package io.github.lightman314.lightmanscurrency.common.menus.validation.types;

import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidatorType;
import io.github.lightman314.lightmanscurrency.common.util.TagUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;

public class BlockValidator extends MenuValidator {

    public static final MenuValidatorType TYPE = new Type();

    private final BlockPos pos;
    private final Block block;
    protected BlockValidator(@Nonnull BlockPos pos, @Nonnull Block block) { super(TYPE); this.pos = pos; this.block = block; }

    public static MenuValidator of(@Nonnull BlockPos pos, @Nonnull Block block) { return new BlockValidator(pos, block); }

    @Override
    protected void encodeAdditional(@Nonnull FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.pos);
        buffer.writeUtf(BuiltInRegistries.BLOCK.getKey(this.block).toString());
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        tag.put("Position", TagUtil.saveBlockPos(this.pos));
        tag.putString("Block", BuiltInRegistries.BLOCK.getKey(this.block).toString());
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return player.level().getBlockState(this.pos).is(this.block) &&
                player.distanceToSqr((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64d;
    }

    private static final class Type extends MenuValidatorType
    {
        private Type() { super(VersionUtil.lcResource("block")); }
        @Nonnull
        @Override
        public MenuValidator decode(@Nonnull FriendlyByteBuf buffer) { return of(buffer.readBlockPos(), BuiltInRegistries.BLOCK.get(VersionUtil.parseResource(buffer.readUtf()))); }
        @Nonnull
        @Override
        public MenuValidator load(@Nonnull CompoundTag tag) { return of(TagUtil.loadBlockPos(tag.getCompound("Position")), BuiltInRegistries.BLOCK.get(VersionUtil.parseResource(tag.getString("Block")))); }
    }

}
