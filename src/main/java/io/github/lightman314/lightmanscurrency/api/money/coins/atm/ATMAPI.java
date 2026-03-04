package io.github.lightman314.lightmanscurrency.api.money.coins.atm;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.ATMIconData;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.data.ATMPageManager;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMExchangeButton;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ATMAPI {

    public static ATMIconData parseIcon(JsonObject data,DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException {
        ResourceLocation type = ResourceLocation.parse(GsonHelper.getAsString(data, "type"));
        if(LCRegistries.ATM_ICON_TYPE.containsKey(type))
            return LCRegistries.ATM_ICON_TYPE.get(type).parse(data,context);
        else
            throw new JsonSyntaxException("No ATM Icon of type '" + type + "'. Unable to parse.");
    }

    public static ATMPageManager getATMPageManager(Player player, Consumer<Object> addChild, Consumer<Object> removeChild, Consumer<String> commandProcessor) { return ATMPageManager.create(player, addChild, removeChild, commandProcessor); }
    public static ATMPageManager getATMPageManager(Player player, Consumer<Object> addChild, Consumer<Object> removeChild, Consumer<String> commandProcessor, Predicate<ATMExchangeButton> selected) { return ATMPageManager.create(player, addChild, removeChild, commandProcessor,selected); }

    public static String UpdateCommand(String oldCommand)
    {
        if(oldCommand.contains("convert"))
            return oldCommand.replace("convert", "exchange");
        return oldCommand;
    }

    public static boolean ExecuteATMExchangeCommand(IItemHandler coinSlots, String command)
    {
        command = UpdateCommand(command);
        if(command.contentEquals("exchangeAllUp"))
        {
            CoinAPI.getApi().CoinExchangeAllUp(coinSlots);
            return true;
        }
        //Convert defined coin upwards
        else if(command.startsWith("exchangeUp-"))
        {
            ResourceLocation coinID;
            String id = "";
            try {
                id = command.substring("exchangeUp-".length());
                coinID = VersionUtil.parseResource(id);
                Item coinItem = BuiltInRegistries.ITEM.get(coinID);
                ChainData chain = CoinAPI.getApi().ChainDataOfCoin(coinItem);
                if(chain == null && !chain.findEntry(coinItem).isSideChain())
                {
                    LightmansCurrency.LogError("Error handling ATM Exchange command '" + command + "'.\n'" + coinID + "' is not a coin.");
                    return false;
                }
                if(chain.getUpperExchange(coinItem) == null)
                {
                    LightmansCurrency.LogError("Error handling ATM Exchange command '" + command + "'.\n'" + coinID + "' is the largest visible coin in its chain, and thus cannot be exchanged any larger.");
                    return false;
                }
                CoinAPI.getApi().CoinExchangeUp(coinSlots, coinItem);
                return true;
            } catch(ResourceLocationException e) { LightmansCurrency.LogError("Error handling ATM Exchange command '" + command + "'.\n'" + id + "' could not be parsed as an item id.", e);}
        }
        else if(command.contentEquals("exchangeAllDown"))
        {
            CoinAPI.getApi().CoinExchangeAllDown(coinSlots);
            return true;
        }
        else if(command.startsWith("exchangeDown-"))
        {
            String id = "";
            try {
                id = command.substring("exchangeDown-".length());
                ResourceLocation coinID = VersionUtil.parseResource(id);
                Item coinItem = BuiltInRegistries.ITEM.get(coinID);
                if(coinItem == null || coinItem == Items.AIR)
                {
                    LightmansCurrency.LogError("Error handling ATM Exchange command '" + command + "'.\n'" + coinID + "' is not a registered item.");
                    return false;
                }
                ChainData chain = CoinAPI.getApi().ChainDataOfCoin(coinItem);
                if(chain == null && !chain.findEntry(coinItem).isSideChain())
                {
                    LightmansCurrency.LogError("Error handling ATM Exchange command '" + command + "'.\n'" + coinID + "' is not a coin.");
                    return false;
                }
                if(chain.getLowerExchange(coinItem) == null)
                {
                    LightmansCurrency.LogError("Error handling ATM Exchange command '" + command + "'.\n'" + coinID + "' is the smallest known coin, and thus cannot be exchanged any smaller.");
                    return false;
                }
                CoinAPI.getApi().CoinExchangeDown(coinSlots, coinItem);
                return true;
            } catch(ResourceLocationException e) { LightmansCurrency.LogError("Error handling ATM Exchange command '" + command + "'.\n'" + id + "' could not be parsed as an item id.", e); }
        }
        else
            LightmansCurrency.LogError("'" + command + "' is not a valid ATM Exchange command.");
        return false;
    }

}
