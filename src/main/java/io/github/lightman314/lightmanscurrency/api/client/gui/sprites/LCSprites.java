package io.github.lightman314.lightmanscurrency.api.client.gui.sprites;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.Identifier;

import java.util.function.BooleanSupplier;

public final class LCSprites {
    private LCSprites() {}

    public static final Identifier GENERIC_BACKGROUND = LCApi.id("container/generic_background");
    public static final SizedSprite GENERIC_INFO = new SizedSprite.Simple(LCApi.id("container/generic_info"),10,10);

    public static final HorizontalSizedSprite SEARCH_FIELD = new HorizontalSizedSprite.Simple(LCApi.id("container/search_field"),12);
    public static final SizedSprite SEARCH_ICON = new SizedSprite.Simple(LCApi.id("container/search_icon"),11,14);

    public static final SizedSprite.Template<BooleanSupplier> TOGGLE = DeferredSizedSprite.toggleSprite(LCApi.id("widget/toggle_on"),LCApi.id("widget/toggle_off"),8,18);
    public static final SizedSprite.Template<BooleanSupplier> TOGGLE_COLORED = DeferredSizedSprite.toggleAndHoverToggleSprite(LCApi.id("widget/toggle_colored_on"),LCApi.id("widget/toggle_colored_off"),8,18);

    public static final WidgetSprites BUTTON_GRAY = hoveredSprites(LCApi.id("widget/button_gray"));
    public static final WidgetSprites BUTTON_TRADE_GREEN = hoveredSprites(LCApi.id("widget/button_green"));
    public static final WidgetSprites BUTTON_GREEN = hoveredSprites(LCApi.id("widget/button_green"));
    public static final WidgetSprites BUTTON_BROWN = hoveredSprites(LCApi.id("widget/button_brown"));

    public static final SizedSprite.Builder BUTTON_BIG_ARROW_DOWN = WidgetContextSprite.hoverToggleSprite(LCApi.id("widget/big_arrow_down"),20,10);
    public static final SizedSprite.Builder BUTTON_BIG_ARROW_UP = WidgetContextSprite.hoverToggleSprite(LCApi.id("widget/big_arrow_up"),20,10);
    public static final SizedSprite.Builder BUTTON_BIG_ARROW_LEFT = WidgetContextSprite.hoverToggleSprite(LCApi.id("widget/big_arrow_left"),10,20);
    public static final SizedSprite.Builder BUTTON_BIG_ARROW_RIGHT = WidgetContextSprite.hoverToggleSprite(LCApi.id("widget/big_arrow_right"),10,20);

    public static final SizedSprite.Builder BUTTON_QUICK_INSERT = WidgetContextSprite.hoverToggleSprite(LCApi.id("widget/quick_insert"),10,10);
    public static final SizedSprite.Builder BUTtON_QUICK_EXTRACT = WidgetContextSprite.hoverToggleSprite(LCApi.id("widget/quick_extract"),10,10);

    public static final SizedSprite SMALL_ARROW_DOWN = new SizedSprite.Simple(LCApi.id("container/small_arrow_down"),8,6);
    public static final SizedSprite SMALL_ARROW_UP = new SizedSprite.Simple(LCApi.id("container/small_arrow_up"),8,6);
    public static final SizedSprite SMALL_ARROW_LEFT = new SizedSprite.Simple(LCApi.id("container/small_arrow_left"),6,8);
    public static final SizedSprite SMALL_ARROW_RIGHT = new SizedSprite.Simple(LCApi.id("container/small_arrow_right"),6,8);

    public static final SizedSprite.Builder FREE_TOGGLE = WidgetContextSprite.hoverToggleSprite(LCApi.id("widget/free_toggle"),10,10);

    public static final SizedSprite.Template<BooleanSupplier> CHECKBOX = DeferredSizedSprite.toggleAndHoverToggleSprite(checkboxSprite(LCApi.id("widget/checkbox_on")),checkboxSprite(LCApi.id("widget/checkbox_off")),10,10);

    public static WidgetSprites hoveredSprites(Identifier sprite) {
        Identifier hovered = sprite.withSuffix("_hovered");
        return new WidgetSprites(sprite,sprite,hovered,hovered);
    }

    public static WidgetSprites checkboxSprite(Identifier sprite) {
        Identifier hovered = sprite.withSuffix("_hovered");
        //For check-boxes, always use the "normal" sprite if hovered when disabled
        return new WidgetSprites(sprite,sprite,hovered,sprite);
    }

}