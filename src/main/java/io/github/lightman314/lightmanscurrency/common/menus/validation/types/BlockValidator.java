package io.github.lightman314.lightmanscurrency.common.menus.validation.types;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidatorType;
import io.github.lightman314.lightmanscurrency.common.util.TagUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

public class BlockValidator extends MenuValidator {

    public static final MenuValidatorType TYPE = new Type();

    private final BlockPos pos;
    private final Block block;
    protected BlockValidator(BlockPos pos, Block block) { super(TYPE); this.pos = pos; this.block = block; }

    public static MenuValidator of(BlockPos pos, Block block) { return new BlockValidator(pos, block); }

    @Override
    protected void encodeAdditional(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.pos);
        buffer.writeUtf(BuiltInRegistries.BLOCK.getKey(this.block).toString());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("Position", TagUtil.saveBlockPos(this.pos));
        tag.putString("Block", BuiltInRegistries.BLOCK.getKey(this.block).toString());
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockState(this.pos).is(this.block) &&
                player.distanceToSqr((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64d;
    }

    private static final class Type extends MenuValidatorType
    {
        private Type() { super(LightmansCurrency.id("block")); }
        @Override
        public MenuValidator decode(FriendlyByteBuf buffer) { return of(buffer.readBlockPos(), BuiltInRegistries.BLOCK.get(ResourceLocation.parse(buffer.readUtf()))); }
        @Override
        public MenuValidator load(CompoundTag tag) { return of(TagUtil.loadBlockPos(tag.getCompound("Position")), BuiltInRegistries.BLOCK.get(ResourceLocation.parse(tag.getString("Block")))); }
    }

}
