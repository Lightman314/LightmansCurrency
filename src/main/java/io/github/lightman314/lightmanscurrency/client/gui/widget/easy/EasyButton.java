package io.github.lightman314.lightmanscurrency.client.gui.widget.easy;

import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public abstract class EasyButton extends EasyWidget {

    public static final Consumer<EasyButton> NULL_PRESS = button -> {};

    private final Consumer<EasyButton> press;

    protected EasyButton(@Nonnull EasyButtonBuilder<?> builder) { super(builder); this.press = builder.action; }
    @Deprecated
    protected EasyButton(int x, int y, int width, int height, Runnable press) { this(x,y,width,height,b -> press.run()); }
    @Deprecated
    protected EasyButton(int x, int y, int width, int height, Consumer<EasyButton> press) { super(x,y,width,height); this.press = press; }
    @Deprecated
    protected EasyButton(ScreenPosition position, int width, int height, Runnable press) { this(position, width, height, b -> press.run()); }
    @Deprecated
    protected EasyButton(ScreenPosition position, int width, int height, Consumer<EasyButton> press) { super(position, width, height); this.press = press; }
    @Deprecated
    protected EasyButton(ScreenPosition position, int width, int height, Component title, Runnable press) { this(position, width, height, title, b -> press.run());}
    @Deprecated
    protected EasyButton(ScreenPosition position, int width, int height, Component title, Consumer<EasyButton> press) { super(position, width, height, title); this.press = press; }
    @Deprecated
    protected EasyButton(ScreenArea area, Runnable press) { this(area, b -> press.run()); }
    @Deprecated
    protected EasyButton(ScreenArea area, Consumer<EasyButton> press) { super(area); this.press = press; }
    @Deprecated
    protected EasyButton(ScreenArea area, Component title, Runnable press) { this(area, title, b -> press.run()); }
    @Deprecated
    protected EasyButton(ScreenArea area, Component title, Consumer<EasyButton> press) { super(area, title); this.press = press; }

    @Override
    protected boolean isValidClickButton(int button) { return button == 0; }

    @Override
    public void playDownSound(@Nonnull SoundManager manager) { playClick(manager); }

    public static void playClick(@Nonnull SoundManager manager) { manager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)); }

    @Override
    public void onClick(double mouseX, double mouseY, int button) { this.onPress(); }

    @Override
    public boolean keyPressed(int p_93374_, int p_93375_, int p_93376_) {
        if (this.active && this.visible) {
            if (CommonInputs.selected(p_93374_)) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
                this.onPress();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    protected void onPress() { this.press.accept(this); this.setFocused(false); }

    @MethodsReturnNonnullByDefault
    @FieldsAreNonnullByDefault
    public static abstract class EasyButtonBuilder<T extends EasyButtonBuilder<T>> extends EasyBuilder<T>
    {

        private Consumer<EasyButton> action = b -> {};

        public final T pressAction(@Nonnull Consumer<EasyButton> action) { this.action = action; return this.getSelf(); }
        public final T pressAction(@Nonnull Runnable action) { this.action = b -> action.run(); return this.getSelf(); }

    }

    @MethodsReturnNonnullByDefault
    public static abstract class EasySizableButtonBuilder<T extends EasySizableButtonBuilder<T>> extends EasyButtonBuilder<T>
    {
        public final T width(int width) { this.changeWidth(width); return this.getSelf(); }
        public final T height(int height) { this.changeHeight(height); return this.getSelf(); }
        public final T size(int width, int height) { this.changeSize(width,height); return this.getSelf(); }
    }

}
