package io.github.lightman314.lightmanscurrency.common.atm.icons;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMConversionButton;
import io.github.lightman314.lightmanscurrency.client.util.RenderUtil;
import io.github.lightman314.lightmanscurrency.common.atm.ATMIconData;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SimpleArrowIcon extends ATMIconData {

	public static final ResourceLocation TYPE_NAME = new ResourceLocation(LightmansCurrency.MODID, "small_arrow");
	public static final IconType TYPE = IconType.create(TYPE_NAME, SimpleArrowIcon::new);
	
	public enum ArrowType{
		UP(0),
		DOWN(6),
		LEFT(12),
		RIGHT(18);
		public final int uOffset;
		ArrowType(int uOffset) { this.uOffset = uOffset; }
		static ArrowType parse(String value) {
			for(ArrowType type : ArrowType.values())
			{
				if(type.name().equals(value))
					return type;
			}
			return ArrowType.RIGHT;
		}
	}
	
	private final ArrowType direction;
	
	public SimpleArrowIcon(JsonObject data) {
		super(data);
		
		if(data.has("direction"))
			this.direction = ArrowType.parse(data.get("direction").getAsString());
		else
		{
			LightmansCurrency.LogWarning("Simple Arrow icon has no defined direction. Will assume it's pointing right.");
			this.direction = ArrowType.RIGHT;
		}
	}
	
	public SimpleArrowIcon(int xPos, int yPos, ArrowType direction) {
		super(xPos, yPos);
		this.direction = direction;
	}

	@Override
	protected void saveAdditional(JsonObject data) {
		
		data.addProperty("direction", this.direction.name());
		
	}
	
	@Override
	protected ResourceLocation getType() { return TYPE_NAME; }

	@Override
	@OnlyIn(Dist.CLIENT)
	public void render(ATMConversionButton button, MatrixStack pose, boolean isHovered) {
		RenderUtil.bindTexture(ATMScreen.BUTTON_TEXTURE);
		button.blit(pose, button.x + this.xPos, button.y + this.yPos, this.direction.uOffset, ATMConversionButton.HEIGHT * 2, 6, 6);
	}
	
}
