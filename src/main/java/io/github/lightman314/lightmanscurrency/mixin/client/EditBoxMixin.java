package io.github.lightman314.lightmanscurrency.mixin.client;

import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.mixin_helpers.EditBoxAccessor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin(EditBox.class)
public abstract class EditBoxMixin extends AbstractWidget implements EditBoxAccessor {

    @Unique
    private boolean lightmanscurrencyAlreadyRendered = false;
    @Override
    public void lightmanscurrencySetAlreadyRenderered(boolean alreadyRendered) { this.lightmanscurrencyAlreadyRendered = alreadyRendered; }

    @Unique
    private Optional<ScreenArea> lightmanscurrencyScissorArea = Optional.empty();
    @Override
    public Optional<ScreenArea> lightmanscurrencyGetScissorArea() { return this.lightmanscurrencyScissorArea; }
    @Override
    public void lightmanscurrencySetScissorArea(@Nullable ScreenArea scissorArea) { this.lightmanscurrencyScissorArea = Optional.ofNullable(scissorArea); }

    public EditBoxMixin(int x, int y, int width, int height, Component message) { super(x, y, width, height, message); }

    @Inject(at = @At("HEAD"), method = "extractWidgetRenderState",cancellable = true)
    private void ignoreIfAlreadyRendered(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if(this.lightmanscurrencyAlreadyRendered)
            ci.cancel();
    }

}
