package io.github.lightman314.lightmanscurrency.common.menus.validation;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class MenuValidatorType {

    private static final Map<ResourceLocation,MenuValidatorType> TYPES = new HashMap<>();

    public static void register(MenuValidatorType validator)
    {
        if(TYPES.containsKey(validator.type))
        {
            LightmansCurrency.LogWarning("Attempted to register duplicate validator type '" + validator.type + "'!");
            return;
        }
        TYPES.put(validator.type, validator);
    }

    public static MenuValidatorType getType(ResourceLocation type) { return TYPES.get(type); }

    public final ResourceLocation type;
    protected MenuValidatorType(ResourceLocation type) { this.type = type; }

    public abstract MenuValidator decode(FriendlyByteBuf buffer);
    public abstract MenuValidator load(CompoundTag tag);


}
