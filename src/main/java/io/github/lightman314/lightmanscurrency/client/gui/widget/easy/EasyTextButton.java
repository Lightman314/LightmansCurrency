package io.github.lightman314.lightmanscurrency.client.gui.widget.easy;

import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EasyTextButton extends EasyButton {

    private Supplier<Component> text;


    public EasyTextButton(int x, int y, int width, int height, @Nonnull Component text, @Nonnull Consumer<EasyButton> press) { this(ScreenArea.of(x,y,width,height), () -> text, press); }
    public EasyTextButton(int x, int y, int width, int height, @Nonnull Component text, @Nonnull Runnable press) { this(ScreenArea.of(x,y,width,height), () -> text, b -> press.run()); }
    public EasyTextButton(@Nonnull ScreenPosition pos, int width, int height, @Nonnull Component text, @Nonnull Consumer<EasyButton> press) { this(pos.asArea(width,height), () -> text, press); }
    public EasyTextButton(@Nonnull ScreenPosition pos, int width, int height, @Nonnull Component text, @Nonnull Runnable press) { this(pos.asArea(width,height), () -> text, b -> press.run()); }
    public EasyTextButton(@Nonnull ScreenArea area, @Nonnull Component text, @Nonnull Consumer<EasyButton> press) { this(area, () -> text, press); }
    public EasyTextButton(@Nonnull ScreenArea area, @Nonnull Component text, @Nonnull Runnable press) { this(area, () -> text, b -> press.run()); }
    public EasyTextButton(int x, int y, int width, int height, @Nonnull Supplier<Component> text, @Nonnull Consumer<EasyButton> press) { this(ScreenArea.of(x,y,width,height), text, press); }
    public EasyTextButton(int x, int y, int width, int height, @Nonnull Supplier<Component> text, @Nonnull Runnable press) { this(ScreenArea.of(x,y,width,height), text, b -> press.run()); }
    public EasyTextButton(@Nonnull ScreenPosition pos, int width, int height, @Nonnull Supplier<Component> text, @Nonnull Consumer<EasyButton> press) { this(pos.asArea(width,height), text, press); }
    public EasyTextButton(@Nonnull ScreenPosition pos, int width, int height, @Nonnull Supplier<Component> text, @Nonnull Runnable press) { this(pos.asArea(width,height), text, b -> press.run()); }

    public EasyTextButton(@Nonnull ScreenArea area, @Nonnull Supplier<Component> text, @Nonnull Runnable press) { this(area, text, b -> press.run()); }
    public EasyTextButton(@Nonnull ScreenArea area, @Nonnull Supplier<Component> text, @Nonnull Consumer<EasyButton> press) {
        super(area, press);
        this.text = text;
    }

    public final EasyTextButton withAddons(WidgetAddon... addons) { this.withAddonsInternal(addons); return this; }

    @Override
    public final void setMessage(@Nonnull Component text) { this.text = () -> text; }
    public final void setMessage(@Nonnull Supplier<Component> text) { this.text = text; }

    //Copy/pasted from AbstractButton.getTextureY()
    private int getTextureY() {
        int i = 1;
        if (!this.active) {
            i = 0;
        } else if (this.isHoveredOrFocused()) {
            i = 2;
        }

        return 46 + (i * 20);
    }

    @Override
    protected void renderWidget(@Nonnull EasyGuiGraphics gui) {
        gui.renderButtonBG(0, 0, this.getWidth(), this.getHeight(), this.alpha, this);
        int i = getFGColor();
        this.renderScrollingString(gui.getGui(), gui.font, 2, i | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    protected void renderTick() { super.setMessage(this.text.get()); }

}
