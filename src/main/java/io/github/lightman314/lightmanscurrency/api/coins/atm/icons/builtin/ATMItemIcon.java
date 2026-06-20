package io.github.lightman314.lightmanscurrency.api.coins.atm.icons.builtin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.helpers.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.coins.atm.icons.ATMIconType;
import io.github.lightman314.lightmanscurrency.api.coins.atm.icons.ATMIconData;
import net.minecraft.IdentifierException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;

public class ATMItemIcon extends ATMIconData {

    public static final ATMIconType TYPE = ATMIconType.create(ATMItemIcon::new);

    private final boolean simpleItem;
    public final ItemStackTemplate item;

    private ATMItemIcon(JsonObject data, DataContext<JsonElement> context) throws JsonSyntaxException, IdentifierException {
        super(data);

        if(data.has("item") && data.get("item").isJsonPrimitive())
        {
            this.item = new ItemStackTemplate(BuiltInRegistries.ITEM.getValue(Identifier.parse(GsonHelper.getAsString(data, "item"))));
            this.simpleItem = true;
        }
        else
        {
            this.item = context.readOrThrow(GsonHelper.getAsJsonObject(data,"item"),ItemStackTemplate.CODEC);
            this.simpleItem = false;
        }
    }

    public ATMItemIcon(int xPos, int yPos, ItemLike item) {
        super(xPos, yPos);
        this.item = new ItemStackTemplate(item.asItem());
        this.simpleItem = true;
    }

    public ATMItemIcon(int xPos, int yPos, ItemStackTemplate item)
    {
        super(xPos, yPos);
        this.item = item;
        this.simpleItem = false;
    }

    @Override
    protected void saveAdditional(JsonObject data,DataContext<JsonElement> context) {

        if(this.simpleItem)
            data.addProperty("item",BuiltInRegistries.ITEM.getKey(this.item.item().value()).toString());
        else
            data.add("item",context.write(this.item,ItemStackTemplate.CODEC));
    }

    @Override
    public ATMIconType getType() { return TYPE; }

}