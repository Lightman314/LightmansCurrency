package io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin;

import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.IconType;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMExchangeButton;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.ATMIconData;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class SpriteIcon extends ATMIconData {

	public static final ResourceLocation TYPE_NAME = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "sprite");
	public static final IconType TYPE = IconType.create(TYPE_NAME, SpriteIcon::new);
	
	private final Sprite sprite;
	
	public SpriteIcon(JsonObject data) throws JsonSyntaxException, ResourceLocationException {
		
		super(data);

		this.sprite = Sprite.SimpleSprite(
				ResourceLocation.parse(GsonHelper.getAsString(data, "texture")),
				GsonHelper.getAsInt(data, "u"),
				GsonHelper.getAsInt(data, "v"),
				GsonHelper.getAsInt(data, "width"),
				GsonHelper.getAsInt(data, "height"));
		
	}

	public SpriteIcon(int xPos, int yPos, @Nonnull Sprite sprite) {
		super(xPos,yPos);
		this.sprite = sprite;
	}
	
	@Override
	protected void saveAdditional(@Nonnull JsonObject data, @Nonnull HolderLookup.Provider lookup) {
		
		data.addProperty("texture", this.sprite.image.toString());
		data.addProperty("u", this.sprite.u);
		data.addProperty("v", this.sprite.v);
		data.addProperty("width", this.sprite.width);
		data.addProperty("height", this.sprite.height);
		
	}
	
	@Nonnull
	@Override
	protected ResourceLocation getType() { return TYPE_NAME; }

	@Override
	@OnlyIn(Dist.CLIENT)
	public void render(@Nonnull ATMExchangeButton button, @Nonnull EasyGuiGraphics gui, boolean isHovered) { gui.blitSprite(this.sprite, this.xPos, this.yPos); }
	
}
