package io.github.lightman314.lightmanscurrency.api.client.gui.sprites;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.WidgetFacing;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.Identifier;

import java.util.Locale;

public interface FaceSensitiveSprites {

    WidgetSprites getSprite(WidgetFacing facing);
    default Identifier getSprite(boolean isActive,boolean isHoveredOrFocused,WidgetFacing facing) {
        WidgetSprites sprites = this.getSprite(facing);
        return sprites.get(isActive,isHoveredOrFocused);
    }

    static FaceSensitiveSprites forTabs(Identifier baseID) {
        ImmutableMap.Builder<WidgetFacing,WidgetSprites> builder = ImmutableMap.builderWithExpectedSize(WidgetFacing.values().length);
        for(WidgetFacing facing : WidgetFacing.values())
        {
            Identifier active = baseID.withSuffix((baseID.getPath().endsWith("/") ? "" : "_") + facing.toString().toLowerCase(Locale.ENGLISH));
            Identifier inactive = active.withSuffix("_disabled");
            builder.put(facing,new WidgetSprites(active,inactive,active,inactive));
        }
        return new NormalSprites(builder.build());
    }

    final class NormalSprites implements FaceSensitiveSprites {
        private final ImmutableMap<WidgetFacing,WidgetSprites> cache;
        public NormalSprites(ImmutableMap<WidgetFacing,WidgetSprites> cache) {
            this.cache = cache;
            if(this.cache.size() != WidgetFacing.values().length)
                throw new IllegalStateException("Cache must contain an entry for all WidgetFacing values!");
        }
        @Override
        public WidgetSprites getSprite(WidgetFacing facing) { return this.cache.get(facing); }
    }

}