package io.github.lightman314.lightmanscurrency.mixin;

import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;

@Mixin(EasyWidget.class)
public class EasyWidgetMixin {

    @Unique
    private EasyWidget self() { return (EasyWidget)(Object)this; }

    @Inject(at = @At("HEAD"),method = "render")
    protected void render(@Nonnull GuiGraphics gui, int mouseX, int mouseY, float partialTick, CallbackInfo callback)
    {
        this.self().renderTickInternal();
    }

}
