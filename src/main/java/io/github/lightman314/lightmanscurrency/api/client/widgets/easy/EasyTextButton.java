package io.github.lightman314.lightmanscurrency.api.client.widgets.easy;

import com.google.common.base.Suppliers;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Supplier;

public class EasyTextButton extends EasyButton {

    private Supplier<Component> text;

    protected EasyTextButton(EasyButtonBuilder<?> builder, Supplier<Component> text)
    {
        super(builder);
        this.text = text;
    }
    protected EasyTextButton(Builder builder)
    {
        super(builder);
        this.text = builder.text;
    }

    @Override
    public final void setMessage(Component text) { this.text = () -> text; }
    public final void setMessage(Supplier<Component> text) { this.text = text; }

    @Override
    protected void renderWidget(EasyGuiGraphics gui) {
        gui.renderButtonBG(0, 0, this.getWidth(), this.getHeight(), this.alpha, this);
        int i = this.getFGColor();
        gui.drawScrollingString(this.text.get(),ScreenArea.of(2,0,this.width - 4,this.height),i | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    protected void renderTick() { super.setMessage(this.text.get()); }

    public static Builder builder() { return new Builder(); }

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
