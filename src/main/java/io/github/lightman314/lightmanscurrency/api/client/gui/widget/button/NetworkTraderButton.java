package io.github.lightman314.lightmanscurrency.api.client.gui.widget.button;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.icon.client.IconRenderer;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IDisplayNode;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class NetworkTraderButton extends FancyButton {

    public static final int WIDTH = 146;
    public static final int HEIGHT = 30;

    private TraderData traderCache = null;

    private final Supplier<TraderData> trader;
    private final BooleanSupplier selected;
    @Nullable
    private final Player player;
    protected NetworkTraderButton(Builder builder) {
        super(builder);
        this.trader = builder.traderSource;
        this.selected = builder.selected;
        this.player = builder.player;
    }

    @Override
    protected void renderTickInternal(ScreenPosition mousePos) {
        this.traderCache = this.trader.get();
        this.visible = this.traderCache != null && this.visible;
    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, ScreenArea area) {
        if(this.traderCache == null)
            return;

        //Draw Button BG
        WidgetSprites sprite = this.selected.getAsBoolean() ? LCSprites.BUTTON_GREEN : LCSprites.BUTTON_BROWN;
        gui.blitSprite(sprite.get(this.active,this.isHovered),0,0,area.width,area.height,this.getSpriteColor());

        //Draw the icon
        IconRenderer.extractState(gui,IDisplayNode.getTraderIcon(this.traderCache),4,7);

        //Draw the name & owner of the trader
        Optional<Integer> color = IDisplayNode.getTraderNameColor(this.traderCache);
        gui.textWithScrollingOverflow(IDisplayNode.getTraderName(this.traderCache),24,6,area.width - 26,color.orElse(0xFF404040),false);
        gui.textWithScrollingOverflow(this.traderCache.getOwner().getName(),24,16,area.width - 26,0xFF404040,false);

    }

    @Override
    @Nullable
    protected List<Component> collectTooltips(ScreenPosition mousePos) {
        if(this.traderCache != null)
            return IDisplayNode.getTerminalInfo(this.player,this.traderCache);
        return null;
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder extends ButtonBuilder<Builder,NetworkTraderButton> {

        private Builder() { super(WIDTH,HEIGHT); this.visible(() -> true); }

        private Supplier<TraderData> traderSource = () -> null;
        private BooleanSupplier selected = () -> false;
        private Player player = null;

        public Builder forTrader(Supplier<TraderData> traderSource) { this.traderSource = traderSource; return this; }
        public Builder selected(BooleanSupplier selected) { this.selected = selected; return this; }
        public Builder forPlayer(Player player) { this.player = player; return this; }

        @Override
        protected Builder getSelf() { return this; }
        @Override
        public NetworkTraderButton build() { return new NetworkTraderButton(this); }

    }

}
