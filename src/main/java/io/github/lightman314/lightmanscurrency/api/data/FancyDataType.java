package io.github.lightman314.lightmanscurrency.api.data;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.RegistryHelper;
import io.github.lightman314.lightmanscurrency.client.data.ClientFancyDataCache;
import io.github.lightman314.lightmanscurrency.features.api_impl.data.FancySaveData;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public final class FancyDataType<T extends FancyData> {

    public final String fileName;
    private final Supplier<T> factory;
    private final Codec<T> codec;
    public final boolean serverOnly;
    public FancyDataType(String fileName,Supplier<T> factory,Codec<T> codec) { this(fileName,factory,codec,false); }
    public FancyDataType(String fileName,Supplier<T> factory,Codec<T> codec,boolean serverOnly) {
        this.fileName = fileName;
        this.factory = factory;
        this.codec = codec;
        this.serverOnly = serverOnly;
    }

    public T create() { return this.factory.get(); }

    public Codec<T> getCodec() { return this.codec; }

    @Nullable
    public T get(boolean isClient) { return this.get(ISidedContext.known(isClient)); }
    @Nullable
    public T get(ISidedContext context) { return context.isClient() ? ClientFancyDataCache.getData(this) : FancySaveData.getData(this); }

    public T getUnknown() {
        if(this.isLoaded(false))
            return this.get(false);
        return this.get(true);
    }

    public boolean isLoaded(boolean isClient) { return this.isLoaded(ISidedContext.known(isClient)); }

    public boolean isLoaded(ISidedContext context) { return (context.isClient() && !this.serverOnly) || (context.isServer() && FancySaveData.isLoaded(this)); }

    @Override
    public int hashCode() { return RegistryHelper.hash(LCRegistries.Data.FANCY_DATA,this); }
    @Override
    public String toString() { return RegistryHelper.toString("FancyDataType",LCRegistries.Data.FANCY_DATA,this); }
}