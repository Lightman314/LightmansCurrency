package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.builtin;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.VariantProperty;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.GsonHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TooltipInfo {

    public static final VariantProperty<TooltipInfo> PROPERTY = new TooltipInfoProperty();

    private final List<Component> tooltip;
    public List<Component> getTooltip() { return new ArrayList<>(this.tooltip); }
    public final boolean drawOnSelection;
    public final boolean drawOnItem;
    public final boolean drawOnJade;
    public TooltipInfo(Component tooltip) { this(ImmutableList.of(tooltip),true,true,true); }
    public TooltipInfo(List<Component> tooltip) { this(tooltip,true,true,true); }
    public TooltipInfo(List<Component> tooltip, boolean drawOnSelection, boolean drawOnItem, boolean drawOnJade)
    {
        this.tooltip = ImmutableList.copyOf(tooltip);
        this.drawOnSelection = drawOnSelection;
        this.drawOnItem = drawOnItem;
        this.drawOnJade = drawOnJade;
    }

    public static TooltipInfo ofModifier(TextEntry modifier) { return ofModifier(modifier.get()); }
    public static TooltipInfo ofModifier(MutableComponent modifier) { return new TooltipInfo(LCText.BLOCK_VARIANT_MODIFIER_LABEL.get(modifier.withStyle(ChatFormatting.ITALIC,ChatFormatting.DARK_AQUA)).withStyle(ChatFormatting.GRAY)); }

    private static class TooltipInfoProperty extends VariantProperty<TooltipInfo>
    {
        @Override
        public TooltipInfo parse(JsonElement element) throws JsonSyntaxException, ResourceLocationException {
            String elementName = this.getID().toString();
            JsonObject json = GsonHelper.convertToJsonObject(element,elementName);
            JsonElement tooltipElement = json.get("tooltip");
            if(tooltipElement == null)
                GsonHelper.getAsJsonArray(json,"tooltip");
            List<Component> tooltip;
            if(tooltipElement.isJsonObject() || tooltipElement.isJsonPrimitive())
            {
                Component line = ComponentSerialization.CODEC.decode(JsonOps.INSTANCE,tooltipElement).getOrThrow(JsonSyntaxException::new).getFirst();
                tooltip = new ArrayList<>();
                tooltip.add(line);
            }
            else
            {
                tooltip = new ArrayList<>();
                JsonArray tooltipArray = GsonHelper.convertToJsonArray(tooltipElement,"tooltip");
                for(int i = 0; i < tooltipArray.size(); ++i)
                {
                    Component line = ComponentSerialization.CODEC.decode(JsonOps.INSTANCE,tooltipArray.get(i)).getOrThrow(JsonSyntaxException::new).getFirst();
                    tooltip.add(line);
                }
            }
            boolean drawOnSelection = GsonHelper.getAsBoolean(json,"selection",true);
            boolean drawOnItem = GsonHelper.getAsBoolean(json,"item",true);
            boolean drawOnJade = GsonHelper.getAsBoolean(json,"jade",true);
            return new TooltipInfo(tooltip,drawOnSelection,drawOnItem,drawOnJade);
        }

        @Override
        public JsonElement write(Object value) {
            if(value instanceof TooltipInfo data)
            {
                JsonObject json = new JsonObject();
                if(data.tooltip.size() == 1)
                    json.add("tooltip",ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE,data.tooltip.getFirst()).getOrThrow(RuntimeException::new));
                else
                {
                    JsonArray tooltipArray = new JsonArray();
                    for(Component line : data.tooltip)
                        tooltipArray.add(ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE,line).getOrThrow(RuntimeException::new));
                    json.add("tooltip",tooltipArray);
                }
                json.addProperty("selection",data.drawOnSelection);
                json.addProperty("item",data.drawOnItem);
                json.addProperty("jade",data.drawOnJade);
                return json;
            }
            else
                throw new IllegalArgumentException("Value must be a TooltipInfo element!");
        }
    }

}
