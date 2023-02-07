package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.NonNullFunction;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class IconButton extends Button{
	
	public static final int SIZE = 20;
	
	private NonNullFunction<IconButton, IconData> iconSource;
	
	private NonNullSupplier<Boolean> activeCheck = () -> this.active;
	private NonNullSupplier<Boolean> visibilityCheck = () -> this.visible;
	
	public IconButton(int x, int y, OnPress pressable, @Nonnull IconData icon)
	{
		super(x,y,SIZE,SIZE,new TextComponent(""), pressable);
		this.setIcon(icon);
	}
	
	public IconButton(int x, int y, OnPress pressable, @Nonnull NonNullSupplier<IconData> iconSource)
	{
		super(x,y,SIZE,SIZE,new TextComponent(""), pressable);
		this.setIcon(iconSource);
	}
	
	public IconButton(int x, int y, OnPress pressable, @Nonnull IconData icon, OnTooltip tooltip)
	{
		super(x,y,SIZE,SIZE, new TextComponent(""), pressable, tooltip);
		this.setIcon(icon);
	}
	
	public IconButton(int x, int y, OnPress pressable, @Nonnull NonNullSupplier<IconData> iconSource, OnTooltip tooltip)
	{
		super(x,y,SIZE,SIZE, new TextComponent(""), pressable, tooltip);
		this.setIcon(iconSource);
	}
	
	public IconButton(int x, int y, OnPress pressable, @Nonnull NonNullFunction<IconButton,IconData> iconSource, OnTooltip tooltip)
	{
		super(x,y,SIZE, SIZE, new TextComponent(""), pressable, tooltip);
		this.setIcon(iconSource);
	}
	
	@Deprecated
	public IconButton(int x, int y, OnPress pressable, Font ignored, IconData icon) { this(x, y, pressable, icon); }
	@Deprecated
	public IconButton(int x, int y, OnPress pressable, ResourceLocation iconResource, int resourceX, int resourceY)
	{
		super(x,y,SIZE,SIZE, new TextComponent(""), pressable);
		this.setIcon(IconData.of(iconResource, resourceX, resourceY));
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
		super.render(pose, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void renderButton(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks)
	{
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
		RenderSystem.setShaderColor(1f,  1f,  1f, 1f);
		
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        int offset = this.getYImage(this.isHovered);
        this.blit(pose, this.x, this.y, 0, 46 + offset * 20, this.width / 2, this.height);
        this.blit(pose, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + offset * 20, this.width / 2, this.height);
        if(!this.active)
            RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
        
        this.iconSource.apply(this).render(pose, this, Minecraft.getInstance().font, this.x + 2, this.y + 2);
		
	}

}
