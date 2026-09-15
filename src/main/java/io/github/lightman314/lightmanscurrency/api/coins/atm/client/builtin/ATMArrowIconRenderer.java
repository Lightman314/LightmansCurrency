package io.github.lightman314.lightmanscurrency.api.coins.atm.client.builtin;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.SizedSprite;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.WidgetContextSprite;
import io.github.lightman314.lightmanscurrency.api.coins.atm.client.ATMIconRenderer;
import io.github.lightman314.lightmanscurrency.api.coins.atm.icons.ATMIconData;
import io.github.lightman314.lightmanscurrency.api.coins.atm.icons.builtin.ATMArrowIcon;
import io.github.lightman314.lightmanscurrency.api.coins.atm.icons.builtin.ATMArrowIcon.ArrowType;

import java.util.Locale;
import java.util.Map;

public final class ATMArrowIconRenderer extends ATMIconRenderer {

    public static final ATMArrowIconRenderer INSTANCE = new ATMArrowIconRenderer();

    private final Map<ArrowType, SizedSprite> arrowSprites;

    private ATMArrowIconRenderer() {
        ImmutableMap.Builder<ArrowType,SizedSprite> builder = ImmutableMap.builderWithExpectedSize(ArrowType.values().length);
        for(ArrowType type : ArrowType.values()) {
            builder.put(type,WidgetContextSprite.hoverToggleSprite(LCApi.id("widget/atm_arrow_" + type.name().toLowerCase(Locale.ROOT)),6,6).buildSprite());
        }
        this.arrowSprites = builder.build();
    }

    @Override
    protected void render(ATMIconData icon,FancyGuiExtractor gui,boolean hovered) {
        if(icon instanceof ATMArrowIcon i) {
            SizedSprite s = this.arrowSprites.get(i.direction);
            if(s instanceof SizedSprite.WithContext c)
                c.defineContext(true,hovered);
            gui.blitSprite(s,icon.xPos,icon.yPos);
        }
    }

}
