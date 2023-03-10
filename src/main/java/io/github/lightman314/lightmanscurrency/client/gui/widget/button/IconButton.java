package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.RenderUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.NonNullFunction;
import net.minecraftforge.common.util.NonNullSupplier;

@OnlyIn(Dist.CLIENT)
public class IconButton extends Button {
	
	public static final int SIZE = 20;
	
	private NonNullFunction<IconButton, IconData> iconSource;
	
	private NonNullSupplier<Boolean> activeCheck = () -> this.active;
	private NonNullSupplier<Boolean> visibilityCheck = () -> this.visible;
	
	public IconButton(int x, int y, IPressable pressable, @Nonnull IconData icon)
	{
		super(x,y,SIZE,SIZE, EasyText.empty(), pressable);
		this.setIcon(icon);
	}
	
	public IconButton(int x, int y, IPressable pressable, @Nonnull NonNullSupplier<IconData> iconSource)
	{
		super(x,y,SIZE,SIZE,EasyText.empty(), pressable);
		this.setIcon(iconSource);
	}
	
	public IconButton(int x, int y, IPressable pressable, @Nonnull IconData icon, ITooltip tooltip)
	{
		super(x,y,SIZE,SIZE, EasyText.empty(), pressable, tooltip);
		this.setIcon(icon);
	}
	
	public IconButton(int x, int y, IPressable pressable, @Nonnull NonNullSupplier<IconData> iconSource, ITooltip tooltip)
	{
		super(x,y,SIZE,SIZE, EasyText.empty(), pressable, tooltip);
		this.setIcon(iconSource);
	}
	
	public IconButton(int x, int y, IPressable pressable, @Nonnull NonNullFunction<IconButton,IconData> iconSource, ITooltip tooltip)
	{
		super(x,y,SIZE, SIZE, EasyText.empty(), pressable, tooltip);
		this.setIcon(iconSource);
	}
	
	public void setVisiblityCheck(NonNullSupplier<Boolean> visibilityCheck) {
		if(visibilityCheck == null)
			this.visibilityCheck = () -> this.visible;
		else
			this.visibilityCheck = visibilityCheck;
	}
	
	public void setActiveCheck(NonNullSupplier<Boolean> activeCheck) {
		if(activeCheck == null)
			this.activeCheck = () -> this.active;
		else
			this.activeCheck = activeCheck;
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
	public void render(@Nonnull MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		this.visible = this.visibilityCheck.get();
		this.active = this.activeCheck.get();
		super.render(pose, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void renderButton(@Nonnull MatrixStack pose, int mouseX, int mouseY, float partialTicks)
	{

		RenderUtil.bindTexture(WIDGETS_LOCATION);
		RenderUtil.color4f(1f,  1f,  1f, 1f);
		
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        int offset = this.getYImage(this.isHovered);
        this.blit(pose, this.x, this.y, 0, 46 + offset * 20, this.width / 2, this.height);
        this.blit(pose, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + offset * 20, this.width / 2, this.height);
        if(!this.active)
			RenderUtil.color4f(0.5F, 0.5F, 0.5F, 1.0F);
        
        this.iconSource.apply(this).render(pose, this, Minecraft.getInstance().font, this.x + 2, this.y + 2);
		
	}

}
