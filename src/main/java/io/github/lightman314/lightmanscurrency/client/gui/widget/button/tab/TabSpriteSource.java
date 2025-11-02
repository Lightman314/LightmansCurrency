package io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab;

import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FixedSizeSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin.WidgetStateSprite;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.WidgetRotation;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public class TabSpriteSource implements Function<WidgetRotation,FixedSizeSprite> {

    private final Map<WidgetRotation,FixedSizeSprite> cache = new HashMap<>();

    private final Function<WidgetRotation,ResourceLocation> fileBuilder;
    private final int size;
    public TabSpriteSource(Function<WidgetRotation,ResourceLocation> fileBuilder,int size)
    {
        this.fileBuilder = fileBuilder;
        this.size = size;
    }

    public static TabSpriteSource create(Function<String,ResourceLocation> fileBuilder,int size) { return new TabSpriteSource(rot -> fileBuilder.apply(rot.name().toLowerCase(Locale.ENGLISH)),size); }
    public static TabSpriteSource createBuiltin(String name,int size) { return create(rot -> VersionUtil.lcResource(name + "_" + rot),size); }

    @Override
    public FixedSizeSprite apply(WidgetRotation rotation) {
        if(!this.cache.containsKey(rotation))
            this.cache.put(rotation,WidgetStateSprite.lazyActive(this.fileBuilder.apply(rotation),this.size,this.size));
        return this.cache.get(rotation);
    }

}