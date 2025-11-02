package io.github.lightman314.lightmanscurrency.api.misc.client.sprites;

import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin.*;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;

import java.util.function.Supplier;

public class SpriteUtil {

    /// Buttons
    //Gray Button
    public static final FlexibleSizeSprite BUTTON_GRAY = new NineSliceSprite(SpriteSource.createTop(VersionUtil.lcResource("common/widgets/button_gray"),128,16),3);
    public static final FlexibleSizeSprite BUTTON_GRAY_HOVERED = new NineSliceSprite(SpriteSource.createBottom(VersionUtil.lcResource("common/widgets/button_gray"),128,16),3);
    public static FixedSizeSprite createButtonGray(int width, int height) { return WidgetStateSprite.lazyHoverable(BUTTON_GRAY,BUTTON_GRAY_HOVERED,width,height); }

    //Trade Green Button
    public static final FlexibleSizeSprite BUTTON_TRADE_GREEN = new NineSliceSprite(SpriteSource.createTop(VersionUtil.lcResource("common/widgets/button_trade_green"),128,16),3);
    public static final FlexibleSizeSprite BUTTON_TRADE_GREEN_HOVERED = new NineSliceSprite(SpriteSource.createBottom(VersionUtil.lcResource("common/widgets/button_trade_green"),128,16),3);
    public static FixedSizeSprite createButtonTradeGreen(int width,int height) { return WidgetStateSprite.lazyHoverable(BUTTON_TRADE_GREEN,BUTTON_TRADE_GREEN_HOVERED,width,height); }

    //Gray Button
    public static final FlexibleSizeSprite BUTTON_BROWN = new NineSliceSprite(SpriteSource.createTop(VersionUtil.lcResource("common/widgets/button_brown"),128,20),3);
    public static final FlexibleSizeSprite BUTTON_BROWN_HOVERED = new NineSliceSprite(SpriteSource.createBottom(VersionUtil.lcResource("common/widgets/button_brown"),128,20),3);
    public static FixedSizeSprite createButtonBrown(int width,int height) { return WidgetStateSprite.lazyHoverable(BUTTON_BROWN,BUTTON_BROWN_HOVERED,width,height); }

    //Green Button
    public static final FlexibleSizeSprite BUTTON_GREEN = new NineSliceSprite(SpriteSource.createTop(VersionUtil.lcResource("common/widgets/button_green"),128,20),3);
    public static final FlexibleSizeSprite BUTTON_GREEN_HOVERED = new NineSliceSprite(SpriteSource.createBottom(VersionUtil.lcResource("common/widgets/button_green"),128,20),3);
    public static FixedSizeSprite createButtonGreen(int width,int height) { return WidgetStateSprite.lazyHoverable(BUTTON_GREEN,BUTTON_GREEN_HOVERED,width,height); }

    //Arrow
    public static final FixedSizeSprite ARROW_GRAY = new NormalSprite(SpriteSource.createTop(VersionUtil.lcResource("common/widgets/misc_arrow"),22,18));
    public static final FixedSizeSprite ARROW_WHITE = new NormalSprite(SpriteSource.createBottom(VersionUtil.lcResource("common/widgets/misc_arrow"),22,18));

    //Quick Insert/Extract
    public static final FixedSizeSprite BUTTON_QUICK_INSERT = WidgetStateSprite.lazyHoverable(VersionUtil.lcResource("common/widgets/button_quick_insert"),10,10);
    public static final FixedSizeSprite BUTTON_QUICK_EXTRACT = WidgetStateSprite.lazyHoverable(VersionUtil.lcResource("common/widgets/button_quick_extract"),10,10);

    //Plus/Minus
    public static final FixedSizeSprite BUTTON_SIGN_PLUS = WidgetStateSprite.lazyHoverable(VersionUtil.lcResource("common/widgets/button_sign_plus"),10,10);
    public static final FixedSizeSprite BUTTON_SIGN_MINUS = WidgetStateSprite.lazyHoverable(VersionUtil.lcResource("common/widgets/button_sign_minus"),10,10);

    //Checkbox
    private static final FixedSizeSprite BUTTON_CHECKBOX_ON = WidgetStateSprite.lazyHoverable(VersionUtil.lcResource("common/widgets/button_checkbox_on"),10,10);
    private static final FixedSizeSprite BUTTON_CHECKBOX_OFF = WidgetStateSprite.lazyHoverable(VersionUtil.lcResource("common/widgets/button_checkbox_off"),10,10);
    public static Supplier<FixedSizeSprite> createCheckbox(Supplier<Boolean> onState) { return () -> onState.get() ? BUTTON_CHECKBOX_ON : BUTTON_CHECKBOX_OFF; }

    //Toggle
    private static final FixedSizeSprite BUTTON_TOGGLE_ON = new NormalSprite(SpriteSource.createTop(VersionUtil.lcResource("common/widgets/button_toggle"),8,18));
    private static final FixedSizeSprite BUTTON_TOGGLE_OFF = new NormalSprite(SpriteSource.createBottom(VersionUtil.lcResource("common/widgets/button_toggle"),8,18));
    public static Supplier<FixedSizeSprite> createNeutralToggle(Supplier<Boolean> onState) { return () -> onState.get() ? BUTTON_TOGGLE_ON : BUTTON_TOGGLE_OFF; }

    //Colored Toggle
    private static final FixedSizeSprite BUTTON_COLORED_TOGGLE_ON = WidgetStateSprite.lazyHoverable(VersionUtil.lcResource("common/widgets/button_colored_toggle_on"),8,18);
    private static final FixedSizeSprite BUTTON_COLORED_TOGGLE_OFF = WidgetStateSprite.lazyHoverable(VersionUtil.lcResource("common/widgets/button_colored_toggle_off"),8,18);
    public static Supplier<FixedSizeSprite> createColoredToggle(Supplier<Boolean> onState) { return () -> onState.get() ? BUTTON_COLORED_TOGGLE_ON : BUTTON_COLORED_TOGGLE_OFF; }

    //Colored X's
    public static final FixedSizeSprite BUTTON_RED_X = WidgetStateSprite.lazyHoverable(VersionUtil.lcResource("common/widgets/button_red_x"),10,10);
    public static final FixedSizeSprite BUTTON_GREEN_X = WidgetStateSprite.lazyHoverable(VersionUtil.lcResource("common/widgets/button_green_x"),10,10);

    //Big Arrows
    public static final FixedSizeSprite BUTTON_BIGARROW_UP = WidgetStateSprite.lazyHoverable(VersionUtil.lcResource("common/widgets/button_bigarrow_up"),20,10);
    public static final FixedSizeSprite BUTTON_BIGARROW_DOWN = WidgetStateSprite.lazyHoverable(VersionUtil.lcResource("common/widgets/button_bigarrow_down"),20,10);
    public static final FixedSizeSprite BUTTON_BIGARROW_LEFT = WidgetStateSprite.lazyHoverable(VersionUtil.lcResource("common/widgets/button_bigarrow_left"),10,20);
    public static final FixedSizeSprite BUTTON_BIGARROW_RIGHT = WidgetStateSprite.lazyHoverable(VersionUtil.lcResource("common/widgets/button_bigarrow_right"),10,20);

    /// Generic
    public static final FlexibleSizeSprite GENERIC_BACKGROUND = new NineSliceSprite(new SpriteSource(VersionUtil.lcResource("textures/gui/generic_background.png"),0,0,256,256,256,256),16);

    public static final FixedSizeSprite GENERIC_ALERT = new NormalSprite(SpriteSource.create(VersionUtil.lcResource("common/container/generic_alert"),16,16));
    public static final FixedSizeSprite GENERIC_INFO = new NormalSprite(SpriteSource.create(VersionUtil.lcResource("common/container/generic_info"),10,10));

    public static final FixedSizeSprite EMPTY_SLOT_NORMAL = new NormalSprite(SpriteSource.create(VersionUtil.lcResource("common/container/slot_normal"),18,18));
    public static final FixedSizeSprite EMPTY_SLOT_YELLOW = new NormalSprite(SpriteSource.create(VersionUtil.lcResource("common/container/slot_yellow"),18,18));
    public static final FixedSizeSprite EMPTY_SLOT_GREEN = new NormalSprite(SpriteSource.create(VersionUtil.lcResource("common/container/slot_green"),18,18));

    public static final FixedSizeSprite SEARCH_ICON = new NormalSprite(SpriteSource.create(VersionUtil.lcResource("common/container/search_icon"),11,14));
    public static final FlexibleWidthSprite SEARCH_FIELD = new HorizontalSliceSprite(SpriteSource.create(VersionUtil.lcResource("common/container/search_field"),105,12),4);

    public static final FixedSizeSprite SMALL_ARROW_UP = new NormalSprite(SpriteSource.create(VersionUtil.lcResource("common/container/small_arrow_up"),8,6));
    public static final FixedSizeSprite SMALL_ARROW_DOWN = new NormalSprite(SpriteSource.create(VersionUtil.lcResource("common/container/small_arrow_down"),8,6));
    public static final FixedSizeSprite SMALL_ARROW_LEFT = new NormalSprite(SpriteSource.create(VersionUtil.lcResource("common/container/small_arrow_left"),6,8));
    public static final FixedSizeSprite SMALL_ARROW_RIGHT = new NormalSprite(SpriteSource.create(VersionUtil.lcResource("common/container/small_arrow_right"),6,8));


}