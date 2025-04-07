package io.github.lightman314.lightmanscurrency.client.gui.widget;

import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.PotentialOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.PotentialOwnerList;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.OwnerSelectButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class OwnerSelectionWidget extends EasyWidgetWithChildren implements IScrollable {

    public static final ResourceLocation SEARCH_BOX_TEXTURE = VersionUtil.lcResource("textures/gui/item_edit.png");

    private final Supplier<OwnerData> currentOwner;
    private final Consumer<Owner> setOwner;
    private final int rows;

    public final PotentialOwnerList list;
    private EditBox searchBox;
    private int scroll = 0;

    private OwnerSelectionWidget(@Nonnull Builder builder)
    {
        super(builder);
        this.currentOwner = builder.selectedOwner;
        this.setOwner = builder.handler;
        this.rows = builder.rows;
        if(builder.oldWidget != null)
        {
            this.scroll = builder.oldWidget.scroll;
            this.searchBox = builder.oldWidget.searchBox;
            this.list = builder.oldWidget.list;
        }
        else
            this.list = new PotentialOwnerList(Minecraft.getInstance().player, this.currentOwner, builder.filter);
    }

    @Override
    protected void renderTick() { this.list.tick(); }

    @Override
    protected void renderWidget(@Nonnull EasyGuiGraphics gui) {
        //Render search box background
        gui.blit(SEARCH_BOX_TEXTURE, this.width - 90, 0, 18, 0, 90, 12);
    }

    @Override
    public void addChildren(@Nonnull ScreenArea area) {
        this.searchBox = this.addChild(new EditBox(Minecraft.getInstance().font, area.pos.x + this.width - 88, area.pos.y + 2, 79, 9, this.searchBox, EasyText.empty()));
        this.searchBox.setBordered(false);
        this.searchBox.setResponder(this::modifySearch);
        //Scroll Bar
        this.addChild(ScrollBarWidget.builder()
                .position(area.pos.offset(area.width,12))
                .height(area.height - 12)
                .scrollable(this)
                .addon(EasyAddonHelper.visibleCheck(this::isVisible))
                .build());
        this.addChild(ScrollListener.builder()
                .area(area)
                .listener(this)
                .build());
        for(int i = 0; i < this.rows; ++i)
        {
            final int index = i;
            this.addChild(OwnerSelectButton.builder()
                    .position(area.pos.offset(0,12 + i * OwnerSelectButton.HEIGHT))
                    .width(area.width)
                    .pressAction(() -> this.setOwner(index))
                    .selected(this.currentOwner)
                    .potentialOwner(() -> this.getOwner(index))
                    .visible(this::isVisible)
                    .build());
        }
    }

    private void modifySearch(@Nonnull String newSearch)
    {
        this.list.updateCache(newSearch);
        this.validateScroll();
    }

    @Nullable
    private PotentialOwner getOwner(int buttonIndex)
    {
        List<PotentialOwner> list = this.list.getOwners();
        int index = buttonIndex + this.scroll;
        if(index >= 0 && index < list.size())
            return list.get(index);
        return null;
    }

    private void setOwner(int buttonIndex)
    {
        PotentialOwner owner = this.getOwner(buttonIndex);
        if(owner != null)
            this.setOwner.accept(owner.asOwner());
    }

    @Override
    public int currentScroll() { return this.scroll; }

    @Override
    public void setScroll(int newScroll) { this.scroll = newScroll; }

    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(this.rows, this.list.getOwners().size()); }

    @Nonnull
    public static Builder builder() { return new Builder(); }

    @MethodsReturnNonnullByDefault
    @FieldsAreNonnullByDefault
    public static class Builder extends EasyBuilder<Builder>
    {
        private Builder() { super(100,12 + OwnerSelectButton.HEIGHT); }
        @Override
        protected Builder getSelf() { return this; }

        private int rows = 1;
        private Supplier<OwnerData> selectedOwner = () -> null;
        private Consumer<Owner> handler = o -> {};
        private Predicate<PotentialOwner> filter = o -> true;
        @Nullable
        private OwnerSelectionWidget oldWidget = null;

        public Builder width(int width) { this.changeWidth(width); return this; }
        public Builder rows(int rows) { this.rows = rows; this.changeHeight(12 + rows * OwnerSelectButton.HEIGHT); return this; }
        public Builder selected(Supplier<OwnerData> selectedOwner) { this.selectedOwner = selectedOwner; return this; }
        public Builder handler(Consumer<Owner> handler) { this.handler = handler; return this; }
        public Builder filter(Predicate<PotentialOwner> filter) { this.filter = filter; return this; }
        public Builder oldWidget(@Nullable OwnerSelectionWidget oldWidget) { this.oldWidget = oldWidget; return this; }

        public OwnerSelectionWidget build() { return new OwnerSelectionWidget(this); }

    }

}
