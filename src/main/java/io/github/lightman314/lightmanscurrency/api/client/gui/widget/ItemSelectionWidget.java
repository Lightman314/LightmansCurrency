package io.github.lightman314.lightmanscurrency.api.client.gui.widget;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.ScreenHelper;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.SimpleSizedSprite;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.SizedSprite;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.IScrollable;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollArea;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.VerticalScrollBar;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.helpers.item_selection.ItemSelectionFilter;
import io.github.lightman314.lightmanscurrency.api.helpers.item_selection.ItemSelectionHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.features.trader.item.TradeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ItemSelectionWidget extends AbstractMultiWidget implements IScrollable.WithBuiltInListener {

    public static final SizedSprite STACK_SIZE_SPRITE = new SimpleSizedSprite(LCApi.id("widget/stack_size_area"),18,18);

    private int scroll = 0;
    private int stackCount = 1;

    private final int columns;
    private final int rows;
    private final int visibleEntries;

    private final ScreenPosition searchOffset;
    private final ScreenPosition stackSizeOffset;

    private List<ItemStack> searchResultItems = new ArrayList<>();
    private String searchString;

    private final Consumer<ItemStack> listener;
    @Nullable
    private final Identifier filter;

    private TextBoxWrapper<String> searchBox = null;

    protected ItemSelectionWidget(Builder builder) {
        super(builder);
        this.columns = builder.columns;
        this.rows = builder.rows;
        this.visibleEntries = this.columns * this.rows;
        this.searchOffset = Objects.requireNonNullElse(builder.searchOffset,ScreenPosition.of(this.width - 90,-13));
        this.stackSizeOffset = Objects.requireNonNullElse(builder.stackSizeOffset,ScreenPosition.of(this.width + 15,0));

        this.listener = builder.listener;
        this.filter = builder.filter;

        ItemSelectionHelper.assertDataIsLoaded(this::refreshSearch);

        this.modifySearch(builder.oldWidget != null ? builder.oldWidget.searchString : "");
        if(builder.oldWidget != null) {
            this.setScroll(builder.oldWidget.scroll);
            this.searchBox = builder.oldWidget.searchBox;
            this.validateScroll();
        }

    }

    private List<ItemStack> getFilteredItems() { return new ArrayList<>(ItemSelectionHelper.getFilteredItems(this.filter)); }

    @Override
    protected void addEarlyChildren(ScreenArea area) { }


    @Override
    protected void addLateChildren(ScreenArea area) {

        //Build as late children so that they render on top of this widgets BG

        this.searchBox = this.addChild(TextBoxWrapper.stringBuilder()
                .atPos(area.pos.offset(this.searchOffset).offset(2,2))
                .ofWidth(79)
                .ofHeight(9)
                .noBorder()
                .withMaxLength(32)
                .visible(this::isVisible)
                .withTextColor(0xFFFFFFFF)
                .withOldWidget(this.searchBox)
                .withHandler(this::modifySearch).build());

        this.addChild(ScrollArea.builder()
                .ofArea(area.offsetPosition(this.stackSizeOffset)
                        .ofSize(18,18))
                .active(this::isVisible)
                .withYConsumer(this::stackCountScroll)
                .build());

        this.addChild(VerticalScrollBar.builder(this)
                .rightOf(this)
                .withKnob(VerticalScrollBar.SMALL_KNOB)
                .visible(this::isVisible)
                .build());
    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui,ScreenArea area) {
        int index = this.scroll * this.columns;
        for(int y = 0; y < this.rows ; ++y) {
            int yPos = y * 18;
            for(int x = 0; x < this.columns; ++x) {
                int xPos = x * 18;
                boolean isHovered = ScreenArea.of(xPos,yPos,18,18).offsetPosition(area.pos).isInArea(gui.getMousePos());
                //Render the slot background
                gui.blitSlot(xPos,yPos);
                //Render hovered background
                if(isHovered)
                    gui.blitSlotHighlightBack(xPos,yPos);
                //Render the slots item
                ItemStack stack = this.quantityFixedItem(index++);
                gui.item(stack,xPos + 1,yPos + 1);
                //Render hovered foreground
                if(isHovered)
                    gui.blitSlotHighlightFront(xPos,yPos);
                //Render the items tooltip (if hovered)
                if(!stack.isEmpty() && isHovered)
                    gui.renderItemTooltipAtMouse(stack);
            }
        }

        //Render the search field
        gui.blitSprite(LCSprites.SEARCH_FIELD,this.searchOffset.x,this.searchOffset.y,90,12);
        //Render the search icon
        gui.blitSprite(LCSprites.SEARCH_ICON,this.searchOffset.x - 11,this.searchOffset.y - 1);

        //Render the count scroll input
        gui.blitSprite(STACK_SIZE_SPRITE,this.stackSizeOffset.x,this.stackSizeOffset.y);
        //Render the count scroll tooltip (if hovered)
        if(ScreenArea.of(this.stackSizeOffset.offset(area.pos),18,18).isInArea(gui.getMousePos()))
            gui.renderTooltipAtMouse(TradeItem.TOOLTIP_ITEM_EDIT_SCROLL.get());

    }

    @Override
    public int getScroll() { return this.scroll; }
    @Override
    public void setScroll(int scroll) { this.scroll = scroll; }
    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(this.searchResultItems.size(),this.columns,this.visibleEntries); }

    public void refreshSearch() { this.modifySearch(this.searchString); }

    public void modifySearch(String newSearch) {
        this.searchString = newSearch.toLowerCase();

        if(!this.searchString.isEmpty()) {
            this.searchResultItems = new ArrayList<>();
            for(ItemStack stack : this.getFilteredItems()) {
                //Check the item id
                if(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().contains(this.searchString)) {
                    this.searchResultItems.add(stack);
                }
                else {
                    //Search the tooltips (this should also include the enchantments, etc.)
                    List<Component> tooltips = Screen.getTooltipFromItem(Minecraft.getInstance(),stack);
                    for(Component line : tooltips) {
                        if(line.getString().toLowerCase().contains(this.searchString)) {
                            this.searchResultItems.add(stack);
                            break;
                        }
                    }
                }
            }
        }
        else
            this.searchResultItems = this.getFilteredItems();

        //Validate the scroll
        this.validateScroll();

    }

    private ItemStack quantityFixedItem(int index) {
        if(index < 0 || index >= this.searchResultItems.size())
            return ItemStack.EMPTY;
        ItemStack stack = this.searchResultItems.get(index).copy();
        stack.setCount(Math.min(this.stackCount,stack.getMaxStackSize()));
        return stack;
    }

    private int isMouseOverSlot(ScreenPosition mousePos) {
        if(!this.isVisible() || !this.getArea().isInArea(mousePos))
            return -1;

        ScreenPosition localPos = mousePos.offset(this.getPosition().invert());
        int column = localPos.x / 18;
        int row = localPos.y / 18;
        return (row * column) + column;
    }

    @Override
    protected boolean isValidClickButton(MouseButtonInfo buttonInfo) { return buttonInfo.button() == 0; }

    @Override
    public void onClick(MouseButtonEvent event,boolean doubleClick) {
        if(!this.visible)
            return;
        int hoveredSlot = this.isMouseOverSlot(ScreenHelper.getMousePos(event));
        if(hoveredSlot >= 0) {
            hoveredSlot += this.scroll * this.columns;
            ItemStack stack = this.quantityFixedItem(hoveredSlot);
            LightmansCurrency.LogDebug("Item Selection detected a click on " + stack.getHoverName().getString());
            if(!stack.isEmpty()) {
                this.listener.accept(stack);
            }
        }
    }

    public void stackCountScroll(double delta) {
        if(delta > 0) {
            if(this.stackCount < 64)
                this.stackCount++;
        }
        else if(delta < 0) {
            if(this.stackCount > 1)
                this.stackCount--;
        }
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder extends AbstractBuilder<Builder,ItemSelectionWidget> {

        private Builder() { this.withSize(9,3); }

        private int columns;
        private int rows;

        private Consumer<ItemStack> listener = s -> {};
        @Nullable
        private ItemSelectionWidget oldWidget = null;
        @Nullable
        private Identifier filter = null;

        private ScreenPosition searchOffset = null;
        private ScreenPosition stackSizeOffset = null;

        public Builder withRows(int rows) {
            this.rows = rows;
            this.setHeight(this.rows * 18);
            return this;
        }
        public Builder withColumns(int columns) {
            this.columns = columns;
            this.setWidth(this.columns * 18);
            return this;
        }
        public Builder withSize(int colums,int rows) { return this.withColumns(colums).withRows(rows); }

        public Builder withListener(Consumer<ItemStack> listener) { this.listener = listener; return this; }
        public Builder withFilter(Identifier filter) { this.filter = filter; return this; }
        public Builder withFilter(ItemSelectionFilter filter) { this.filter = filter.key(); return this; }
        public Builder withOldWidget(@Nullable ItemSelectionWidget oldWidget) { this.oldWidget = oldWidget; return this; }

        public Builder withSearchOffset(int offsetX,int offsetY) { return this.withSearchOffset(ScreenPosition.of(offsetX,offsetY)); }
        public Builder withSearchOffset(ScreenPosition searchOffset) { this.searchOffset = searchOffset; return this; }

        public Builder withStackSizeOffset(int offsetX,int offsetY) { return this.withStackSizeOffset(ScreenPosition.of(offsetX,offsetY)); }
        public Builder withStackSizeOffset(ScreenPosition stackSizeOffset) { this.stackSizeOffset = stackSizeOffset; return this; }

        @Override
        protected Builder getSelf() { return this; }
        @Override
        public ItemSelectionWidget build() { return new ItemSelectionWidget(this); }

    }

}
