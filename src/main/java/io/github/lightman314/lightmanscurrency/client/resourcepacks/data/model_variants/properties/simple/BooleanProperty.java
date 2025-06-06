package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.simple;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.VariantPropertyWithDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.util.GsonHelper;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BooleanProperty extends VariantPropertyWithDefault<Boolean> {

    public BooleanProperty() {}

    @Override
    public Boolean parse(JsonElement element) throws JsonSyntaxException, ResourceLocationException { return GsonHelper.convertToBoolean(element,this.getID().toString()); }

    @Override
    public JsonElement write(Object value) {
        if(value instanceof Boolean v)
            return new JsonPrimitive(v);
        throw new IllegalArgumentException("Value must be a boolean!");
    }

    @Override
    public Boolean getMissingDefault() { return false; }
    @Override
    public Boolean getBuilderDefault() { return true; }

}