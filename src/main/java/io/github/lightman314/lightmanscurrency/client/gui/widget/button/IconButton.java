package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.NonNullFunction;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class IconButton extends Button{
	
	public static final int SIZE = 20;
	
	private NonNullFunction<IconButton, IconData> iconSource;
	
	private NonNullSupplier<Boolean> activeCheck = () -> this.active;
	private NonNullSupplier<Boolean> visibilityCheck = () -> this.visible;
	private final Supplier<Tooltip> tooltipSupplier;
	
	public IconButton(int x, int y, OnPress pressable, @Nonnull IconData icon)
	{
		super(x, y, SIZE, SIZE, Component.empty(), pressable, Button.DEFAULT_NARRATION);
		this.tooltipSupplier = () -> null;
		this.setIcon(icon);
	}
	
	public IconButton(int x, int y, OnPress pressable, @Nonnull NonNullSupplier<IconData> iconSource)
	{
		super(x, y, SIZE, SIZE, Component.empty(), pressable, Button.DEFAULT_NARRATION);
		this.tooltipSupplier = () -> null;
		this.setIcon(iconSource);
	}
	
	public IconButton(int x, int y, OnPress pressable, @Nonnull IconData icon, Supplier<Tooltip> tooltip)
	{
		super(x, y, SIZE, SIZE, Component.empty(), pressable, Button.DEFAULT_NARRATION);
		this.tooltipSupplier = tooltip;
		this.setIcon(icon);
	}
	
	public IconButton(int x, int y, OnPress pressable, @Nonnull NonNullSupplier<IconData> iconSource, Supplier<Tooltip> tooltip)
	{
		super(x, y, SIZE, SIZE, Component.empty(), pressable, Button.DEFAULT_NARRATION);
		this.tooltipSupplier = tooltip;
		this.setIcon(iconSource);
	}
	
	public IconButton(int x, int y, OnPress pressable, @Nonnull NonNullFunction<IconButton,IconData> iconSource, Supplier<Tooltip> tooltip)
	{
		super(x,y,SIZE, SIZE, Component.empty(), pressable, Button.DEFAULT_NARRATION);
		this.tooltipSupplier = tooltip;
		this.setIcon(iconSource);
	}
	
	public void setVisiblityCheck(NonNullSupplier<Boolean> visibilityCheck) {
		this.visibilityCheck = Objects.requireNonNullElseGet(visibilityCheck, () -> () -> this.visible);
	}
	
	public void setActiveCheck(NonNullSupplier<Boolean> activeCheck) {
		if(activeCheck == null)
			this.activeCheck = () -> this.active;
		else
			this.activeCheck = activeCheck;
	}
	
	@Deprecated
	public void setResource(ResourceLocation iconResource, int resourceX, int resourceY)
	{
		this.iconSource = b -> IconData.of(iconResource, resourceX, resourceY);
	}
	
	public void setIcon(@Nonnull IconData icon)
	{
		this.iconSource = b -> icon;
	}
	
	public void setIcon(@Nonnull NonNullSupplier<IconData> iconSource) {
		this.iconSource = b -> iconSource.get();
	}
	
	public void setIcon(@Nonnull NonNullFunction<IconButton,IconData> iconSource)
	{
		this.iconSource = iconSource;
	}
	
	@Override
	public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		this.visible = this.visibilityCheck.get();
		this.active = this.activeCheck.get();
		this.setTooltip(this.tooltipSupplier.get());
		super.render(pose, mouseX, mouseY, partialTicks);
	}

	@Override
	public void renderButton(@NotNull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
		RenderSystem.setShaderColor(1f,  1f,  1f, 1f);
		
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        int offset = this.getYImage(this.isHovered);
        this.blit(matrixStack, this.getX(), this.getY(), 0, 46 + offset * 20, this.width / 2, this.height);
        this.blit(matrixStack, this.getX() + this.width / 2, this.getY(), 200 - this.width / 2, 46 + offset * 20, this.width / 2, this.height);
        if(!this.active)
            RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
        
        this.iconSource.apply(this).render(matrixStack, this, Minecraft.getInstance().font, this.getX() + 2, this.getY() + 2);
		
	}

}
