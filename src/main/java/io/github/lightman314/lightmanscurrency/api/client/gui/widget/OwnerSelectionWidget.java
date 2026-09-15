package io.github.lightman314.lightmanscurrency.api.client.gui.widget;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.OwnerSelectButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.IScrollable;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollArea;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.VerticalScrollBar;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.interfaces.IOwnerHolder;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.PotentialOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.PotentialOwnerList;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class OwnerSelectionWidget extends AbstractMultiWidget implements IScrollable {

    private final Supplier<Owner> selectedOwner;
    private final Consumer<Owner> handler;
    private final int rows;

    public final PotentialOwnerList list;
    private TextBoxWrapper<String> searchBox;
    private int scroll = 0;

    private OwnerSelectionWidget(Builder builder) {
        super(builder);
        this.selectedOwner = builder.selectedOwner;
        this.handler = builder.handler;
        this.rows = builder.rows;
        if(builder.oldWidget != null) {
            this.scroll = builder.oldWidget.scroll;
            this.searchBox = builder.oldWidget.searchBox;
            this.list = builder.oldWidget.list;
        }
        else
            this.list = new PotentialOwnerList(builder.player,this.selectedOwner,builder.filter);
    }

    @Override
    protected void renderTickInternal(ScreenPosition mousePos) {
        this.list.tick();
    }

    @Override
    protected void addEarlyChildren(ScreenArea area) { }

    @Override
    protected void addLateChildren(ScreenArea area) {
        //Search Box
        this.searchBox = this.addChild(TextBoxWrapper.stringBuilder()
                .atPos(area.pos.offset(this.width - 99,2))
                .withOldWidget(this.searchBox)
                .ofSize(79,9)
                .noBorder()
                .withHandler(this::modifySearch)
                .visible(this::isVisible)
                .build());
        //Scroll Bar
        this.addChild(VerticalScrollBar.builder(this)
                .atPos(area.pos.offset(area.width,12))
                .ofHeight(area.height - 12)
                .visible(this::isVisible)
                .build());
        this.addChild(ScrollArea.builder()
                .ofArea(this.getArea())
                .withListener(this.buildScrollListener())
                .build());

        for(int i = 0; i < this.rows; ++i) {
            final int index = i;
            this.addChild(OwnerSelectButton.builder()
                    .atPos(area.pos.offset(0,12 + i * OwnerSelectButton.HEIGHT))
                    .ofWidth(area.width)
                    .onPress(() -> this.setOwner(index))
                    .selected(this.selectedOwner)
                    .owner(() -> this.getOwner(index))
                    .visible(this::isVisible)
                    .build());
        }
    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, ScreenArea area) {
        gui.blitSprite(LCSprites.SEARCH_FIELD,this.width - 90,0,90,12);
    }

    private void modifySearch(String newSearch) {
        this.list.updateCache(newSearch);
        this.validateScroll();
    }

    @Nullable
    private PotentialOwner getOwner(int buttonIndex) {
        List<PotentialOwner> list = this.list.getOwners();
        int index = buttonIndex + this.scroll;
        if(index >= 0 && index < list.size())
            return list.get(index);
        return null;
    }

    private void setOwner(int buttonIndex) {
        PotentialOwner owner = this.getOwner(buttonIndex);
        if(owner != null)
            this.handler.accept(owner.asOwner());
    }

    @Override
    public int getScroll() { return this.scroll; }
    @Override
    public void setScroll(int scroll) { this.scroll = scroll; }
    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(this.rows,this.list.getOwners().size()); }

    public static Builder builder() { return new Builder(); }

    public static class Builder extends AbstractBuilder<Builder,OwnerSelectionWidget> {

        private Builder() { super(100,12 + OwnerSelectButton.HEIGHT); }

        @Override
        protected Builder getSelf() { return this; }

        private Player player = Minecraft.getInstance().player;
        private int rows = 1;
        private Supplier<Owner> selectedOwner = () -> null;
        private Consumer<Owner> handler = o -> {};
        private Predicate<PotentialOwner> filter = o -> true;
        @Nullable
        private OwnerSelectionWidget oldWidget = null;

        public Builder ofWidth(int width) { this.setWidth(width); return this; }
        public Builder rows(int rows) { this.rows = rows; this.setHeight(12 + rows * OwnerSelectButton.HEIGHT); return this; }
        public Builder selectedHolder(Supplier<? extends IOwnerHolder> selectedOwner) {
            return this.selected(() -> {
                IOwnerHolder holder = selectedOwner.get();
                return holder == null ? null : holder.getValidOwner();
            });
        }
        public Builder selected(Supplier<Owner> selectedOwner) { this.selectedOwner = selectedOwner; return this; }
        public Builder handler(Consumer<Owner> handler) { this.handler = handler; return this; }
        public Builder filter(Predicate<PotentialOwner> filter) { this.filter = filter; return this; }
        public Builder forPlayer(Player player) { this.player = player; return this; }
        public Builder oldWidget(@Nullable OwnerSelectionWidget oldWidget) { this.oldWidget = oldWidget; return this; }

        @Override
        public OwnerSelectionWidget build() { return new OwnerSelectionWidget(this); }

    }

}
