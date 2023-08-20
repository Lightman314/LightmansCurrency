package io.github.lightman314.lightmanscurrency.common.menus.validation;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.SimpleValidator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

public abstract class MenuValidator {

    public final MenuValidatorType type;
    protected MenuValidator(MenuValidatorType type) { this.type = type; }

    public final void encode(FriendlyByteBuf buffer)
    {
        buffer.writeUtf(this.type.type.toString());
        this.encodeAdditional(buffer);
        //LightmansCurrency.LogDebug("Encoded MenuValidator of type '" + this.type.type);
    }

    protected abstract void encodeAdditional(@Nonnull FriendlyByteBuf buffer);

    public final void save()
    {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        tag.putString("Type", this.type.type.toString());
        //LightmansCurrency.LogDebug("Saved MenuValidator of type '" + this.type.type);
    }

    protected abstract void saveAdditional(@Nonnull CompoundTag tag);

    public abstract boolean stillValid(@Nonnull Player player);

    @Nonnull
    public static MenuValidator decode(@Nonnull FriendlyByteBuf buffer)
    {
        try {
            ResourceLocation type = new ResourceLocation(buffer.readUtf());
            MenuValidatorType decoder = MenuValidatorType.getType(type);
            if(decoder != null)
            {
                //LightmansCurrency.LogDebug("Decoding MenuValidator of type '" + type + "'!");
                return decoder.decode(buffer);
            }
            LightmansCurrency.LogError("Could not decode MenuValidator of type '" + type + "'!");
            return SimpleValidator.NULL;
        } catch(Throwable t) { LightmansCurrency.LogError("Error decoding MenuValidator!"); return SimpleValidator.NULL; }
    }

    @Nonnull
    public static MenuValidator load(@Nonnull CompoundTag tag)
    {
        try {
            ResourceLocation type = new ResourceLocation(tag.getString("Type"));
            MenuValidatorType decoder = MenuValidatorType.getType(type);
            if(decoder != null)
            {
                //LightmansCurrency.LogDebug("Loading MenuValidator of type '" + type + "'!");
                decoder.load(tag);
            }
            LightmansCurrency.LogError("Could not load MenuValidator of type '" + type + "'!");
            return SimpleValidator.NULL;
        } catch(Throwable t) { LightmansCurrency.LogError("Error loading MenuValidator!"); return SimpleValidator.NULL; }
    }

}
