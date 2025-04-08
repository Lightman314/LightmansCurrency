package io.github.lightman314.lightmanscurrency.api.easy_data;

import net.minecraft.nbt.CompoundTag;

public class ReadWriteContext {

    public final CompoundTag tag;
    private ReadWriteContext(CompoundTag tag) { this.tag = tag; }

    public static ReadWriteContext of(CompoundTag tag) { return new ReadWriteContext(tag); }

}