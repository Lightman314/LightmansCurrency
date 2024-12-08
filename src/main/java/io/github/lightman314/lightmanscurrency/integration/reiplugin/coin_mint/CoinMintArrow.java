package io.github.lightman314.lightmanscurrency.integration.reiplugin.coin_mint;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.MintScreen;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.Dimension;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.widgets.Arrow;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class CoinMintArrow extends Arrow {

    private final Rectangle bounds;
    private double animationDuration = -1;
    private NumberAnimator<Float> darkBackgroundAlpha = ValueAnimator.ofFloat()
            .withConvention(() -> REIRuntime.getInstance().isDarkThemeEnabled() ? 1.0F : 0.0F, ValueAnimator.typicalTransitionTime())
            .asFloat();

    public CoinMintArrow(Point position) { this.bounds = new Rectangle(Objects.requireNonNull(position),new Dimension(22,16)); }

    @Override
    public double getAnimationDuration() {
        return animationDuration;
    }

    @Override
    public void setAnimationDuration(double animationDurationMS) {
        this.animationDuration = animationDurationMS;
        if (this.animationDuration <= 0)
            this.animationDuration = -1;
    }

    @ApiStatus.Internal
    public void setDarkBackgroundAlpha(NumberAnimator<Float> darkBackgroundAlpha) {
        this.darkBackgroundAlpha = darkBackgroundAlpha;
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.darkBackgroundAlpha.update(delta);
        renderBackground(graphics, false, 1.0F);
        if (darkBackgroundAlpha.value() > 0.0F) {
            renderBackground(graphics, true, this.darkBackgroundAlpha.value());
        }
    }

    public void renderBackground(GuiGraphics graphics, boolean dark, float alpha) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.blendFunc(770, 771);
        ResourceLocation texture = MintScreen.GUI_TEXTURE;
        if (getAnimationDuration() > 0) {
            int width = Mth.ceil((System.currentTimeMillis() / (animationDuration / 22) % 22d));
            graphics.blit(texture, getX(), getY(), 176, 0, width, 16);
        } else {
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }

}