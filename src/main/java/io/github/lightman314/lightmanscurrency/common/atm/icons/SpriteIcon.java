package io.github.lightman314.lightmanscurrency.common.atm.icons;

import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMExchangeButton;
import io.github.lightman314.lightmanscurrency.common.atm.ATMIconData;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class SpriteIcon extends ATMIconData {

	public static final ResourceLocation TYPE_NAME = new ResourceLocation(LightmansCurrency.MODID, "sprite");
	public static final IconType TYPE = IconType.create(TYPE_NAME, SpriteIcon::new);
	
	private final Sprite sprite;
	
	public SpriteIcon(JsonObject data) {
		
		super(data);

		this.sprite = Sprite.SimpleSprite(
				new ResourceLocation(data.get("texture").getAsString()),
				data.get("u").getAsInt(),
				data.get("v").getAsInt(),
				data.get("width").getAsInt(),
				data.get("height").getAsInt());
		
	}

	public SpriteIcon(int xPos, int yPos, Sprite sprite) {
		super(xPos,yPos);
		this.sprite = sprite;
	}
	
	@Override
	protected void saveAdditional(JsonObject data) {
		
		data.addProperty("texture", this.sprite.image.toString());
		data.addProperty("u", this.sprite.u);
		data.addProperty("v", this.sprite.v);
		data.addProperty("width", this.sprite.width);
		data.addProperty("height", this.sprite.height);
		
	}
	
	@Override
	protected ResourceLocation getType() { return TYPE_NAME; }

	@Override
	@OnlyIn(Dist.CLIENT)
	public void render(@Nonnull ATMExchangeButton button, @Nonnull EasyGuiGraphics gui, boolean isHovered) { gui.blitSprite(this.sprite, this.xPos, this.yPos); }
	
}
