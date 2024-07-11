package io.github.lightman314.lightmanscurrency.mixin;

import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;

@Mixin(AbstractWidget.class)
public class AbstractWidgetMixin {

    @Unique
    private AbstractWidget self() { return (AbstractWidget)(Object)this; }

    @Inject(at = @At("HEAD"),method = "render")
    protected void render(@Nonnull GuiGraphics gui, int mouseX, int mouseY, float partialTick, CallbackInfo callback)
    {
        if(this.self() instanceof EasyWidget widget)
            widget.renderTickInternal();
    }

}
