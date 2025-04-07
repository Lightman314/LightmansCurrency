package io.github.lightman314.lightmanscurrency.api.easy_data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class ReadWriteContext {

    public final CompoundTag tag;
    public final HolderLookup.Provider lookup;
    private ReadWriteContext(CompoundTag tag, HolderLookup.Provider lookup) { this.tag = tag; this.lookup = lookup; }

    public static ReadWriteContext of(CompoundTag tag, HolderLookup.Provider lookup) { return new ReadWriteContext(tag,lookup); }

}
