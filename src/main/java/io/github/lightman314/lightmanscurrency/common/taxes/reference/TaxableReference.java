package io.github.lightman314.lightmanscurrency.common.taxes.reference;

import io.github.lightman314.lightmanscurrency.common.taxes.ITaxable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public abstract class TaxableReference {

    public final TaxReferenceType type;
    protected TaxableReference(TaxReferenceType type) { this.type = type; }

    @Nullable
    public abstract ITaxable getTaxable(boolean isClient);

    public final boolean stillValid(boolean isClient) { return this.getTaxable(isClient) != null; }

    public final CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        tag.putString("Type",this.type.typeID.toString());
        return tag;
    }

    protected abstract void saveAdditional(CompoundTag tag);

    @Nullable
    public static TaxableReference load(CompoundTag tag)
    {
        ResourceLocation type = new ResourceLocation(tag.getString("Type"));
        TaxReferenceType t = TaxReferenceType.getType(type);
        if(t != null)
            return t.load(tag);
        return null;
    }

    @Override
    public final boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj instanceof TaxableReference reference)
            return this.matches(reference);
        return false;
    }

    protected abstract boolean matches(TaxableReference otherReference);
}
