package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.blaze3d.FieldsAreNonnullByDefault;
import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FixedSizeSprite;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PlainButton extends EasyButton {

    private Supplier<FixedSizeSprite> sprite;
    private final boolean drawInForeground;

    protected PlainButton(Builder builder)
    {
        super(builder);
        this.sprite = builder.sprite;
        this.drawInForeground = builder.drawInForeground;
    }

    public void setSprite(FixedSizeSprite sprite) { this.setSprite(() -> sprite); }

    public void setSprite(Supplier<FixedSizeSprite> sprite) { this.sprite = sprite; }

    @Override
    public void renderWidget(EasyGuiGraphics gui)
    {
        gui.resetColor();
        if(!this.active)
            gui.setColor(0.5f,0.5f,0.5f);
        if(this.drawInForeground)
            gui.pushPose().TranslateToForeground();
        this.sprite.get().render(gui,0,0,this);
        gui.resetColor();
        if(this.drawInForeground)
            gui.popPose();
    }

    public static Builder builder() { return new Builder(); }

    @FieldsAreNonnullByDefault
    public static class Builder extends EasyButtonBuilder<Builder>
    {
        protected Builder() {}

        @Override
        protected Builder getSelf() { return this; }

        private boolean drawInForeground = false;
        private Supplier<FixedSizeSprite> sprite = null;
        public Builder sprite(FixedSizeSprite sprite) { this.sprite = () -> sprite; this.changeSize(sprite.getWidth(),sprite.getHeight()); return this; }
        public Builder sprite(Supplier<FixedSizeSprite> sprite) {
            this.sprite = sprite;
            FixedSizeSprite example = sprite.get();
            if(example != null)
                this.changeSize(example.getWidth(),example.getHeight());
            return this;
        }

        public Builder drawInForeground() { this.drawInForeground = true; return this; }

        public PlainButton build() { return new PlainButton(this); }

    }
}