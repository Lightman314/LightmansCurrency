package io.github.lightman314.lightmanscurrency.common.bank.reference;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public abstract class BankReferenceType {

    private static final Map<ResourceLocation, BankReferenceType> TYPES = new HashMap<>();

    public static void register(BankReferenceType type)
    {
        ResourceLocation id = type.id;
        if(TYPES.containsKey(id))
            LightmansCurrency.LogWarning("Attempted to register the AccountReferenceType '" + id + "' twice!");
        else
        {
            TYPES.put(id, type);
            LightmansCurrency.LogDebug("Registered BankReferenceType '" + id + "'!");
        }
    }

    public static BankReferenceType getType(ResourceLocation type) { return TYPES.get(type); }

    protected final ResourceLocation id;
    protected BankReferenceType(ResourceLocation id) { this.id = id; }

    public abstract BankReference load(CompoundTag tag);
    public abstract BankReference decode(FriendlyByteBuf buffer);


}