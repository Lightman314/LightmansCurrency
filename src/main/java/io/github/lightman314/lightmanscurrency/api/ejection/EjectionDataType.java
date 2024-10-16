package io.github.lightman314.lightmanscurrency.api.ejection;

import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

public abstract class EjectionDataType {

    @Nonnull
    public abstract EjectionData load(@Nonnull CompoundTag tag);

}