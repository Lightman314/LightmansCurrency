package io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons;

import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ATMIconData {
	
	public final int xPos;
    public final int yPos;
	
	protected ATMIconData(JsonObject data) throws JsonSyntaxException, ResourceLocationException {
		this.xPos = GsonHelper.getAsInt(data, "x");
		this.yPos = GsonHelper.getAsInt(data, "y");
	}
	
	protected ATMIconData(int xPos, int yPos) {
		this.xPos = xPos;
		this.yPos = yPos;
	}

	public final JsonObject save() {
		JsonObject data = new JsonObject();
		data.addProperty("type", this.getType().toString());
		data.addProperty("x", this.xPos);
		data.addProperty("y", this.yPos);
		this.saveAdditional(data);
		return data;
	}
	
	public abstract ResourceLocation getType();
	
	protected abstract void saveAdditional(JsonObject data);
	
}
