package io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin;

import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.IconType;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMExchangeButton;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.ATMIconData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class SimpleArrowIcon extends ATMIconData {

	public static final ResourceLocation TYPE_NAME = VersionUtil.lcResource("small_arrow");
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
	
	public SimpleArrowIcon(@Nonnull JsonObject data) throws JsonSyntaxException, ResourceLocationException {
		super(data);
		
		if(data.has("direction"))
			this.direction = ArrowType.parse(GsonHelper.getAsString(data, "direction"));
		else
		{
			LightmansCurrency.LogWarning("Simple Arrow icon has no defined direction. Will assume it's pointing right.");
			this.direction = ArrowType.RIGHT;
		}
	}
	
	public SimpleArrowIcon(int xPos, int yPos, @Nonnull ArrowType direction) {
		super(xPos, yPos);
		this.direction = direction;
	}

	@Override
	protected void saveAdditional(@Nonnull JsonObject data, @Nonnull HolderLookup.Provider lookup) {
		
		data.addProperty("direction", this.direction.name());
		
	}
	
	@Nonnull
	@Override
	protected ResourceLocation getType() { return TYPE_NAME; }

	@Override
	@OnlyIn(Dist.CLIENT)
	public void render(@Nonnull ATMExchangeButton button, @Nonnull EasyGuiGraphics gui, boolean isHovered)
	{
		gui.blitSprite(Sprite.SimpleSprite(ATMScreen.BUTTON_TEXTURE, this.direction.uOffset, 36, 6, 6), this.xPos, this.yPos, isHovered);
	}
	
}
