package io.github.lightman314.lightmanscurrency.api.client.gui.widget.button;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.IMoveableWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.WidgetFacing;
import io.github.lightman314.lightmanscurrency.api.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.client.IconRenderer;

import java.util.function.Function;
import java.util.function.Supplier;

public class IconButton extends EasyButton implements IMoveableWidget {

    private final Function<IconButton,IconData> icon;
    protected IconButton(Builder builder) {
        super(builder);
        this.icon = builder.icon;
    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, ScreenArea area) {
        gui.blitButtonSprite(0,0,this.getWidth(),this.getHeight(),this.isActive(),this.isHoveredOrFocused());
        //Render normal button background
        IconData i = this.icon.apply(this);
        if(i != null)
            IconRenderer.extractState(gui,i,2,2);
    }

    public static Builder builder() { return new Builder(); }

    @Override
    public void move(ScreenPosition position, WidgetFacing facing) { this.setPosition(position); }

    public static final class Builder extends ButtonBuilder<Builder,IconButton>
    {

        private Function<IconButton,IconData> icon = b -> IconData.empty();

        private Builder() { super(20,20); }

        public Builder withIcon(IconData icon) { return this.withIcon(b -> icon); }
        public Builder withIcon(Supplier<IconData> icon) { return this.withIcon(b -> icon.get()); }
        public Builder withIcon(Function<IconButton,IconData> icon) { this.icon = icon; return this; }

        @Override
        protected Builder getSelf() { return this; }
        @Override
        public IconButton build() { return new IconButton(this); }
    }

}