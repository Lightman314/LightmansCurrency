package io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.ATMIconType;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.ATMIconData;
import net.minecraft.ResourceLocationException;
import net.minecraft.util.GsonHelper;

public class SimpleArrowIcon extends ATMIconData {

	public static final ATMIconType TYPE = ATMIconType.create(SimpleArrowIcon::new);

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
	protected void saveAdditional(JsonObject data, DataContext<JsonElement> context) {
		
		data.addProperty("direction", this.direction.name());
		
	}

	@Override
	public ATMIconType getType() { return TYPE; }
	
}
