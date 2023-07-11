package io.github.lightman314.lightmanscurrency.common.money;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

public class ATMUtil {

    public static String UpdateCommand(String oldCommand)
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
            MoneyUtil.ExchangeAllCoinsUp(coinSlots);
            return true;
        }
        //Convert defined coin upwards
        else if(command.startsWith("exchangeUp-"))
        {
            ResourceLocation coinID;
            String id = "";
            try {
                id = command.substring("exchangeUp-".length());
                coinID = new ResourceLocation(id);
                Item coinItem = ForgeRegistries.ITEMS.getValue(coinID);
                if(coinItem == null)
                {
                    LightmansCurrency.LogError("Error handling ATM Exchange command '" + command + "'.\n'" + coinID + "' is not a registered item.");
                    return false;
                }
                if(!MoneyUtil.isCoin(coinItem))
                {
                    LightmansCurrency.LogError("Error handling ATM Exchange command '" + command + "'.\n'" + coinID + "' is not a coin.");
                    return false;
                }
                if(MoneyUtil.getUpwardConversion(coinItem) == null)
                {
                    LightmansCurrency.LogError("Error handling ATM Exchange command '" + command + "'.\n'" + coinID + "' is the largest visible coin in its chain, and thus cannot be exchanged any larger.");
                    return false;
                }
                MoneyUtil.ExchangeCoinsUp(coinSlots, coinItem);
                return true;
            } catch(Exception e) { LightmansCurrency.LogError("Error handling ATM Exchange command '" + command + "'.\n'" + id + "' could not be parsed as an item id.", e);}
        }
        else if(command.contentEquals("exchangeAllDown"))
        {
            MoneyUtil.ExchangeAllCoinsDown(coinSlots);
            return true;
        }
        else if(command.startsWith("exchangeDown-"))
        {
            String id = "";
            try {
                id = command.substring("exchangeDown-".length());
                ResourceLocation coinID = new ResourceLocation(id);
                Item coinItem = ForgeRegistries.ITEMS.getValue(coinID);
                if(coinItem == null)
                {
                    LightmansCurrency.LogError("Error handling ATM Exchange command '" + command + "'.\n'" + coinID + "' is not a registered item.");
                    return false;
                }
                if(!MoneyUtil.isCoin(coinItem))
                {
                    LightmansCurrency.LogError("Error handling ATM Exchange command '" + command + "'.\n'" + coinID + "' is not a coin.");
                    return false;
                }
                if(MoneyUtil.getDownwardConversion(coinItem) == null)
                {
                    LightmansCurrency.LogError("Error handling ATM Exchange command '" + command + "'.\n'" + coinID + "' is the smallest known coin, and thus cannot be exchanged any smaller.");
                    return false;
                }
                MoneyUtil.ExchangeCoinsDown(coinSlots, coinItem);
                return true;
            } catch(Exception e) { LightmansCurrency.LogError("Error handling ATM Exchange command '" + command + "'.\n'" + id + "' could not be parsed as an item id.", e); }
        }
        else
            LightmansCurrency.LogError("'" + command + "' is not a valid ATM Exchange command.");
        return false;
    }

}
