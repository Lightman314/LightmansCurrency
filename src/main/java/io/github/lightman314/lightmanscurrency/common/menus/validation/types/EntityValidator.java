package io.github.lightman314.lightmanscurrency.common.menus.validation.types;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidatorType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

public class EntityValidator extends MenuValidator {

    public static final MenuValidatorType TYPE = new Type();

    private int id = -1;
    private Entity entity;
    private int safeGetID() { return this.entity == null ? this.id : this.entity.getId(); }
    private void validateEntity(@Nonnull Player player)
    {
        if(this.id >= 0 && this.entity == null)
        {
            this.entity = player.level().getEntity(this.id);
            this.id = -1;
        }
    }

    private EntityValidator(int entityID) { super(TYPE); this.id = entityID;}
    private EntityValidator(@Nonnull Entity entity) { super(TYPE); this.entity = entity; }

    @Nonnull
    public static EntityValidator of(@Nonnull Entity entity) { return new EntityValidator(entity); }

    @Override
    protected void encodeAdditional(@Nonnull FriendlyByteBuf buffer) { buffer.writeInt(this.safeGetID()); }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) { tag.putInt("EntityID", this.safeGetID()); }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        this.validateEntity(player);
        return this.entity != null && canInteractWithEntity(player,this.entity);
    }

    private static boolean canInteractWithEntity(@Nonnull Player player, @Nonnull Entity entity) { return player.distanceToSqr(entity) <= 8 * 8; }


    private static class Type extends MenuValidatorType
    {
        protected Type() { super(new ResourceLocation(LightmansCurrency.MODID, "entity")); }
        @Nonnull
        @Override
        public MenuValidator decode(@Nonnull FriendlyByteBuf buffer) { return new EntityValidator(buffer.readInt()); }
        @Nonnull
        @Override
        public MenuValidator load(@Nonnull CompoundTag tag) { return new EntityValidator(tag.getInt("EntityID")); }
    }

}