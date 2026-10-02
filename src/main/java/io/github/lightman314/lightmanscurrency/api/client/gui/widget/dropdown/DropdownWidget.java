package io.github.lightman314.lightmanscurrency.api.client.gui.widget.dropdown;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.SizedSprite;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.WidgetContextSprite;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.AbstractMultiWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.FancyWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.WidgetPositioner;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollArea;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.VerticalScrollBar;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.text.TextEntryBundle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class DropdownWidget extends AbstractMultiWidget.EarlyChildren implements IMouseListener {

    public static final int HEIGHT = 12;
    public static final SizedSprite.Builder DEFAULT_ARROW_SPRITE = WidgetContextSprite.hoverToggleSprite(LCApi.id("widget/dropdown"),11,12);
    public static final WidgetSprites DEFAULT_BG_SPRITE = LCSprites.BUTTON_GRAY;
    public static final WidgetSprites DEFAULT_BUTTON_SPRITE = LCSprites.BUTTON_BROWN;

    private boolean open = false;
    private int currentlySelected;
    public int getCurrentlySelected() { return this.currentlySelected; }
    public void setCurrentlySelected(int currentlySelected) { this.currentlySelected = Math.clamp(currentlySelected,-1,this.options.size()); }

    private final WidgetSprites backgroundSprite;
    private final WidgetSprites buttonSprites;
    private final SizedSprite arrowSprite;
    private final List<DropdownOption> options;
    private final Consumer<Integer> onSelect;
    private final Function<Integer,Boolean> optionActive;
    private final int maxDisplay;

    @Nullable
    private FancyWidget scrollBar;
    private List<DropdownButton> optionButtons = new ArrayList<>();

    public DropdownWidget(Builder builder) {
        super(builder);
        this.currentlySelected = builder.currentlySelected;
        this.options = ImmutableList.copyOf(builder.options);
        this.onSelect = builder.onSelect;
        this.optionActive = builder.activeCheck;
        this.backgroundSprite = builder.backgroundSprite;
        this.buttonSprites = builder.buttonSprite;
        this.arrowSprite = builder.arrowSprite;
        this.maxDisplay = builder.maxDisplay;
    }

    @Override
    protected void addEarlyChildren(ScreenArea area) {
        WidgetPositioner positioner = this.addChild(WidgetPositioner.below(area.cornerBottomLeft(),HEIGHT,this.maxDisplay > 0 ? this.maxDisplay : Integer.MAX_VALUE));
        if(this.maxDisplay > 0)
        {
            this.addChild(ScrollArea.builder()
                    .ofArea(this.getArea())
                    .forScrollable(positioner)
                    .build());
            this.scrollBar = this.addChild(VerticalScrollBar.builder(positioner)
                    .atPos(area.cornerTopRight())
                    .ofHeight((this.options.size() + 1) * HEIGHT)
                    .build());
        }
        this.optionButtons = new ArrayList<>();
        for(int i = 0; i < this.options.size(); ++i)
        {
            final int index = i;
            DropdownOption option = this.options.get(i);
            DropdownButton button = this.addChild(DropdownButton.builder()
                    .forOption(option)
                    .withSprite(this.buttonSprites)
                    .ofWidth(this.width)
                    .visible(() -> this.isButtonVisible(option))
                    .onPress(() -> this.onSelect(index))
                    .active(() -> optionActive.apply(index) && index != this.currentlySelected)
                    .build());
            this.optionButtons.add(button);
            positioner.addWidgets(button);
        }
    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui,ScreenArea area) {
        //Render the background
        gui.blitSprite(this.backgroundSprite.get(this.active,this.isHovered),0,0,area.width,HEIGHT,this.getSpriteColor());
        //Render the arrow
        int arrowWidth = this.arrowSprite.width();
        gui.blitSprite(this.arrowSprite,area.width - arrowWidth,0,this.getFGColor(),this.active,this.isHovered);

        //Draw the option
        DropdownOption option = this.options.get(this.currentlySelected);
        extractOption(gui,area,option,arrowWidth);

    }

    public static void extractOption(FancyGuiExtractor gui,ScreenArea area,DropdownOption option,int rightSpacing) {
        //Draw the option sprite
        int textX = 2;
        if(option.icon() != null)
        {
            gui.blitSprite(option.icon(),2,2,8,8);
            textX += 10;
        }
        //Draw the option text
        int availableWidth = area.width - textX - rightSpacing;
        Component label = option.label();
        //Render the text normally if it fits within the space
        gui.textWithScrollingOverflow(label,textX,2,availableWidth,0xFF404040,false);
    }

    @Override
    public boolean onMouseClicked(MouseButtonEvent event,boolean doubleClick) {
        if(this.active && this.visible)
        {
            if(this.isMouseOver(event.x(),event.y()) && this.isValidClickButton(event.buttonInfo()))
            {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
                this.open = !this.open;
                return true;
            }
            //Close the dropdown if clicked outside the child widgets
            else if(this.open && !this.isOverChild(event.x(),event.y()))
                this.open = false;
        }
        return false;
    }

    private boolean isOverChild(double mouseX,double mouseY) {
        for(DropdownButton b : this.optionButtons)
        {
            if(b.isMouseOver(mouseX,mouseY))
                return true;
        }
        return this.scrollBar != null && this.scrollBar.visible && this.scrollBar.isMouseOver(mouseX, mouseY);
    }

    private void onSelect(int index) {
        if(index < 0 || index >= this.optionButtons.size())
            return;
        this.currentlySelected = index;
        this.onSelect.accept(index);
        this.open = false;
    }

    @Override
    protected boolean isValidClickButton(MouseButtonInfo buttonInfo) { return buttonInfo.button() == 0; }

    @Override
    public void playDownSound(SoundManager soundManager) {
        playButtonClickSound(soundManager);
    }

    private boolean isButtonVisible(DropdownOption option) { return this.visible && this.open && option.isVisible(); }

    public static Builder builder() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<Builder,DropdownWidget> {

        private Builder() { super(20,HEIGHT); }

        private int currentlySelected = 0;
        private WidgetSprites backgroundSprite = DEFAULT_BG_SPRITE;
        private WidgetSprites buttonSprite = DEFAULT_BUTTON_SPRITE;
        private SizedSprite arrowSprite = DEFAULT_ARROW_SPRITE.buildSprite();
        private final List<DropdownOption> options = new ArrayList<>();
        private Consumer<Integer> onSelect = i -> {};
        private Function<Integer,Boolean> activeCheck = i -> true;
        private int maxDisplay = 0;

        public Builder ofWidth(int width) { this.setWidth(width); return this; }

        public Builder withOption(Component entry) { return this.withOption(new DropdownOption(entry)); }
        public Builder withOption(TextEntry entry) { return this.withOption(entry.get()); }
        public Builder withOption(Component entry, Identifier icon) { return this.withOption(new DropdownOption(entry,icon)); }
        public Builder withOption(BooleanSupplier visible, Component entry) { return this.withOption(new DropdownOption(visible,entry)); }
        public Builder withOption(BooleanSupplier visible, Component entry, Identifier icon) { return this.withOption(new DropdownOption(visible,entry,icon)); }
        public Builder withOption(DropdownOption entry) { this.options.add(entry); return this; }

        public Builder withSimpleOptions(List<Component> entries) { return this.withOptions(entries.stream().map(DropdownOption::new).toList()); }
        public Builder withOptions(List<DropdownOption> entries) { this.options.addAll(entries); return this; }

        public <T extends Enum<T>> Builder enumOptions(TextEntryBundle<T> bundle,Class<T> type) { return this.enumOptions(bundle,type.getEnumConstants()); }
        public <T extends Enum<T>> Builder enumOptions(TextEntryBundle<T> bundle,T[] allValues) {
            for(T val : allValues)
                this.withOption(bundle.get(val));
            return this;
        }

        public Builder withVisibleOptions(int visibleOptions) { this.maxDisplay = visibleOptions; return this; }

        public Builder withCurrentlySelected(int selected) { this.currentlySelected = selected; return this; }
        public Builder withHandler(Consumer<Integer> action) { this.onSelect = action; return this; }

        public Builder withBackgroundSprite(WidgetSprites sprite) { this.backgroundSprite = sprite; return this; }
        public Builder withButtonSprite(WidgetSprites sprite) { this.buttonSprite = sprite; return this; }
        public Builder withArrowSprite(SizedSprite sprite) { this.arrowSprite = sprite; return this; }
        public Builder withArrowSprite(SizedSprite.Builder sprite) { return this.withArrowSprite(sprite,null); }
        public <T> Builder withArrowSprite(SizedSprite.Template<T> sprite,T arg) { return this.withArrowSprite(sprite.buildSprite(arg)); }
        public Builder withSprites(WidgetSprites dropdownSprite,WidgetSprites buttonSprite) { return this.withBackgroundSprite(dropdownSprite).withButtonSprite(buttonSprite); }

        public Builder withActiveCheck(Function<Integer,Boolean> activeCheck) { this.activeCheck = activeCheck; return this; }

        @Override
        protected Builder getSelf() { return this; }
        @Override
        public DropdownWidget build() { return new DropdownWidget(this); }
    }

}