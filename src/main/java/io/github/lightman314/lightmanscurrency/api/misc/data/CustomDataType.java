package io.github.lightman314.lightmanscurrency.api.misc.data;

import io.github.lightman314.lightmanscurrency.common.data.*;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@FieldsAreNonnullByDefault
public final class CustomDataType<T extends CustomData> {

    public final String fileName;
    private final Supplier<T> constructor;
    public final boolean serverOnly;
    public CustomDataType(String fileName,Supplier<T> constructor) { this(fileName,constructor,false); }
    public CustomDataType(String fileName,Supplier<T> constructor,boolean serverOnly) { this.fileName = fileName; this.constructor = constructor; this.serverOnly = serverOnly; }

    /**
     * Used to create a new instance of the data<br>
     * Called on both the logical server & the logical client<br>
     * On the logical server {@link CustomData#load(CompoundTag)} will be called to load the data if the data file already exists<br>
     */
    public T create() { return this.constructor.get(); }

    /**
     * Easy access to the data from the given logical side
     */
    @Nullable
    public T get(boolean isClient) { return isClient ? ClientCustomDataCache.getData(this) : CustomSaveData.getData(this); }
    /**
     * Easy access to the data from the logical side of the given side-tracker
     */
    @Nullable
    public T get(IClientTracker tracker) { return get(tracker.isClient()); }

    /**
     * Easy unsided access to the data cache.<br>
     * Use with caution only in instances where you have no way of knowing which side your own, but always assume the possiblity of only getting the client-side data.<br>
     * No alterations to the data should be done with this get, and you should treat it as read-only
     */
    @Nonnull
    public T getUnknown() {
        if(this.isLoaded(false))
            return this.get(false);
        return this.get(true);
    }

    /**
     * Whether the data is loaded<br>
     * Should always be true once the server is loaded, and will always return true on the logical client
     */
    public boolean isLoaded(boolean isClient) { return isClient || CustomSaveData.isLoaded(this); }

    /**
     * Whether the data is loaded<br>
     * Should always be true once the server is loaded, and will always return true on the logical client
     */
    public boolean isLoaded(IClientTracker tracker) { return this.isLoaded(tracker.isClient()); }

}