package io.github.lightman314.lightmanscurrency.api.text;

import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.types.VanillaColor;
import net.minecraft.network.chat.MutableComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ColorBundle extends TextEntryBundle<VanillaColor> {

    private ColorBundle(Map<VanillaColor,TextEntry> map) { super(map); }

    public static ColorBundle of(Function<String,TextEntry> factory) {
        Map<VanillaColor,TextEntry> map = new HashMap<>();
        for(VanillaColor color : VanillaColor.values())
            map.put(color,factory.apply(EnumHelper.resourceSafeName(color)));
        return new ColorBundle(map);
    }

    public MutableComponent getColored(VanillaColor color) { return this.getComponent(color).withStyle(s -> s.withColor(color.color.getTextColor())); }

}
