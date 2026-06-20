package io.github.lightman314.lightmanscurrency.api.coins.atm;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.coins.atm.commands.ATMCommand;
import io.github.lightman314.lightmanscurrency.api.coins.atm.commands.ExchangeDirection;
import io.github.lightman314.lightmanscurrency.api.coins.atm.commands.builtin.ExchangeAllCommand;
import io.github.lightman314.lightmanscurrency.api.coins.atm.commands.builtin.ExchangeCommand;
import io.github.lightman314.lightmanscurrency.api.helpers.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.coins.atm.icons.ATMIconData;
import io.github.lightman314.lightmanscurrency.api.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.coins.atm.icons.builtin.ATMItemIcon;
import io.github.lightman314.lightmanscurrency.api.coins.atm.icons.builtin.ATMArrowIcon;
import io.github.lightman314.lightmanscurrency.api.coins.atm.icons.builtin.ATMArrowIcon.ArrowType;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import net.minecraft.IdentifierException;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.ItemLike;

public class ATMExchangeButtonData {

    public final ScreenPosition position;
    public final int width;
    public final int height;
    public final ATMCommand command;
    private final List<ATMIconData> icons;
    public ImmutableList<ATMIconData> getIcons() { return ImmutableList.copyOf(this.icons); }

    public static ATMExchangeButtonData parse(JsonObject data,DataContext<JsonElement> context) throws JsonSyntaxException, IdentifierException { return new ATMExchangeButtonData(data,context); }

    private ATMExchangeButtonData(JsonObject data,DataContext<JsonElement> context) throws JsonSyntaxException, IdentifierException {
        this.position = ScreenPosition.of(GsonHelper.getAsInt(data, "x"), GsonHelper.getAsInt(data,"y"));
        this.width = GsonHelper.getAsInt(data,"width");
        this.height = GsonHelper.getAsInt(data,"height",18);
        if(this.height <= 0)
            throw new JsonSyntaxException("height cannot be 0 or less!");
        this.command = ATMCommand.parse(GsonHelper.getAsJsonObject(data,"command"),context);

        this.icons = new ArrayList<>();
        if(data.has("icons"))
        {
            JsonArray iconListData = GsonHelper.getAsJsonArray(data, "icons");
            for(int i = 0; i < iconListData.size(); ++i)
            {
                try {
                    JsonObject iconData = iconListData.get(i).getAsJsonObject();
                    this.icons.add(ATMIconData.parse(iconData,context));
                } catch(JsonSyntaxException | IdentifierException e) { LightmansCurrency.LogError("Error parsing ATM Icon #" + (i + 1) + ".", e);}
            }
        }
        else
        {
            LightmansCurrency.LogWarning("ATM Button Data has no 'icons' entry. Button will be blank.");
        }
    }

    public ATMExchangeButtonData(int xPos, int yPos, int width, ATMCommand command, List<ATMIconData> icons) { this(xPos,yPos,width,0,command,icons);}
    public ATMExchangeButtonData(int xPos, int yPos, int width, int height, ATMCommand command, List<ATMIconData> icons) {
        this.position = ScreenPosition.of(xPos, yPos);
        this.width = width;
        this.height = height == 0 ? 18 : height;
        this.command = command;
        this.icons = icons;
    }

    public JsonObject save(DataContext<JsonElement> context) {
        JsonObject data = new JsonObject();

        data.addProperty("x", this.position.x);
        data.addProperty("y", this.position.y);
        data.addProperty("width", this.width);
        data.addProperty("height",this.height);
        data.add("command",this.command.write(context));

        JsonArray iconListData = new JsonArray();
        for (ATMIconData icon : this.icons)
            iconListData.add(icon.save(context));
        data.add("icons", iconListData);

        return data;
    }

    public static void generateMain(ATMData.Builder builder) {
        //Exchange All
        builder.addButton(exchangeAllUpDefault());
        builder.addButton(exchangeAllDownDefault());
        //Copper <-> Iron
        builder.addButton(exchangeSingle(6, 61, LCItems.COIN_IRON, LCItems.COIN_COPPER, new ExchangeCommand(ExchangeDirection.DOWN, LCItems.COIN_IRON)));
        builder.addButton(exchangeSingle(6, 88, LCItems.COIN_COPPER, LCItems.COIN_IRON, new ExchangeCommand(ExchangeDirection.UP, LCItems.COIN_COPPER)));
        //Iron <-> Gold
        builder.addButton(exchangeSingle(41, 61, LCItems.COIN_GOLD, LCItems.COIN_IRON, new ExchangeCommand(ExchangeDirection.DOWN, LCItems.COIN_GOLD)));
        builder.addButton(exchangeSingle(41, 88, LCItems.COIN_IRON, LCItems.COIN_GOLD, new ExchangeCommand(ExchangeDirection.UP, LCItems.COIN_IRON)));
        //Gold <-> Emerald
        builder.addButton(exchangeSingle(75, 61, LCItems.COIN_EMERALD, LCItems.COIN_GOLD, new ExchangeCommand(ExchangeDirection.DOWN, LCItems.COIN_EMERALD)));
        builder.addButton(exchangeSingle(75, 88, LCItems.COIN_GOLD, LCItems.COIN_EMERALD, new ExchangeCommand(ExchangeDirection.UP, LCItems.COIN_GOLD)));
        //Emerald <-> Diamond
        builder.addButton(exchangeSingle(109, 61, LCItems.COIN_DIAMOND, LCItems.COIN_EMERALD, new ExchangeCommand(ExchangeDirection.DOWN, LCItems.COIN_DIAMOND)));
        builder.addButton(exchangeSingle(109, 88, LCItems.COIN_EMERALD, LCItems.COIN_DIAMOND, new ExchangeCommand(ExchangeDirection.UP, LCItems.COIN_EMERALD)));
        //Diamond <-> Netherite
        builder.addButton(exchangeSingle(144, 61, LCItems.COIN_NETHERITE, LCItems.COIN_DIAMOND, new ExchangeCommand(ExchangeDirection.DOWN, LCItems.COIN_NETHERITE)));
        builder.addButton(exchangeSingle(144, 88, LCItems.COIN_DIAMOND, LCItems.COIN_NETHERITE, new ExchangeCommand(ExchangeDirection.UP, LCItems.COIN_DIAMOND)));
    }

    public static void generateChocolate(ATMData.Builder builder) {
        //builder.addButton(exchangeAllUpChocolate());
        //builder.addButton(exchangeAllDownChocolate());
        //Copper <-> Iron
        builder.addButton(exchangeSingle(6, 61, LCItems.COIN_CHOCOLATE_IRON, LCItems.COIN_CHOCOLATE_COPPER, new ExchangeCommand(ExchangeDirection.DOWN, LCItems.COIN_CHOCOLATE_IRON)));
        builder.addButton(exchangeSingle(6, 88, LCItems.COIN_CHOCOLATE_COPPER, LCItems.COIN_CHOCOLATE_IRON, new ExchangeCommand(ExchangeDirection.UP, LCItems.COIN_CHOCOLATE_COPPER)));
        //Iron <-> Gold
        builder.addButton(exchangeSingle(41, 61, LCItems.COIN_CHOCOLATE_GOLD, LCItems.COIN_CHOCOLATE_IRON, new ExchangeCommand(ExchangeDirection.DOWN, LCItems.COIN_CHOCOLATE_GOLD)));
        builder.addButton(exchangeSingle(41, 88, LCItems.COIN_CHOCOLATE_IRON, LCItems.COIN_CHOCOLATE_GOLD, new ExchangeCommand(ExchangeDirection.UP, LCItems.COIN_CHOCOLATE_IRON)));
        //Gold <-> Emerald
        builder.addButton(exchangeSingle(75, 61, LCItems.COIN_CHOCOLATE_EMERALD, LCItems.COIN_CHOCOLATE_GOLD, new ExchangeCommand(ExchangeDirection.DOWN, LCItems.COIN_CHOCOLATE_EMERALD)));
        builder.addButton(exchangeSingle(75, 88, LCItems.COIN_CHOCOLATE_GOLD, LCItems.COIN_CHOCOLATE_EMERALD, new ExchangeCommand(ExchangeDirection.UP, LCItems.COIN_CHOCOLATE_GOLD)));
        //Emerald <-> Diamond
        builder.addButton(exchangeSingle(109, 61, LCItems.COIN_CHOCOLATE_DIAMOND, LCItems.COIN_CHOCOLATE_EMERALD, new ExchangeCommand(ExchangeDirection.DOWN, LCItems.COIN_CHOCOLATE_DIAMOND)));
        builder.addButton(exchangeSingle(109, 88, LCItems.COIN_CHOCOLATE_EMERALD, LCItems.COIN_CHOCOLATE_DIAMOND, new ExchangeCommand(ExchangeDirection.UP, LCItems.COIN_CHOCOLATE_EMERALD)));
        //Diamond <-> Netherite
        builder.addButton(exchangeSingle(144, 61, LCItems.COIN_CHOCOLATE_NETHERITE, LCItems.COIN_CHOCOLATE_DIAMOND, new ExchangeCommand(ExchangeDirection.DOWN, LCItems.COIN_CHOCOLATE_NETHERITE)));
        builder.addButton(exchangeSingle(144, 88, LCItems.COIN_CHOCOLATE_DIAMOND, LCItems.COIN_CHOCOLATE_NETHERITE, new ExchangeCommand(ExchangeDirection.UP, LCItems.COIN_CHOCOLATE_DIAMOND)));
    }

    private static ATMExchangeButtonData exchangeAllUpDefault() {
        return new ATMExchangeButtonData(5,34,82,new ExchangeAllCommand(ExchangeDirection.UP,3),
                Lists.newArrayList(
                        new ATMItemIcon(-2,1, LCItems.COIN_COPPER.get()),
                        new ATMArrowIcon(10,6,ArrowType.RIGHT),
                        new ATMItemIcon(12,1, LCItems.COIN_IRON.get()),
                        new ATMArrowIcon(24,6,ArrowType.RIGHT),
                        new ATMItemIcon(26,1, LCItems.COIN_GOLD.get()),
                        new ATMArrowIcon(38,6,ArrowType.RIGHT),
                        new ATMItemIcon(40,1, LCItems.COIN_EMERALD.get()),
                        new ATMArrowIcon(52,6,ArrowType.RIGHT),
                        new ATMItemIcon(54,1, LCItems.COIN_DIAMOND.get()),
                        new ATMArrowIcon(66,6,ArrowType.RIGHT),
                        new ATMItemIcon(68,1, LCItems.COIN_NETHERITE.get())
                )
        );
    }

    private static ATMExchangeButtonData exchangeAllUpChocolate() {
        return new ATMExchangeButtonData(5,34,82,new ExchangeAllCommand(ExchangeDirection.UP,3),
                Lists.newArrayList(
                        new ATMItemIcon(-2,1, LCItems.COIN_CHOCOLATE_COPPER.get()),
                        new ATMArrowIcon(10,6,ArrowType.RIGHT),
                        new ATMItemIcon(12,1, LCItems.COIN_CHOCOLATE_IRON.get()),
                        new ATMArrowIcon(24,6,ArrowType.RIGHT),
                        new ATMItemIcon(26,1, LCItems.COIN_CHOCOLATE_GOLD.get()),
                        new ATMArrowIcon(38,6,ArrowType.RIGHT),
                        new ATMItemIcon(40,1, LCItems.COIN_CHOCOLATE_EMERALD.get()),
                        new ATMArrowIcon(52,6,ArrowType.RIGHT),
                        new ATMItemIcon(54,1, LCItems.COIN_CHOCOLATE_DIAMOND.get()),
                        new ATMArrowIcon(66,6,ArrowType.RIGHT),
                        new ATMItemIcon(68,1, LCItems.COIN_CHOCOLATE_NETHERITE.get())
                )
        );
    }

    private static ATMExchangeButtonData exchangeAllDownDefault() {
        return new ATMExchangeButtonData(89,34,82,new ExchangeAllCommand(ExchangeDirection.DOWN,3),
                Lists.newArrayList(
                        new ATMItemIcon(-2,1, LCItems.COIN_NETHERITE.get()),
                        new ATMArrowIcon(10,6,ArrowType.RIGHT),
                        new ATMItemIcon(12,1, LCItems.COIN_DIAMOND.get()),
                        new ATMArrowIcon(24,6,ArrowType.RIGHT),
                        new ATMItemIcon(26,1, LCItems.COIN_EMERALD.get()),
                        new ATMArrowIcon(38,6,ArrowType.RIGHT),
                        new ATMItemIcon(40,1, LCItems.COIN_GOLD.get()),
                        new ATMArrowIcon(52,6,ArrowType.RIGHT),
                        new ATMItemIcon(54,1, LCItems.COIN_IRON.get()),
                        new ATMArrowIcon(66,6,ArrowType.RIGHT),
                        new ATMItemIcon(68,1, LCItems.COIN_COPPER.get())
                )
        );
    }

    private static ATMExchangeButtonData exchangeAllDownChocolate() {
        return new ATMExchangeButtonData(89,34,82,new ExchangeAllCommand(ExchangeDirection.DOWN,3),
                Lists.newArrayList(
                        new ATMItemIcon(-2,1, LCItems.COIN_CHOCOLATE_NETHERITE.get()),
                        new ATMArrowIcon(10,6,ArrowType.RIGHT),
                        new ATMItemIcon(12,1, LCItems.COIN_CHOCOLATE_DIAMOND.get()),
                        new ATMArrowIcon(24,6,ArrowType.RIGHT),
                        new ATMItemIcon(26,1, LCItems.COIN_CHOCOLATE_EMERALD.get()),
                        new ATMArrowIcon(38,6,ArrowType.RIGHT),
                        new ATMItemIcon(40,1, LCItems.COIN_CHOCOLATE_GOLD.get()),
                        new ATMArrowIcon(52,6,ArrowType.RIGHT),
                        new ATMItemIcon(54,1, LCItems.COIN_CHOCOLATE_IRON.get()),
                        new ATMArrowIcon(66,6,ArrowType.RIGHT),
                        new ATMItemIcon(68,1, LCItems.COIN_CHOCOLATE_COPPER.get())
                )
        );
    }

    private static ATMExchangeButtonData exchangeSingle(int x, int y, Supplier<? extends ItemLike> from, Supplier<? extends ItemLike> to, ATMCommand command) {
        return new ATMExchangeButtonData(x, y, 26, command,
                Lists.newArrayList(
                        new ATMItemIcon(-2,1,from.get()),
                        new ATMArrowIcon(10,6,ArrowType.RIGHT),
                        new ATMItemIcon(12,1,to.get())
                )
        );
    }



}