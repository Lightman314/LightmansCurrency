package io.github.lightman314.lightmanscurrency.client.gui.widget.easy;

import com.google.common.base.Suppliers;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class EasyTextButton extends EasyButton {

    private Supplier<Component> text;

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
        gui.renderButtonBG(0, 0, this.getWidth(), this.getHeight(), this.alpha, this.getTextureY());
        int i = this.getFGColor();
        gui.drawScrollingString(this.text.get(),ScreenArea.of(2,0,this.width - 4,this.height),i | Mth.ceil(this.alpha * 255.0F) << 24);
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

        public Builder text(Component text) { this.text = () -> text; return this; }
        public Builder text(Supplier<Component> text) { this.text = text; return this; }
        public Builder text(TextEntry text) { this.text = Suppliers.memoize(text::get); return this; }

        public EasyTextButton build() { return new EasyTextButton(this); }

    }

}