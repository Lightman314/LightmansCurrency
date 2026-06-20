package io.github.lightman314.lightmanscurrency.api.coins.atm.icons;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.data.DataContext;
import net.minecraft.IdentifierException;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

public abstract class ATMIconData {

    public static ATMIconData parse(JsonObject json,DataContext<JsonElement> context) throws JsonSyntaxException, IdentifierException {
        Identifier type = Identifier.parse(GsonHelper.getAsString(json,"type"));
        ATMIconType iconType = LCRegistries.Coins.ATM_ICON_TYPE.getValue(type);
        if(iconType == null)
            throw new JsonSyntaxException(type + " is not a valid ATM Icon type!");
        return iconType.parse(json,context);
    }

    public final int xPos;
    public final int yPos;

    protected ATMIconData(JsonObject data) throws JsonSyntaxException, IdentifierException {
        this.xPos = GsonHelper.getAsInt(data, "x");
        this.yPos = GsonHelper.getAsInt(data, "y");
    }

    protected ATMIconData(int xPos, int yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public final JsonObject save(DataContext<JsonElement> context) {
        JsonObject data = new JsonObject();
        data.addProperty("x", this.xPos);
        data.addProperty("y", this.yPos);
        this.saveAdditional(data,context);
        //Define the type last so that it can't be overridden
        data.addProperty("type", LCRegistries.Coins.ATM_ICON_TYPE.getKey(this.getType()).toString());
        return data;
    }

    public abstract ATMIconType getType();

    protected abstract void saveAdditional(JsonObject data,DataContext<JsonElement> context);

}
