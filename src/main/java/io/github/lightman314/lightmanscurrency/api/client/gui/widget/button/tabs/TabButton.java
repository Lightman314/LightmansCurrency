package io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.tabs;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed.ClientMenuTab;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.FancyButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.IMoveableWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.WidgetFacing;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.FaceSensitiveSprites;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.client.IconRenderer;

import java.util.function.Function;
import java.util.function.Supplier;

public class TabButton extends FancyButton implements IMoveableWidget {

    public static final int SIZE = 20;

    public static final FaceSensitiveSprites DEFAULT = FaceSensitiveSprites.forTabs(LCApi.id("widget/tab/normal"));

    private final FaceSensitiveSprites sprites;
    private final Function<TabButton,IconData> icon;
    private WidgetFacing facing;
    protected TabButton(Builder builder) {
        super(builder);
        this.sprites = builder.sprites;
        this.icon = builder.icon;
        this.facing = builder.facing;
    }

    @Override
    public void move(ScreenPosition position, WidgetFacing facing) {
        this.setPosition(position);
        this.facing = facing;
    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, ScreenArea area) {
        gui.blitSprite(this.sprites.getSprite(this.isActive(),this.isHoveredOrFocused(),this.facing),0,0,20,20);
        IconRenderer.extractState(gui,this.icon.apply(this),2,2);
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder extends ButtonBuilder<Builder,TabButton>
    {

        private FaceSensitiveSprites sprites = DEFAULT;
        private Function<TabButton,IconData> icon;
        private WidgetFacing facing;

        private Builder() { super(SIZE,SIZE); }

        public Builder withFacing(WidgetFacing facing) { this.facing = facing; return this; }

        public Builder withSprites(FaceSensitiveSprites sprites) { this.sprites = sprites; return this; }

        public Builder withIcon(IconData icon) { return this.withIcon(b -> icon); }
        public Builder withIcon(Supplier<IconData> icon) { return this.withIcon(b -> icon.get()); }
        public Builder withIcon(Function<TabButton,IconData> icon) { this.icon = icon; return this; }

        public Builder forTab(ClientMenuTab<?,?,?,?> tab) {
            return this.withIcon(tab::getIcon)
                    .tooltip(TooltipSource.deferredSingle(tab::getName,true))
                    .visible(tab::isVisible);
        }

        @Override
        protected Builder getSelf() { return this; }
        @Override
        public TabButton build() { return new TabButton(this); }
    }

}