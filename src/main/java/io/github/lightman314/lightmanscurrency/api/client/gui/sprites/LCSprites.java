package io.github.lightman314.lightmanscurrency.api.client.gui.sprites;

import io.github.lightman314.lightmanscurrency.api.LCApi;

import java.util.function.BooleanSupplier;

public final class LCSprites {
    private LCSprites() {}

    public static final SizedSprite.Template<BooleanSupplier> TOGGLE = DeferredSizedSprite.toggleSprite(LCApi.id("widgets/toggle_on"),LCApi.id("widgets/toggle_off"),8,18);
    public static final SizedSprite.Template<BooleanSupplier> TOGGLE_COLORED = DeferredSizedSprite.toggleAndHoverToggleSprite(LCApi.id("widgets/toggle_colored_on"),LCApi.id("widgets/toggle_colored_off"),8,18);

    public static final SizedSprite.Builder FREE_TOGGLE = WidgetContextSprite.hoverToggleSprite(LCApi.id("widget/free_toggle"),10,10);


}