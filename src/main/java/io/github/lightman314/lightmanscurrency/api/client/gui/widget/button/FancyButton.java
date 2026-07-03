package io.github.lightman314.lightmanscurrency.api.client.gui.widget.button;


import io.github.lightman314.lightmanscurrency.api.client.gui.widget.FancyWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.sounds.SoundManager;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class FancyButton extends FancyWidget {

    private final Consumer<FancyButton> pressAction;
    private final Optional<Consumer<FancyButton>> altPressAction;

    protected FancyButton(ButtonBuilder<?,?> builder) {
        super(builder);
        this.pressAction = builder.pressAction;
        this.altPressAction = Optional.ofNullable(builder.altPressAction);
    }

    @Override
    @ApiStatus.Internal
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
        this.handleCursor(graphics);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        playButtonClickSound(soundManager);
    }

    @Override
    protected boolean isValidClickButton(MouseButtonInfo buttonInfo) {
        int button = buttonInfo.button();
        //Always allow button 0, but allow button 1 (right-click) if the alt-press action is present
        return button == 0 || (button == 1 && this.altPressAction.isPresent());
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        int button = event.button();
        //Normal Press
        if(button == 0)
            this.pressAction.accept(this);
        //Alt-Press
        else if(button == 1 && this.altPressAction.isPresent())
            this.altPressAction.get().accept(this);
    }

    public static abstract class ButtonBuilder<T extends ButtonBuilder<T,X>,X extends FancyButton> extends Builder<T,X>
    {
        private Consumer<FancyButton> pressAction = b -> {};
        @Nullable
        private Consumer<FancyButton> altPressAction = null;

        protected ButtonBuilder() { }
        protected ButtonBuilder(int width,int height) { super(width,height); }

        public T onPress(Runnable action) { return this.onPress(b -> action.run()); }
        public T onPress(Consumer<FancyButton> action) { this.pressAction = action; return this.getSelf(); }

        public T onAltPress(Runnable action) { return this.onAltPress(b -> action.run()); }
        public T onAltPress(Consumer<FancyButton> action) { this.altPressAction = action; return this.getSelf(); }

    }

    public static abstract class FlexibleSizeButtonBuilder<T extends FlexibleSizeButtonBuilder<T,X>,X extends FancyButton> extends ButtonBuilder<T,X>
    {
        protected FlexibleSizeButtonBuilder() { }
        protected FlexibleSizeButtonBuilder(int width, int height) { super(width, height); }

        public final T ofSize(int width,int height) { this.setSize(width,height); return this.getSelf(); }
        public final T ofWidth(int width) { this.setWidth(width); return this.getSelf(); }
        public final T ofHeight(int height) { this.setHeight(height); return this.getSelf(); }

    }

}