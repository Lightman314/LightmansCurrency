package io.github.lightman314.lightmanscurrency.api.taxes.reference;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TaxReferenceType {

    public final ResourceLocation typeID;
    protected TaxReferenceType(@Nonnull ResourceLocation typeID) { this.typeID = typeID; }

    public abstract TaxableReference load(CompoundTag tag);

}
