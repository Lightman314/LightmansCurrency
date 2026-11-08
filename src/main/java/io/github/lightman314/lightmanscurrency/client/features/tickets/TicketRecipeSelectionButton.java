package io.github.lightman314.lightmanscurrency.client.features.tickets;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.FancyButton;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

public class TicketRecipeSelectionButton extends FancyButton {

    public static final WidgetSprites SPRITES = new WidgetSprites(LCApi.id("container/ticket_station/recipe"),LCApi.id("container/ticket_station/recipe_selected"),LCApi.id("container/ticket_station/recipe_highlighted"));

    private final Supplier<ItemStack> itemDisplay;

    protected TicketRecipeSelectionButton(Builder builder) {
        super(builder);
        this.itemDisplay = builder.itemDisplay;
    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, ScreenArea area) {
        ItemStack display = this.itemDisplay.get();
        if(display.isEmpty())
            return;
        //Draw the background
        gui.blitSprite(SPRITES.get(this.active,this.isHovered),area);
        //Draw the item
        gui.item(display,0,1);
        //Add the tooltip
        if(this.isHovered)
            gui.renderPositionedTooltip(List.of(display.getHoverName()),this.getTooltipPositioner());
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder extends ButtonBuilder<Builder,TicketRecipeSelectionButton> {

        private Builder() { super(16,18); }

        private Supplier<ItemStack> itemDisplay = () -> ItemStack.EMPTY;

        @Override
        protected Builder getSelf() { return this; }

        public Builder withDisplay(Supplier<ItemStack> itemDisplay) { this.itemDisplay = itemDisplay; return this; }

        @Override
        public TicketRecipeSelectionButton build() { return new TicketRecipeSelectionButton(this); }

    }

}
