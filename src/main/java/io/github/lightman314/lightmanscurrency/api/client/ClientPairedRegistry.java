package io.github.lightman314.lightmanscurrency.api.client;

import net.minecraft.core.Registry;
import net.minecraft.core.TypedInstance;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

/**
 * A pseudo-registry for client-only classes that can be matched with a common registry object.
 * @param <T> The type of the common class
 * @param <C> The type of the client-only class
 */
public class ClientPairedRegistry<T,C> implements Iterable<C> {

    private final Registry<T> registry;
    private final Map<Identifier,C> clientRegistry = new HashMap<>();
    @Nullable
    private final C defaultValue;
    private final boolean throwIfUndefined;

    private ClientPairedRegistry(Builder<T,C> builder) {
        this.registry = builder.registry;
        this.defaultValue = builder.defaultValue.get();
        this.throwIfUndefined = builder.throwIfUndefined;
    }

    private Identifier getKey(T commonEntry) throws IllegalStateException
    {
        Identifier id = this.registry.getKey(commonEntry);
        if(id == null)
            throw new IllegalStateException("Cannot get the id of an unregistered object!");
        return id;
    }

    /**
     * Registers the client entry to this registry.
     * @param commonEntry the common entry that this should be registered under.
     * @param value the client-only value that should be paired with the common entry.
     * @throws IllegalStateException if the common entry is not registered to common registry.
     */
    public void register(T commonEntry,C value) throws IllegalStateException
    {
        this.register(this.getKey(commonEntry),value);
    }
    /**
     * Registers the client entry to this registry.
     * @param id the id of the common entry that this should be registered under.
     * @param value the client-only value that should be paired with the common entry.
     */
    public void register(Identifier id,C value)
    {
        this.clientRegistry.put(id,Objects.requireNonNull(value));
    }

    /**
     * @return The default value for this client paired registry.
     * @throws IllegalStateException if no default value has been defined.
     */
    @Nullable
    public C getDefaultValue() throws IllegalStateException
    {
        if(this.defaultValue == null && this.throwIfUndefined)
            throw new IllegalStateException("Default Value is not defined!");
        return this.defaultValue;
    }
    @Nullable
    public C getValue(TypedInstance<T> commonEntry) { return getValue(commonEntry.typeHolder().value()); }
    /**
     * Gets the registered client-only entry that matches the common entry.
     * @param commonEntry The common entry that we want the client-only entry for.
     * @return The client-only entry that matches the common entry.
     * @throws IllegalStateException if the common entry is not registered to the registry, or if there is no client entry registered to this common entry and no default value is defined.
     */
    @Nullable
    public C getValue(T commonEntry) throws IllegalStateException { return this.getValue(this.getKey(commonEntry)); }
    /**
     * Gets the registered client-only entry that matches the common entry.
     * @param id The id of the common entry that we want the client-only entry for.
     * @return The client-only entry that matches the common entry.
     * @throws IllegalStateException if there is no client entry registered to this common entry and no default value is defined.
     */
    @Nullable
    public C getValue(Identifier id) throws IllegalStateException {
        if(!this.clientRegistry.containsKey(id))
            return this.getDefaultValue();
        return this.clientRegistry.get(id);
    }

    @Override
    public Iterator<C> iterator() { return this.clientRegistry.values().iterator(); }

    public static <T,C> Builder<T,C> builder(Registry<T> registry,Class<C> clazz) { return new Builder<>(registry); }

    public static final class Builder<T,C> {

        private final Registry<T> registry;
        private Supplier<C> defaultValue = () -> null;
        private boolean throwIfUndefined = false;
        private Builder(Registry<T> registry) { this.registry = registry; }

        public Builder<T,C> defaultValue(C defaultValue) { this.defaultValue = () -> defaultValue; return this; }
        public Builder<T,C> defaultValueBuilder(Supplier<C> defaultValue) { this.defaultValue = defaultValue; return this; }

        public Builder<T,C> throwIfUndefined() { this.throwIfUndefined = true; return this; }

        public ClientPairedRegistry<T,C> build() { return new ClientPairedRegistry<>(this); }

    }

}