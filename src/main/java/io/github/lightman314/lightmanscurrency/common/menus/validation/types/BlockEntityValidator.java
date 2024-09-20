package io.github.lightman314.lightmanscurrency.common.menus.validation.types;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidatorType;
import io.github.lightman314.lightmanscurrency.common.util.TagUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockEntityValidator extends MenuValidator {

    public static final MenuValidatorType TYPE = new Type();

    private static final MenuValidator NULL = new BlockEntityValidator((BlockEntity)null);

    private BlockEntity be = null;
    private BlockPos bePos = null ;
    private void validateBE(@Nonnull Player player)
    {
        if(this.bePos != null)
        {
            this.be = player.level().getBlockEntity(this.bePos);
            this.bePos = null;
        }
    }

    protected BlockEntityValidator(BlockEntity be) { super(TYPE); this.be = be; }
    protected BlockEntityValidator(BlockPos pos) { super(TYPE); this.bePos = pos;}

    public static MenuValidator of(@Nullable BlockEntity be) { return be == null ? NULL : new BlockEntityValidator(be); }

    @Override
    protected void encodeAdditional(@Nonnull FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.be != null);
        if(this.be != null)
            buffer.writeBlockPos(this.be.getBlockPos());
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        if(this.be != null)
            tag.put("Position", TagUtil.saveBlockPos(this.be.getBlockPos()));
    }

    @Override
    public boolean stillValid(@Nonnull Player player) { this.validateBE(player); return this.be != null && Container.stillValidBlockEntity(this.be, player); }

    private static class Type extends MenuValidatorType
    {
        protected Type() { super(new ResourceLocation(LightmansCurrency.MODID, "block_entity")); }

        @Nonnull
        @Override
        public MenuValidator decode(@Nonnull FriendlyByteBuf buffer) {
            if(buffer.readBoolean())
                return new BlockEntityValidator(buffer.readBlockPos());
            return NULL;
        }

        @Nonnull
        @Override
        public MenuValidator load(@Nonnull CompoundTag tag) {
            if(tag.contains("Position"))
                return new BlockEntityValidator(TagUtil.loadBlockPos(tag.getCompound("Position")));
            return NULL;
        }
    }
}
