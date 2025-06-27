package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.builtin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.IIndependentProperty;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.VariantProperty;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.util.GsonHelper;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public record FreezerDoorData(float rotation, float hingeX, float hingeZ) implements IIndependentProperty {

    public static final VariantProperty<FreezerDoorData> PROPERTY = new Property();

    public static final FreezerDoorData DEFAULT = new FreezerDoorData(90f,15.5f/16f,3.5f/16f);

    private static class Property extends VariantProperty<FreezerDoorData>
    {

        @Override
        public FreezerDoorData parse(JsonElement element) throws JsonSyntaxException, ResourceLocationException {
            JsonObject json = GsonHelper.convertToJsonObject(element,this.getID().toString());
            if(json.has("rotation") || json.has("hingeX") || json.has("hingeZ"))
            {
                float rotation = GsonHelper.getAsFloat(json,"rotation",DEFAULT.rotation);
                float hingeX = GsonHelper.getAsFloat(json,"hingeX",DEFAULT.hingeX);
                float hingeZ = GsonHelper.getAsFloat(json,"hingeZ",DEFAULT.hingeZ);
                return new FreezerDoorData(rotation,hingeX,hingeZ);
            }
            throw new JsonSyntaxException("Must include 'rotation','hingeX', or 'hingeZ' property!");
        }

        @Override
        public JsonElement write(Object value) {
            if(value instanceof FreezerDoorData data)
            {
                JsonObject json = new JsonObject();
                json.addProperty("rotation",data.rotation);
                json.addProperty("hingeX",data.hingeX);
                json.addProperty("hingeZ",data.hingeZ);
                return json;
            }
            else
                throw new IllegalArgumentException("Value must be a FreezerDoorData element!");
        }
    }

}