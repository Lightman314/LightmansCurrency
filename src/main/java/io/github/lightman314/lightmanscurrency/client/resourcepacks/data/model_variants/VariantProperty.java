package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class VariantProperty<T> {

    private static final Map<ResourceLocation,VariantProperty<?>> registry = new HashMap<>();

    public static void register(ResourceLocation type,VariantProperty<?> property)
    {
        if(registry.containsKey(type))
        {
            LightmansCurrency.LogWarning("Attempted to register duplicate VariantPropery '" + type + "'!");
            return;
        }
        registry.put(type,property);
        property.id = type;
    }

    private ResourceLocation id;
    public ResourceLocation getID() { return Objects.requireNonNull(id,"VariantProperty has not been properly registered!"); }

    public static void forEach(BiConsumer<ResourceLocation,VariantProperty<?>> consumer) { registry.forEach(consumer); }

    public abstract T parse(JsonElement element) throws JsonSyntaxException, ResourceLocationException;

    public abstract JsonElement write(Object value);

}
