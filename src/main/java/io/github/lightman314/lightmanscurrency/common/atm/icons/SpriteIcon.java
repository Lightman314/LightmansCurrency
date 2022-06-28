package io.github.lightman314.lightmanscurrency.common.atm.icons;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMConversionButton;
import io.github.lightman314.lightmanscurrency.common.atm.ATMIconData;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SpriteIcon extends ATMIconData {

	public static final ResourceLocation TYPE_NAME = new ResourceLocation(LightmansCurrency.MODID, "sprite");
	public static final IconType TYPE = IconType.create(TYPE_NAME, SpriteIcon::new);
	
	private final ResourceLocation texture;
	private final int u;
	private final int v;
	private final int width;
	private final int height;
	
	public SpriteIcon(JsonObject data) {
		
		super(data);
		
		this.texture = new ResourceLocation(data.get("texture").getAsString());
		this.u = data.get("u").getAsInt();
		this.v = data.get("v").getAsInt();
		this.width = data.get("width").getAsInt();
		this.height = data.get("height").getAsInt();
		
	}

	public SpriteIcon(int xPos, int yPos, ResourceLocation texture, int u, int v, int width, int height) {
		super(xPos,yPos);
		this.texture = texture;
		this.u = u;
		this.v = v;
		this.width = width;
		this.height = height;
	}
	
	@Override
	protected void saveAdditional(JsonObject data) {
		
		data.addProperty("texture", this.texture.toString());
		data.addProperty("u", this.u);
		data.addProperty("v", this.v);
		data.addProperty("width", this.width);
		data.addProperty("height", this.height);
		
	}
	
	@Override
	protected ResourceLocation getType() { return TYPE_NAME; }

	@Override
	@OnlyIn(Dist.CLIENT)
	public void render(ATMConversionButton button, PoseStack pose, boolean isHovered) {
		RenderSystem.setShaderTexture(0, this.texture);
		button.blit(pose, button.x + this.xPos, button.y + this.yPos, this.u, this.v, this.width, this.height);
	}
	
}
