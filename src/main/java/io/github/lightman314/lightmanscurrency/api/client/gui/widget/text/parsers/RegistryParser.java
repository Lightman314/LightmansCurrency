package io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.parsers;

import net.minecraft.IdentifierException;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

public class RegistryParser<T> implements Function<String,T> {

    private final boolean requireNamespace;
    private final Supplier<T> emptyValue;
    private final Registry<T> registry;
    private RegistryParser(Builder<T> builder) {
        this.requireNamespace = builder.requireNamespace;
        this.emptyValue = builder.emptyValue;
        this.registry = builder.registry;
    }

    @Override
    @Nullable
    public T apply(String s) {
        if(s.isBlank())
            return this.emptyValue.get();
        try {
            if(this.requireNamespace && !s.contains(":"))
                return null;
            Identifier key = Identifier.parse(s);
            if(this.registry.containsKey(key))
                return this.registry.getValue(key);
        }catch (IdentifierException ignored) { }
        return null;
    }

    public Function<T,String> writer() { return value -> this.registry.getKey(value).toString(); }

    public static <T> Builder<T> builder(Registry<T> registry) { return new Builder<>(registry); }

    public static final class Builder<T> {

        private final Registry<T> registry;
        private Builder(Registry<T> registry) { this.registry = registry; }

        private boolean requireNamespace = false;
        private Supplier<T> emptyValue = () -> null;

        public Builder<T> requiresNamespace() { this.requireNamespace = true; return this; }
        public Builder<T> withEmptyValue(T emptyValue) { return this.withEmptyValue(() -> emptyValue); }
        public Builder<T> withEmptyValue(Supplier<T> emptyValue) { this.emptyValue = emptyValue; return this; }

        public RegistryParser<T> build() { return new RegistryParser<>(this); }

    }

}
