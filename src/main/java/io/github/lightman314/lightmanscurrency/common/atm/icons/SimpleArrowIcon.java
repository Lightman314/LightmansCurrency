package io.github.lightman314.lightmanscurrency.common.atm.icons;

import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMExchangeButton;
import io.github.lightman314.lightmanscurrency.common.atm.ATMIconData;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

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
				if(type.name().equalsIgnoreCase(value))
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
	public void render(@Nonnull ATMExchangeButton button, @Nonnull EasyGuiGraphics gui, boolean isHovered)
	{
		gui.blitSprite(Sprite.SimpleSprite(ATMScreen.BUTTON_TEXTURE, this.direction.uOffset, ATMExchangeButton.HEIGHT * 2, 6, 6), this.xPos, this.yPos, isHovered);
	}
	
}
