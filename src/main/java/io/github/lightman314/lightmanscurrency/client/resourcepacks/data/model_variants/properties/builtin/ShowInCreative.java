package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.builtin;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import io.github.lightman314.lightmanscurrency.api.variants.item.IVariantItem;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.VariantPropertyWithDefault;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public record ShowInCreative(boolean show, boolean locked, List<ResourceLocation> targets) {

    public static final ShowInCreative FALSE = new ShowInCreative(false,false, ImmutableList.of());
    public static final ShowInCreative TRUE = new ShowInCreative(true,false, ImmutableList.of());
    public static final ShowInCreative LOCKED = new ShowInCreative(true,true, ImmutableList.of());

    public static final VariantPropertyWithDefault<ShowInCreative> PROPERTY = new Property();

    public boolean showFor(IVariantItem item) {
        if(this.targets.isEmpty())
            return true;
        return this.targets.contains(item.getItemID());
    }

    public ShowInCreative withTargets(List<ResourceLocation> targets) { return new ShowInCreative(this.show,this.locked,ImmutableList.copyOf(targets)); }

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
            List<ResourceLocation> targets = new ArrayList<>();
            if(object.has("targets"))
            {
                if(object.get("targets").isJsonPrimitive())
                    targets.add(VersionUtil.parseResource(GsonHelper.getAsString(object,"targets")));
                else
                {
                    JsonArray targetList = GsonHelper.getAsJsonArray(object,"targets");
                    for(int i = 0; i < targetList.size(); ++i)
                        targets.add(VersionUtil.parseResource(GsonHelper.convertToString(targetList.get(i),"targets[" + i + "]")));
                }
            }
            return new ShowInCreative(true,locked,ImmutableList.copyOf(targets));
        }

        @Override
        public JsonElement write(Object value) {
            if(value instanceof ShowInCreative sic)
            {
                if(sic.show && (sic.locked || !sic.targets.isEmpty()))
                {
                    JsonObject result = new JsonObject();
                    result.addProperty("locked",sic.locked);
                    if(sic.targets.size() == 1)
                        result.addProperty("targets",sic.targets.get(0).toString());
                    else if(!sic.targets.isEmpty())
                    {
                        JsonArray targetList = new JsonArray();
                        for(ResourceLocation t : sic.targets)
                            targetList.add(t.toString());
                        result.add("targets",targetList);
                    }
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