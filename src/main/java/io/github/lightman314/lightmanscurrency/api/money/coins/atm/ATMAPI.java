package io.github.lightman314.lightmanscurrency.api.money.coins.atm;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.ATMIconData;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.IconType;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin.SimpleArrowIcon;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.data.ATMPageManager;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMExchangeButton;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ATMAPI {

    private static final Map<String,IconType> registeredIcons = new HashMap<>();

    public static void Setup() {
        registerIconType(ItemIcon.TYPE);
        registerIconType(SimpleArrowIcon.TYPE);
        registerIconType(SpriteIcon.TYPE);
    }

    public static void registerIconType(@Nonnull IconType iconType)
    {
        String type = iconType.type.toString();
        if(registeredIcons.containsKey(type))
        {
            if(registeredIcons.get(type) == iconType)
                LightmansCurrency.LogWarning("ATM Icon Type '" + type + "' was registered twice.");
            else
                LightmansCurrency.LogWarning("Attempted to registerType an ATM Icon Type of type '" + type + "', but an ATM Icon of that type is already registered.");
        }
        registeredIcons.put(type, iconType);
        LightmansCurrency.LogInfo("ATM Icon Type '" + type + "' has been registered successfully.");
    }

    public static ATMIconData parseIcon(@Nonnull JsonObject data, @Nonnull HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException {
        String type = GsonHelper.getAsString(data, "type");
        if(registeredIcons.containsKey(type))
            return registeredIcons.get(type).parse(data,lookup);
        else
            throw new JsonSyntaxException("No ATM Icon of type '" + type + "'. Unable to parse.");
    }

    @Nonnull
    public static ATMPageManager getATMPageManager(@Nonnull Player player, @Nonnull Consumer<Object> addChild, @Nonnull Consumer<Object> removeChild, @Nonnull Consumer<String> commandProcessor) { return ATMPageManager.create(player, addChild, removeChild, commandProcessor); }
    public static ATMPageManager getATMPageManager(@Nonnull Player player, @Nonnull Consumer<Object> addChild, @Nonnull Consumer<Object> removeChild, @Nonnull Consumer<String> commandProcessor, @Nonnull Predicate<ATMExchangeButton> selected) { return ATMPageManager.create(player, addChild, removeChild, commandProcessor,selected); }

    @Nonnull
    public static String UpdateCommand(@Nonnull String oldCommand)
    {
        if(oldCommand.contains("convert"))
            return oldCommand.replace("convert", "exchange");
        return oldCommand;
    }

    public static boolean ExecuteATMExchangeCommand(@Nonnull Container coinSlots, @Nonnull String command)
    {
        command = UpdateCommand(command);
        if(command.contentEquals("exchangeAllUp"))
        {
            CoinAPI.API.CoinExchangeAllUp(coinSlots);
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
                ChainData chain = CoinAPI.API.ChainDataOfCoin(coinItem);
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
                CoinAPI.API.CoinExchangeUp(coinSlots, coinItem);
                return true;
            } catch(ResourceLocationException e) { LightmansCurrency.LogError("Error handling ATM Exchange command '" + command + "'.\n'" + id + "' could not be parsed as an item id.", e);}
        }
        else if(command.contentEquals("exchangeAllDown"))
        {
            CoinAPI.API.CoinExchangeAllDown(coinSlots);
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
                ChainData chain = CoinAPI.API.ChainDataOfCoin(coinItem);
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
                CoinAPI.API.CoinExchangeDown(coinSlots, coinItem);
                return true;
            } catch(ResourceLocationException e) { LightmansCurrency.LogError("Error handling ATM Exchange command '" + command + "'.\n'" + id + "' could not be parsed as an item id.", e); }
        }
        else
            LightmansCurrency.LogError("'" + command + "' is not a valid ATM Exchange command.");
        return false;
    }

}
