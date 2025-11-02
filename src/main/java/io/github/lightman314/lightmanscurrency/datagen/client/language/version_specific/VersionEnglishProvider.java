package io.github.lightman314.lightmanscurrency.datagen.client.language.version_specific;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCVersionText;
import io.github.lightman314.lightmanscurrency.datagen.client.language.TranslationAttachment;
import io.github.lightman314.lightmanscurrency.datagen.client.language.TranslationProvider;
import net.minecraft.data.PackOutput;

public class VersionEnglishProvider extends TranslationAttachment {

    public VersionEnglishProvider(PackOutput output,TranslationProvider parent) { super(output,parent); }

    @Override
    protected void createTranslations() {
        //1.20 exclusive as in 1.21 this is part of the enchantment datagen
        this.translateConfigOption(LCConfig.SERVER.moneyMendingItemOverrides,"MM Item Overrides","A list of base cost overrides to be applied to specific items!");
        this.translateConfigOption(LCConfig.SERVER.moneyMendingBonusForEnchantments,"MM Enchantment  Overrides","A list of bonus costs to be applied to items with the given enchantments");

        //1.20 exclusive as LDI is not present in 1.21+
        //Compat -> LDI
        this.translateConfigSection(LCConfig.SERVER,"compat.ldi","Lightman's Discord Compat Settings");
        this.translateConfigOption(LCConfig.SERVER.ldiCurrencyChannel,"Currency Channel","The channel where users can run the currency commands and where currency related announcements will be made.");
        this.translateConfigOption(LCConfig.SERVER.ldiCurrencyCommandPrefix,"Currency Command Prefix","Prefix for currency commands.");
        this.translateConfigOption(LCConfig.SERVER.ldiLimitSearchToNetworkTraders,"Limit Search to Network Traders","Whether the !search command should limit its search results to only Network Traders, or if it should list all traders.");

        //Compat -> LDI -> Notifications
        this.translateConfigSection(LCConfig.SERVER,"compat.ldi.notifications","Currency Bot Notification Options");
        this.translateConfigOption(LCConfig.SERVER.ldiNetworkTraderNotification,"Network Trader Creation",
                "Whether a notification will appear in the currency bot channel when a Network Trader is created.",
                "Notification will have a 60 second delay to allow them time to customize the traders name, etc.");
        this.translateConfigOption(LCConfig.SERVER.ldiAuctionCreateNotification,"Auction Creation",
                "Whether a notification will appear in the currency bot channel when a player starts an auction.");
        this.translateConfigOption(LCConfig.SERVER.ldiAuctionPersistentCreateNotification,"Persistent Auction Creation",
                "Whether a notification will appear in the currency bot channel when a Persistent Auction is created automatically.",
                "Requires that Auction Creation notifications also be enabled.");
        this.translateConfigOption(LCConfig.SERVER.ldiAuctionCancelNotification,"Auction Cancelled",
                "Whether a notification will appear in the currency bot channel when an Auction is cancelled in the Auction House.");
        this.translateConfigOption(LCConfig.SERVER.ldiAuctionWinNotification,"Auction Won",
                "Whether a notification will appear in the currency bot channel when an Auction is completed and had a bidder.");

        // Config Labels
        this.translate(LCVersionText.CONFIG_ITEM_OVERRIDE_LABEL_MONEY,"Additional Repair Cost");
        this.translate(LCVersionText.CONFIG_ITEM_OVERRIDE_LABEL_ITEMS,"Item IDs or Tags");
        this.translate(LCVersionText.CONFIG_ITEM_OVERRIDE_LABEL_ITEMS_TOOLTIP,"Start tags with a # (e.g. #minecraft:swords)");

        this.translate(LCVersionText.CONFIG_ENCHANTMENT_BONUS_LABEL_MONEY,"Additional Repair Cost");
        this.translate(LCVersionText.CONFIG_ENCHANTMENT_BONUS_LABEL_ENCHANTMENT,"Enchantment ID");
        this.translate(LCVersionText.CONFIG_ENCHANTMENT_BONUS_LABEL_LEVEL,"Max Level Multiplier (0 for unlimited)");

    }

}
