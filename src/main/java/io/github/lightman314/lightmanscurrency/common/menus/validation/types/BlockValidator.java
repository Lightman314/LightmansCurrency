package io.github.lightman314.lightmanscurrency.common.menus.validation.types;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidatorType;
import io.github.lightman314.lightmanscurrency.common.util.TagUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

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
        buffer.writeUtf(ForgeRegistries.BLOCKS.getKey(this.block).toString());
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        tag.put("Position", TagUtil.saveBlockPos(this.pos));
        tag.putString("Block", ForgeRegistries.BLOCKS.getKey(this.block).toString());
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return player.level().getBlockState(this.pos).is(this.block) &&
                player.distanceToSqr((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64d;
    }

    private static final class Type extends MenuValidatorType
    {
        private Type() { super(new ResourceLocation(LightmansCurrency.MODID, "block")); }
        @Nonnull
        @Override
        public MenuValidator decode(@Nonnull FriendlyByteBuf buffer) { return of(buffer.readBlockPos(), ForgeRegistries.BLOCKS.getValue(new ResourceLocation(buffer.readUtf()))); }
        @Nonnull
        @Override
        public MenuValidator load(@Nonnull CompoundTag tag) { return of(TagUtil.loadBlockPos(tag.getCompound("Position")), ForgeRegistries.BLOCKS.getValue(new ResourceLocation(tag.getString("Block")))); }
    }

}
