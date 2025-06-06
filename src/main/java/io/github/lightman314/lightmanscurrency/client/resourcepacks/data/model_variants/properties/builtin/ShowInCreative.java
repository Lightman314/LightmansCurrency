package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.builtin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.VariantPropertyWithDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.util.GsonHelper;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public record ShowInCreative(boolean show, boolean locked) {

    public static final ShowInCreative FALSE = new ShowInCreative(false,false);
    public static final ShowInCreative TRUE = new ShowInCreative(true,false);
    public static final ShowInCreative LOCKED = new ShowInCreative(true,true);

    public static final VariantPropertyWithDefault<ShowInCreative> PROPERTY = new Property();

    private static class Property extends VariantPropertyWithDefault<ShowInCreative>
    {

        @Override
        public ShowInCreative getMissingDefault() { return FALSE; }
        @Override
        public ShowInCreative getBuilderDefault() { return TRUE; }

        @Override
        public ShowInCreative parse(JsonElement element) throws JsonSyntaxException, ResourceLocationException {
            if(element.isJsonPrimitive())
                return GsonHelper.convertToBoolean(element,this.getID().toString()) ? TRUE : FALSE;
            JsonObject object = GsonHelper.convertToJsonObject(element,this.getID().toString());
            boolean locked = GsonHelper.getAsBoolean(object,"locked");
            return new ShowInCreative(true,locked);
        }

        @Override
        public JsonElement write(Object value) {
            if(value instanceof ShowInCreative sic)
            {
                if(sic.locked && sic.show)
                {
                    JsonObject result = new JsonObject();
                    result.addProperty("locked",sic.locked);
                    return result;
                }
                else
                    return new JsonPrimitive(sic.show);
            }
            else if(value instanceof Boolean bool)
                return new JsonPrimitive(bool);
            throw new IllegalArgumentException("Value must be an ShowInCreative element!");
        }
    }

}