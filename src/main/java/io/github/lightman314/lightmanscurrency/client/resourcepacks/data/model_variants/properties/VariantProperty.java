package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.events.client.RegisterVariantPropertiesEvent;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModLoader;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class VariantProperty<T> {

    private static Map<ResourceLocation,VariantProperty<?>> registry = null;

    private ResourceLocation id;
    public ResourceLocation getID() { return Objects.requireNonNull(id,"VariantProperty has not been properly registered!"); }
    public void setID(ResourceLocation id) { if(this.id != null) throw new IllegalStateException("Cannot define the ID of a Variant Property that's already been registered!"); this.id = id; }

    @Override
    public int hashCode() { return this.getID().hashCode(); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof VariantProperty<?> other)
            return other.getClass() == this.getClass() && other.getID().equals(this.getID());
        return false;
    }

    public static void forEach(BiConsumer<ResourceLocation,VariantProperty<?>> consumer) {
        confirmRegistration();
        registry.forEach(consumer);
    }

    public static void confirmRegistration()
    {
        if(registry != null)
            return;
        RegisterVariantPropertiesEvent event = new RegisterVariantPropertiesEvent();
        ModLoader.get().postEvent(event);
        registry = event.getRegistry();
    }

    public abstract T parse(JsonElement element) throws JsonSyntaxException, ResourceLocationException;

    public abstract JsonElement write(Object value);

}