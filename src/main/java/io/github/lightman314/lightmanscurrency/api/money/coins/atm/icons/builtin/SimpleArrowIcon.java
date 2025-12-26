package io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin;

import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.IconType;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.ATMIconData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import javax.annotation.ParametersAreNonnullByDefault;

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

		static ArrowType parse(String value) {
			for(ArrowType type : ArrowType.values())
			{
				if(type.name().equalsIgnoreCase(value))
					return type;
			}
			return ArrowType.RIGHT;
		}
	}

	public final ArrowType direction;
	
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
	public ResourceLocation getType() { return TYPE_NAME; }
	
}
