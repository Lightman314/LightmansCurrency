package io.github.lightman314.lightmanscurrency.common.menus.validation;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.SimpleValidator;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class MenuValidator {

    public final MenuValidatorType type;
    public boolean isThroughNetwork = false;
    protected MenuValidator(MenuValidatorType type) { this.type = type; }

    public final void encode(FriendlyByteBuf buffer)
    {
        buffer.writeUtf(this.type.type.toString());
        this.encodeAdditional(buffer);
        buffer.writeBoolean(this.isThroughNetwork);
        //LightmansCurrency.LogDebug("Encoded MenuValidator of type '" + this.type.type);
    }

    protected abstract void encodeAdditional(FriendlyByteBuf buffer);

    public final boolean isNull() { return this == SimpleValidator.NULL; }

    public final void save()
    {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        tag.putString("Type", this.type.type.toString());
        if(this.isThroughNetwork)
            tag.put("NetworkAccess",new CompoundTag());
        //LightmansCurrency.LogDebug("Saved MenuValidator of type '" + this.type.type);
    }

    protected abstract void saveAdditional(CompoundTag tag);

    public abstract boolean stillValid(Player player);

    public static MenuValidator decode(FriendlyByteBuf buffer)
    {
        try {
            ResourceLocation type = VersionUtil.parseResource(buffer.readUtf());
            MenuValidatorType decoder = MenuValidatorType.getType(type);
            if(decoder != null)
            {
                //LightmansCurrency.LogDebug("Decoding MenuValidator of type '" + type + "'!");
                MenuValidator result = decoder.decode(buffer);
                result.isThroughNetwork = buffer.readBoolean();
                return result;
            }
            LightmansCurrency.LogError("Could not decode MenuValidator of type '" + type + "'!");
            return SimpleValidator.NULL;
        } catch(Throwable t) { LightmansCurrency.LogError("Error decoding MenuValidator!"); return SimpleValidator.NULL; }
    }


    public static MenuValidator load(CompoundTag tag)
    {
        try {
            ResourceLocation type = VersionUtil.parseResource(tag.getString("Type"));
            MenuValidatorType decoder = MenuValidatorType.getType(type);
            if(decoder != null)
            {
                //LightmansCurrency.LogDebug("Loading MenuValidator of type '" + type + "'!");
                MenuValidator result = decoder.load(tag);
                result.isThroughNetwork = tag.contains("NetworkAccess");
                return result;
            }
            LightmansCurrency.LogError("Could not load MenuValidator of type '" + type + "'!");
            return SimpleValidator.NULL;
        } catch(Throwable t) { LightmansCurrency.LogError("Error loading MenuValidator!"); return SimpleValidator.NULL; }
    }

}