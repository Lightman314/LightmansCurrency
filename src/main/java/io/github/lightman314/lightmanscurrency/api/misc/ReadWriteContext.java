package io.github.lightman314.lightmanscurrency.api.misc;

import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ReadWriteContext<T> {

    public final T data;
    public final HolderLookup.Provider lookup;

    public ReadWriteContext(T data,HolderLookup.Provider lookup)
    {
        this.data = data;
        this.lookup = lookup;
    }

    public static ReadWriteContext<CompoundTag> createTag(HolderLookup.Provider lookup) { return new ReadWriteContext<>(new CompoundTag(),lookup); }
    public static ReadWriteContext<JsonObject> createJson(HolderLookup.Provider lookup) { return new ReadWriteContext<>(new JsonObject(),lookup); }

    public ReadWriteContext<T> getEntry(String key) {
        if(this.data instanceof CompoundTag tag)
        {
            CompoundTag child = tag.getCompound(key);
            if(child == null)
                return null;
            return new ReadWriteContext<>((T)child,this.lookup);
        }
        else
            return null;
    }

}
