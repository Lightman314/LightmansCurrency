package io.github.lightman314.lightmanscurrency.mixin.client;

import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.mixin_helpers.EditBoxAccessor;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetMixin {

    @Inject(at = @At("HEAD"),method = "isMouseOver",cancellable = true)
    private void ignoreIfOutsideScissor(double mouseX,double mouseY,CallbackInfoReturnable<Boolean> cir) {
        if(this instanceof EditBoxAccessor a) {
            Optional<ScreenArea> area = a.lightmanscurrencyGetScissorArea();
            if(area.isPresent() && !area.get().isInArea(mouseX,mouseY))
                cir.setReturnValue(false);
        }
    }

}
