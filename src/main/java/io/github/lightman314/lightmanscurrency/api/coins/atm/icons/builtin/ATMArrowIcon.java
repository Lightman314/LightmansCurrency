package io.github.lightman314.lightmanscurrency.api.coins.atm.icons.builtin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.coins.atm.icons.ATMIconType;
import io.github.lightman314.lightmanscurrency.api.coins.atm.icons.ATMIconData;
import net.minecraft.IdentifierException;

public class ATMArrowIcon extends ATMIconData {

    public static final ATMIconType TYPE = ATMIconType.create(ATMArrowIcon::new);

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

    private ATMArrowIcon(JsonObject data) throws JsonSyntaxException, IdentifierException {
        super(data);

        if(data.has("direction"))
            this.direction = EnumHelper.getAsEnum(data,"direction",ArrowType.class,"Arrow Type");
        else
        {
            LightmansCurrency.LogWarning("Simple Arrow icon has no defined direction. Will assume it's pointing right.");
            this.direction = ArrowType.RIGHT;
        }
    }

    public ATMArrowIcon(int xPos, int yPos, ArrowType direction) {
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