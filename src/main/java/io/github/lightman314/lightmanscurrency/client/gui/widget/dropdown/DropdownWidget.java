package io.github.lightman314.lightmanscurrency.client.gui.widget.dropdown;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FlexibleWidthSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteSource;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin.HorizontalSliceSprite;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import io.github.lightman314.lightmanscurrency.common.text.TextEntryBundle;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DropdownWidget extends EasyWidgetWithChildren implements IMouseListener {

    public static final int HEIGHT = 12;

    public static final FlexibleWidthSprite DROPDOWN_SPRITE = new HorizontalSliceSprite(SpriteSource.createTop(VersionUtil.lcResource("common/widgets/dropdown_main"),128,12),12);
    public static final FlexibleWidthSprite DROPDOWN_HIGHLIGHTED_SPRITE = new HorizontalSliceSprite(SpriteSource.createBottom(VersionUtil.lcResource("common/widgets/dropdown_main"),128,12),12);

    boolean open = false;

    int currentlySelected;
    public int getCurrentlySelected() { return this.currentlySelected; }
    public void setCurrentlySelected(int currentlySelected) { this.currentlySelected = MathUtil.clamp(currentlySelected,0,this.options.size() - 1); }

    private final List<Component> options;
    private final Consumer<Integer> onSelect;
    private final Function<Integer,Boolean> optionActive;

    List<DropdownButton> optionButtons = new ArrayList<>();

    private DropdownWidget(Builder builder) {
        super(builder);
        this.options = ImmutableList.copyOf(builder.options);
        this.currentlySelected = MathUtil.clamp(builder.selected,0,this.options.size() - 1);
        this.onSelect = builder.action;
        this.optionActive = builder.activeCheck;
    }

    //Init the buttons before this, so that they get pressed before this closes them on an offset click
    @Override
    public boolean addChildrenBeforeThis() { return true; }

    @Override
    public void addChildren(ScreenArea area) {
        this.optionButtons = new ArrayList<>();
        for(int i = 0; i < this.options.size(); ++i)
        {
            final int index = i;
            int yOff = HEIGHT + (i * HEIGHT);
            DropdownButton button = this.addChild(DropdownButton.builder()
                    .position(area.pos.offset(0,yOff))
                    .width(this.width)
                    .text(this.options.get(i))
                    .pressAction(() -> this.OnSelect(index))
                    .build());
            this.optionButtons.add(button);
            this.optionButtons.get(i).visible = this.open;
        }
    }

    @Override
    public void renderTick() {
        //Confirm the option buttons active state
        if(this.open)
        {
            for(int i = 0; i < this.optionButtons.size(); ++i)
                this.optionButtons.get(i).active = this.optionActive.apply(i) && i != this.currentlySelected;
        }
    }

    @Override
    public void renderWidget(EasyGuiGraphics gui) {

        //Draw the background
        if(!this.active)
            gui.setColor(0.5f, 0.5f, 0.5f);
        else
            gui.resetColor();

        FlexibleWidthSprite sprite = this.isHoveredOrFocused() ? DROPDOWN_HIGHLIGHTED_SPRITE : DROPDOWN_SPRITE;
        sprite.render(gui,0,0,this.width);

        //Draw the option text
        gui.drawString(this.fitString(gui, this.options.get(this.currentlySelected).getString()), 2, 2, 0x404040);

        gui.resetColor();

    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int click) {
        if (this.active && this.visible) {
            if (this.clicked(mouseX, mouseY) && this.isValidClickButton(click)) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
                this.open = !this.open;
                this.optionButtons.forEach(button -> button.visible = this.open);
                return true;
            }
            else if(this.open && !this.isOverChild(mouseX, mouseY))
            {
                this.open = false;
                this.optionButtons.forEach(button -> button.visible = false);
            }
        }
        return false;
    }

    private boolean isOverChild(double mouseX, double mouseY)
    {
        for(DropdownButton b : this.optionButtons)
        {
            if(b.isMouseOver(mouseX, mouseY))
                return true;
        }
        return false;
    }

    private void OnSelect(int index)
    {
        if(index < 0 || index >= this.optionButtons.size())
            return;
        this.currentlySelected = index;
        this.onSelect.accept(index);
        this.open = false;
        this.optionButtons.forEach(b -> b.visible = false);
    }

    private String fitString(EasyGuiGraphics gui, String text) {
        if(gui.font.width(text) <= this.width - 14)
            return text;
        while(gui.font.width(text + "...") > this.width - 14 && !text.isEmpty())
            text = text.substring(0, text.length() - 1);
        return text + "...";
    }

    @Override
    protected boolean isValidClickButton(int button) { return button == 0; }

    @Override
    public void playDownSound(SoundManager manager) { EasyButton.playClick(manager); }

    public static Builder builder() { return new Builder(); }

    @FieldsAreNonnullByDefault
    public static class Builder extends EasyBuilder<Builder>
    {

        private final List<Component> options = new ArrayList<>();
        private int selected = 0;
        private Consumer<Integer> action = i -> {};
        private Function<Integer,Boolean> activeCheck = i -> true;

        private Builder() { super(20,HEIGHT); }
        @Override
        protected Builder getSelf() { return this; }

        public Builder width(int width) { this.changeWidth(width); return this; }

        public Builder option(Component option) { this.options.add(option); return this; }
        public Builder option(TextEntry option) { this.options.add(option.get()); return this; }
        public Builder options(List<Component> options) { this.options.addAll(options); return this; }

        public <T> Builder enumOptions(TextEntryBundle<T> bundle, T[] values)
        {
            for(T val : values)
                this.option(bundle.get(val));
            return this;
        }

        public Builder selected(int selected) { this.selected = selected; return this; }

        public Builder selectAction(Consumer<Integer> action) { this.action = action; return this; }

        public Builder activeCheck(Function<Integer,Boolean> activeCheck) { this.activeCheck = activeCheck; return this; }

        public DropdownWidget build() { return new DropdownWidget(this); }

    }

}