package io.github.lightman314.lightmanscurrency.client.gui.widget.easy;

import com.google.common.base.Suppliers;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EasyTextButton extends EasyButton {

    private Supplier<Component> text;

    @Deprecated
    public EasyTextButton(int x, int y, int width, int height, @Nonnull Component text, @Nonnull Consumer<EasyButton> press) { this(ScreenArea.of(x,y,width,height), () -> text, press); }
    @Deprecated
    public EasyTextButton(int x, int y, int width, int height, @Nonnull Component text, @Nonnull Runnable press) { this(ScreenArea.of(x,y,width,height), () -> text, b -> press.run()); }
    @Deprecated
    public EasyTextButton(@Nonnull ScreenPosition pos, int width, int height, @Nonnull Component text, @Nonnull Consumer<EasyButton> press) { this(pos.asArea(width,height), () -> text, press); }
    @Deprecated
    public EasyTextButton(@Nonnull ScreenPosition pos, int width, int height, @Nonnull Component text, @Nonnull Runnable press) { this(pos.asArea(width,height), () -> text, b -> press.run()); }
    @Deprecated
    public EasyTextButton(@Nonnull ScreenArea area, @Nonnull Component text, @Nonnull Consumer<EasyButton> press) { this(area, () -> text, press); }
    @Deprecated
    public EasyTextButton(@Nonnull ScreenArea area, @Nonnull Component text, @Nonnull Runnable press) { this(area, () -> text, b -> press.run()); }
    @Deprecated
    public EasyTextButton(int x, int y, int width, int height, @Nonnull Supplier<Component> text, @Nonnull Consumer<EasyButton> press) { this(ScreenArea.of(x,y,width,height), text, press); }
    @Deprecated
    public EasyTextButton(int x, int y, int width, int height, @Nonnull Supplier<Component> text, @Nonnull Runnable press) { this(ScreenArea.of(x,y,width,height), text, b -> press.run()); }
    @Deprecated
    public EasyTextButton(@Nonnull ScreenPosition pos, int width, int height, @Nonnull Supplier<Component> text, @Nonnull Consumer<EasyButton> press) { this(pos.asArea(width,height), text, press); }
    @Deprecated
    public EasyTextButton(@Nonnull ScreenPosition pos, int width, int height, @Nonnull Supplier<Component> text, @Nonnull Runnable press) { this(pos.asArea(width,height), text, b -> press.run()); }
    @Deprecated
    public EasyTextButton(@Nonnull ScreenArea area, @Nonnull Supplier<Component> text, @Nonnull Runnable press) { this(area, text, b -> press.run()); }
    @Deprecated
    public EasyTextButton(@Nonnull ScreenArea area, @Nonnull Supplier<Component> text, @Nonnull Consumer<EasyButton> press) {
        super(area, press);
        this.text = text;
    }
    protected EasyTextButton(@Nonnull EasyButtonBuilder<?> builder, @Nonnull Supplier<Component> text)
    {
        super(builder);
        this.text = text;
    }
    protected EasyTextButton(@Nonnull Builder builder)
    {
        super(builder);
        this.text = builder.text;
    }

    @Deprecated
    public final EasyTextButton withAddons(WidgetAddon... addons) { this.withAddonsInternal(addons); return this; }

    @Override
    public final void setMessage(@Nonnull Component text) { this.text = () -> text; }
    public final void setMessage(@Nonnull Supplier<Component> text) { this.text = text; }

    @Override
    protected void renderWidget(@Nonnull EasyGuiGraphics gui) {
        gui.renderButtonBG(0, 0, this.getWidth(), this.getHeight(), this.alpha, this);
        int i = getFGColor();
        this.renderScrollingString(gui.getGui(), gui.font, 2, i | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    protected void renderTick() { super.setMessage(this.text.get()); }

    @Nonnull
    public static Builder builder() { return new Builder(); }

    @FieldsAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    public static class Builder extends EasySizableButtonBuilder<Builder>
    {

        protected Builder() {}

        private Supplier<Component> text = EasyText::empty;

        @Override
        protected Builder getSelf() { return this; }

        public Builder withText(Component text) { this.text = () -> text; return this; }
        public Builder withText(Supplier<Component> text) { this.text = text; return this; }
        public Builder withText(TextEntry text) { this.text = Suppliers.memoize(text::get); return this; }

        public EasyTextButton build() { return new EasyTextButton(this); }

    }

}
