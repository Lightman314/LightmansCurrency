package io.github.lightman314.lightmanscurrency.api.client.gui.widget.button;

import com.google.common.base.Predicates;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.coins.atm.ATMExchangeButtonData;
import io.github.lightman314.lightmanscurrency.api.coins.atm.client.ATMIconRenderer;
import io.github.lightman314.lightmanscurrency.api.coins.atm.commands.ATMCommand;
import io.github.lightman314.lightmanscurrency.api.coins.atm.icons.ATMIconData;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;

import java.util.function.Consumer;
import java.util.function.Predicate;

public final class ATMExchangeButton extends FancyButton {

    public final ATMExchangeButtonData data;

    private final Predicate<ATMCommand> selected;

    private ATMExchangeButton(Builder builder) {
        super(builder);
        this.data = builder.data;
        this.selected = builder.selected;
    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, ScreenArea area) {
        //Render background to width
        boolean highlighted = this.isHovered != this.selected.test(this.data.command);
        gui.blitSprite(LCSprites.BUTTON_GRAY.get(this.active,highlighted),0,0,this.width,this.height,this.getSpriteColor());

        //Draw the icons
        for(ATMIconData icon : this.data.getIcons()) {
            try {
                ATMIconRenderer.renderIcon(area.pos,icon,gui,highlighted);
            } catch (Throwable t) {
                LightmansCurrency.LogError("Error rendering ATM Exchange Button icon",t);
            }
        }

    }

    public static Builder builder(ATMExchangeButtonData data) { return new Builder(data); }

    public static final class Builder extends ButtonBuilder<Builder,ATMExchangeButton> {

        private final ATMExchangeButtonData data;
        private Builder(ATMExchangeButtonData data) { super(data.width,data.height); this.data = data; }
        private Predicate<ATMCommand> selected = Predicates.alwaysFalse();

        public Builder withCorner(ScreenPosition corner) { return this.atPos(corner.offset(data.position)); }
        public Builder withHandler(Consumer<ATMCommand> handler) { return this.onPress(() -> handler.accept(this.data.command)); }
        public Builder selected(Predicate<ATMCommand> selected) { this.selected = selected; return this; }

        @Override
        protected Builder getSelf() { return this; }

        @Override
        public ATMExchangeButton build() { return new ATMExchangeButton(this); }
    }

}
