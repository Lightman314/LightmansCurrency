package io.github.lightman314.lightmanscurrency.client.util;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ClientRegistry<T,C> implements Iterable<C> {

    private final Registry<T> registry;
    private final String name;
    private final Function<T,C> defaultValue;
    private final Map<ResourceLocation,C> data = new HashMap<>();
    public ClientRegistry(Registry<T> registry,String name) { this(registry,name,(Supplier<C>)null); }
    public ClientRegistry(Registry<T> registry,String name,C defaultValue) { this(registry,name,() -> defaultValue); }
    public ClientRegistry(Registry<T> registry,String name,Supplier<C> defaultValue) { this(registry,name,t -> defaultValue.get()); }
    public ClientRegistry(Registry<T> registry,String name,Function<T,C> defaultValue) {
        this.registry = registry;
        this.name = name;
        this.defaultValue = defaultValue;
    }

    private ResourceLocation getID(T key) { return this.registry.getKey(key); }

    public void register(T key,C value) {
        ResourceLocation id = this.getID(key);
        C old = this.data.put(id,Objects.requireNonNull(value,"Cannot register a null " + this.name));
        if(old != null)
            LightmansCurrency.LogError("Registered duplicate " + this.name + " for " + id);
    }

    @SafeVarargs
    public final void registerBatch(C value,T... keys) {
        for(T key : keys)
            this.register(key,value);
    }

    @Nullable
    private C getDefaultValue(T key) { return this.defaultValue == null ? null : this.defaultValue.apply(key); }

    @Nullable
    public C getOrDefault(T key) {
        C result = this.data.get(this.getID(key));
        if(result == null)
        {
            result = this.getDefaultValue(key);
            if(result != null) //Store the default value into the map so that we don't have to continue getting it every time
                this.data.put(this.getID(key),result);
        }
        return result;
    }

    public C getOrThrow(T key) { return this.getOrThrow(key,IllegalStateException::new); }
    public C getOrThrow(T key,Function<String,RuntimeException> exception) {
        C result = this.getOrDefault(key);
        if(result == null)
            throw exception.apply("No " + this.name + " was registered for " + this.getID(key));
        return result;
    }

    public void forEach(BiConsumer<T,C> action) {
        this.data.forEach((id,value) -> {
            T key = this.registry.get(id);
            action.accept(key,value);
        });
    }

    @Override
    public Iterator<C> iterator() { return this.data.values().iterator(); }

}