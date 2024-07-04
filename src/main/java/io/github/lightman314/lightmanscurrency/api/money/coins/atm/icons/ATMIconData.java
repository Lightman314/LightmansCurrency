package io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons;

import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMExchangeButton;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public abstract class ATMIconData {
	
	protected final int xPos;
	protected final int yPos;
	
	protected ATMIconData(@Nonnull JsonObject data) throws JsonSyntaxException, ResourceLocationException {
		this.xPos = GsonHelper.getAsInt(data, "x");
		this.yPos = GsonHelper.getAsInt(data, "y");
	}
	
	protected ATMIconData(int xPos, int yPos) {
		this.xPos = xPos;
		this.yPos = yPos;
	}

	@Nonnull
	public final JsonObject save() {
		JsonObject data = new JsonObject();
		data.addProperty("type", this.getType().toString());
		data.addProperty("x", this.xPos);
		data.addProperty("y", this.yPos);
		this.saveAdditional(data);
		return data;
	}

	@Nonnull
	protected abstract ResourceLocation getType();
	
	protected abstract void saveAdditional(@Nonnull JsonObject data);
	
	@OnlyIn(Dist.CLIENT)
	public abstract void render(@Nonnull ATMExchangeButton button, @Nonnull EasyGuiGraphics gui, boolean isHovered);
	
}
