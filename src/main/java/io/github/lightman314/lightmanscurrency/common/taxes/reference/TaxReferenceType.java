package io.github.lightman314.lightmanscurrency.common.taxes.reference;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class TaxReferenceType {

    private static final Map<ResourceLocation, TaxReferenceType> types = new HashMap<>();

    @Nullable
    public static TaxReferenceType getType(ResourceLocation type) { return types.get(type); }

    protected final ResourceLocation typeID;
    protected TaxReferenceType(ResourceLocation typeID) { this.typeID = typeID; }

    public static void register(TaxReferenceType type) {
        ResourceLocation id = type.typeID;
        if(types.containsKey(id))
            LightmansCurrency.LogWarning("Attempted to registerNotification the TaxReferenceType '" + id + "' twice!");
        else
        {
            types.put(id, type);
            LightmansCurrency.LogDebug("Registered TaxReferenceType '" + id + "'!");
        }
    }

    public abstract TaxableReference load(CompoundTag tag);

}
