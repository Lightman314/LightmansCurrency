package io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.misc.SafeSpriteData;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.ATMIconType;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.ATMIconData;
import net.minecraft.ResourceLocationException;

public class SpriteIcon extends ATMIconData {

	public static final ATMIconType TYPE = ATMIconType.create(SpriteIcon::new);

    public Object sprite = null;

	public final SafeSpriteData data;

	public SpriteIcon(JsonObject data) throws JsonSyntaxException, ResourceLocationException {
		super(data);

        this.data = SafeSpriteData.CODEC.decode(JsonOps.INSTANCE,data).getOrThrow(JsonSyntaxException::new).getFirst();
	}

	public SpriteIcon(int xPos, int yPos, SafeSpriteData data) {
		super(xPos,yPos);
		this.data = data;
	}
	
	@Override
	protected void saveAdditional(JsonObject data,DataContext<JsonElement> context) {

		data.addProperty("texture", this.data.texture().toString());
		data.addProperty("u", this.data.u());
		data.addProperty("v", this.data.v());
		data.addProperty("width", this.data.width());
		data.addProperty("height", this.data.height());
        data.addProperty("textureWidth",this.data.textureWidth());
        data.addProperty("textureHeight",this.data.textureHeight());

	}

	@Override
	public ATMIconType getType() { return TYPE; }
	
}
