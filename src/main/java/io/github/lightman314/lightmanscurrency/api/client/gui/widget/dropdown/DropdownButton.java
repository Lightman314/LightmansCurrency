package io.github.lightman314.lightmanscurrency.api.client.gui.widget.dropdown;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.FancyButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.ILateRenderer;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.IMoveableWidget;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class DropdownButton extends FancyButton implements ILateRenderer, IMoveableWidget.WithoutFacing, IMouseListener {

    private final WidgetSprites sprites;
    private final DropdownOption option;
    private DropdownButton(Builder builder) {
        super(builder);
        this.sprites = builder.sprite;
        this.option = builder.option;
    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, ScreenArea area) { }

    @Override
    public void extractLateRender(FancyGuiExtractor gui) {
        if(!this.isVisible())
            return;
        gui.push(this.getPosition());
        gui.blitSprite(this.sprites.get(this.active,this.isHovered),0,0,this.width,this.height,this.getSpriteColor());
        DropdownWidget.extractOption(gui,this.getArea(),option,2);
        gui.pop();
        this.handleCursor(gui.getGui());
    }

    protected static Builder builder() { return new Builder(); }

    @Override
    public boolean onMouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if(this.visible)
            return this.mouseClicked(event, doubleClick);
        return false;
    }

    protected static final class Builder extends ButtonBuilder<Builder,DropdownButton> {

        private Builder() { super(20,DropdownWidget.HEIGHT); }

        private WidgetSprites sprite = DropdownWidget.DEFAULT_BUTTON_SPRITE;
        private DropdownOption option = new DropdownOption(() -> false, Component.empty());

        @Override
        protected Builder getSelf() { return this; }

        public Builder ofWidth(int width) { this.setWidth(width); return this; }

        public Builder withSprite(WidgetSprites sprite) { this.sprite = sprite; return this; }
        public Builder forOption(DropdownOption option) { this.option = option; return this; }

        @Override
        public DropdownButton build() { return new DropdownButton(this); }

    }

}
