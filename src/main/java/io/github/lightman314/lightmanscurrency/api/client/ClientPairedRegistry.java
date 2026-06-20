package io.github.lightman314.lightmanscurrency.api.client;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A pseudo-registry for client-only classes that can be matched with a common registry object.
 * @param <T> The type of the common class
 * @param <C> The type of the client-only class
 */
public class ClientPairedRegistry<T,C> {

    private final Registry<T> registry;
    private final Map<Identifier,C> clientRegistry = new HashMap<>();
    private final Supplier<C> defaultValue;

    public ClientPairedRegistry(Registry<T> registry) { this(registry,() -> null); }
    public ClientPairedRegistry(Registry<T> registry,C defaultValue) { this(registry,() -> defaultValue); }
    public ClientPairedRegistry(Registry<T> registry,Supplier<C> defaultValue) { this.registry = registry; this.defaultValue = defaultValue; }

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
    public C getDefaultValue() throws IllegalStateException
    {
        C d = this.defaultValue.get();
        if(d == null)
            throw new IllegalStateException("Default Value is not defined!");
        return d;
    }

    /**
     * Gets the registered client-only entry that matches the common entry.
     * @param commonEntry The common entry that we want the client-only entry for.
     * @return The client-only entry that matches the common entry.
     * @throws IllegalStateException if the common entry is not registered to the registry, or if there is no client entry registered to this common entry and no default value is defined.
     */
    public C getValue(T commonEntry) throws IllegalStateException { return this.getValue(this.getKey(commonEntry)); }
    /**
     * Gets the registered client-only entry that matches the common entry.
     * @param id The id of the common entry that we want the client-only entry for.
     * @return The client-only entry that matches the common entry.
     * @throws IllegalStateException if there is no client entry registered to this common entry and no default value is defined.
     */
    public C getValue(Identifier id) throws IllegalStateException {
        if(!this.clientRegistry.containsKey(id))
            return this.getDefaultValue();
        return this.clientRegistry.get(id);
    }

}