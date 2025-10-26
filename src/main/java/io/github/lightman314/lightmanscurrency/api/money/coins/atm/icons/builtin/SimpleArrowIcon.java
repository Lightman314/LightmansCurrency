package io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin;

import com.google.common.base.Suppliers;
import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FixedSizeSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteSource;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.IconType;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin.NormalSprite;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMExchangeButton;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.ATMIconData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Locale;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SimpleArrowIcon extends ATMIconData {

	public static final ResourceLocation TYPE_NAME = VersionUtil.lcResource("small_arrow");
	public static final IconType TYPE = IconType.create(TYPE_NAME, SimpleArrowIcon::new);

	public enum ArrowType{
		UP,
		DOWN,
		LEFT,
		RIGHT;
        private final Supplier<FixedSizeSprite> normalSprite;
        private final Supplier<FixedSizeSprite> hoveredSprite;
        ArrowType() {
            this.normalSprite = Suppliers.memoize(() -> new NormalSprite(SpriteSource.createTop(this.getSpriteID(),6,6)));
            this.hoveredSprite = Suppliers.memoize(() -> new NormalSprite(SpriteSource.createBottom(this.getSpriteID(),6,6)));
        }

        private ResourceLocation getSpriteID() { return VersionUtil.lcResource("common/widgets/atm_arrow_" + this.name().toLowerCase(Locale.ENGLISH)); }
        private FixedSizeSprite getSprite(boolean hovered) { return hovered ? this.hoveredSprite.get() : this.normalSprite.get(); }

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
	
	public SimpleArrowIcon(JsonObject data) throws JsonSyntaxException, ResourceLocationException {
		super(data);
		
		if(data.has("direction"))
			this.direction = ArrowType.parse(GsonHelper.getAsString(data, "direction"));
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
	protected void saveAdditional(JsonObject data, HolderLookup.Provider lookup) {
		
		data.addProperty("direction", this.direction.name());
		
	}

	@Override
	protected ResourceLocation getType() { return TYPE_NAME; }

	@Override
	@OnlyIn(Dist.CLIENT)
	public void render(ATMExchangeButton button, EasyGuiGraphics gui, boolean isHovered)
	{
        this.direction.getSprite(isHovered).render(gui,this.xPos,this.yPos);
	}
	
}
