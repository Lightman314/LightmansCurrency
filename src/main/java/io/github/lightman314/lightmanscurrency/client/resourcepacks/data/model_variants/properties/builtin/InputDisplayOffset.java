package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.builtin;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.VariantProperty;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class InputDisplayOffset {

    public static final VariantProperty<InputDisplayOffset> PROPERTY = new InputDisplayOffsetProperty();

    private final Map<Direction,ScreenPosition> offsetMap;
    public InputDisplayOffset(Map<Direction,ScreenPosition> offsetMap) { this.offsetMap = ImmutableMap.copyOf(offsetMap); }

    public ScreenPosition getOffset(Direction side) { return this.offsetMap.getOrDefault(side,ScreenPosition.ZERO); }

    private static class InputDisplayOffsetProperty extends VariantProperty<InputDisplayOffset> {

        @Override
        public InputDisplayOffset parse(JsonElement element) throws JsonSyntaxException, ResourceLocationException {
            String elementName = this.getID().toString();
            Map<Direction,ScreenPosition> offsetMap = new HashMap<>();
            JsonObject json = GsonHelper.convertToJsonObject(element,elementName);
            for(Direction side : Direction.values())
            {
                if(json.has(side.toString()))
                {
                    JsonObject field = GsonHelper.getAsJsonObject(json,side.toString());
                    int x = GsonHelper.getAsInt(field,"x",0);
                    int y = GsonHelper.getAsInt(field,"y",0);
                    if(x != 0 || y != 0)
                        offsetMap.put(side,ScreenPosition.of(x,y));
                }
            }
            return new InputDisplayOffset(offsetMap);
        }

        @Override
        public JsonElement write(Object value) {
            if(value instanceof InputDisplayOffset data)
            {
                JsonObject json = new JsonObject();
                data.offsetMap.forEach((side,offset) -> {
                    if(offset.x != 0 || offset.y != 0)
                    {
                        JsonObject field = new JsonObject();
                        if(offset.x != 0)
                            field.addProperty("x",offset.x);
                        if(offset.y != 0)
                            field.addProperty("y",offset.y);
                        json.add(side.toString(),field);
                    }
                });
                return json;
            }
            else
                throw new IllegalArgumentException("Value must be an InputDisplayOffset element!");
        }
    }

}
