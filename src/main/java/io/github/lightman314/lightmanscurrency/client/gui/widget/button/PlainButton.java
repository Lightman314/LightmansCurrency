package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.client.util.RenderUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class PlainButton extends Button {
	
	private ResourceLocation buttonResource;
	private NonNullSupplier<Pair<Integer,Integer>> resourceSource;
	
	
	public PlainButton(int x, int y, int sizeX, int sizeY, IPressable pressable, ResourceLocation buttonResource, int resourceX, int resourceY) {
		this(x, y, sizeX, sizeY, pressable, buttonResource, () -> Pair.of(resourceX, resourceY));
	}
	
	public PlainButton(int x, int y, int sizeX, int sizeY, IPressable pressable, ResourceLocation buttonResource, NonNullSupplier<Pair<Integer, Integer>> resourceSource)
	{
		super(x, y, sizeX, sizeY, EasyText.empty(), pressable);
		this.buttonResource = buttonResource;
		this.resourceSource = resourceSource;
	}
	
	public void setResource(ResourceLocation buttonResource, int resourceX, int resourceY) { this.setResource(buttonResource, () -> Pair.of(resourceX, resourceY)); }
	
	public void setResource(ResourceLocation buttonResource, NonNullSupplier<Pair<Integer, Integer>> resourceSource)
	{
		this.buttonResource = buttonResource;
		this.resourceSource = resourceSource;
	}
	
	@Override
	public void renderButton(@Nonnull MatrixStack poseStack, int mouseX, int mouseY, float partialTicks)
	{
		RenderUtil.bindTexture(this.buttonResource);
		RenderUtil.color4f(1f, 1f, 1f, 1f);
        int offset = this.isHovered ? this.height : 0;
        if(!this.active)
			RenderUtil.color4f(0.5F, 0.5F, 0.5F, 1.0F);
        Pair<Integer,Integer> resource = this.resourceSource.get();
        this.blit(poseStack, this.x, this.y, resource.getFirst(), resource.getSecond() + offset, this.width, this.height);
		
	}

}