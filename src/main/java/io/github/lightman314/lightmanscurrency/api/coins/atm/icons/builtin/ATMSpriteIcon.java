package io.github.lightman314.lightmanscurrency.api.coins.atm.icons.builtin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.helpers.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.coins.atm.icons.ATMIconType;
import io.github.lightman314.lightmanscurrency.api.coins.atm.icons.ATMIconData;
import net.minecraft.IdentifierException;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

public class ATMSpriteIcon extends ATMIconData {

    public static final ATMIconType TYPE = ATMIconType.create(ATMSpriteIcon::new);

    public final Identifier sprite;
    public final int width;
    public final int height;

    public ATMSpriteIcon(JsonObject data,DataContext<JsonElement> context) throws JsonSyntaxException, IdentifierException {
        super(data);

        this.sprite = Identifier.parse(GsonHelper.getAsString(data,"sprite"));
        this.width = GsonHelper.getAsInt(data,"width");
        this.height = GsonHelper.getAsInt(data,"height");
    }

    public ATMSpriteIcon(int xPos, int yPos,Identifier sprite,int width,int height) {
        super(xPos,yPos);
        this.sprite = sprite;
        this.width = width;
        this.height = height;
    }

    @Override
    protected void saveAdditional(JsonObject data,DataContext<JsonElement> context) {
        data.addProperty("sprite",this.sprite.toString());
        data.addProperty("width", this.width);
        data.addProperty("height", this.height);
    }

    @Override
    public ATMIconType getType() { return TYPE; }

}