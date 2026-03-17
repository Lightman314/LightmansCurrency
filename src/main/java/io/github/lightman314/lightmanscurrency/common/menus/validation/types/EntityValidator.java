package io.github.lightman314.lightmanscurrency.common.menus.validation.types;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidatorType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class EntityValidator extends MenuValidator {

    public static final MenuValidatorType TYPE = new Type();

    private int id = -1;
    private Entity entity;
    private int safeGetID() { return this.entity == null ? this.id : this.entity.getId(); }
    private void validateEntity(Player player)
    {
        if(this.id >= 0 && this.entity == null)
        {
            this.entity = player.level().getEntity(this.id);
            this.id = -1;
        }
    }

    private EntityValidator(int entityID) { super(TYPE); this.id = entityID;}
    private EntityValidator(Entity entity) { super(TYPE); this.entity = entity; }

    public static EntityValidator of(Entity entity) { return new EntityValidator(entity); }

    @Override
    protected void encodeAdditional(FriendlyByteBuf buffer) { buffer.writeInt(this.safeGetID()); }

    @Override
    protected void saveAdditional(CompoundTag tag) { tag.putInt("EntityID", this.safeGetID()); }

    @Override
    public boolean stillValid(Player player) {
        this.validateEntity(player);
        return this.entity != null && player.canInteractWithEntity(this.entity,4f);
    }

    private static class Type extends MenuValidatorType
    {
        protected Type() { super(LightmansCurrency.id("entity")); }
        
        @Override
        public MenuValidator decode(FriendlyByteBuf buffer) { return new EntityValidator(buffer.readInt()); }
        
        @Override
        public MenuValidator load(CompoundTag tag) { return new EntityValidator(tag.getInt("EntityID")); }
    }

}
