package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.*;
import io.github.lightman314.lightmanscurrency.core.neoforge.LCCraftingConditions;
import io.github.lightman314.lightmanscurrency.core.neoforge.LCDataAttachments;
import io.github.lightman314.lightmanscurrency.core.neoforge.LCHolderSetTypes;
import net.neoforged.bus.api.IEventBus;

public final class LCRegistrySetup {

    private LCRegistrySetup() {}

    public static void initialize(IEventBus bus)
    {

        //Vanilla Registries
        LCItems.REGISTER.register(bus);
        LCBlocks.REGISTER.register(bus);
        LCBlockEntities.REGISTER.register(bus);
        LCSounds.REGISTER.register(bus);
        LCCreativeGroups.REGISTER.register(bus);
        LCDataComponents.REGISTER.register(bus);
        LCEnchantmentDataComponents.REGISTER.register(bus);
        LCMenuTypes.REGISTER.register(bus);
        LCCommandArguments.REGISTER.register(bus);
        LCRecipeSerializers.REGISTER.register(bus);
        LCRecipeTypes.REGISTER.register(bus);
        LCRecipeBookCategories.REGISTER.register(bus);
        LCGameRules.REGISTER.register(bus);
        LCLootItemFunctions.REGISTER.register(bus);

        //NeoForge Registries
        LCDataAttachments.REGISTER.register(bus);
        LCHolderSetTypes.REGISTER.register(bus);
        LCCraftingConditions.REGISTER.register(bus);

        //Lightman's Currency Registries
        //Money
        LCMoneyValueTypes.REGISTER.register(bus);
        LCMoneyValueSourceTypes.REGISTER.register(bus);
        LCMoneyValueHelpers.REGISTER.register(bus);
        //Coins
        LCCoinDisplaySerializers.REGISTER.register(bus);
        LCATMIconTypes.REGISTER.register(bus);
        LCATMCommandTypes.REGISTER.register(bus);
        //Ownership
        LCOwnerTypes.REGISTER.register(bus);
        LCOwnerProviders.REGISTER.register(bus);
        //Bank
        LCBankReferenceTypes.REGISTER.register(bus);
        //Trader
        LCTraderTypes.REGISTER.register(bus);
        LCTraderNodeTypes.REGISTER.register(bus);
        LCTradeTypes.REGISTER.register(bus);
        LCTradePriceTypes.REGISTER.register(bus);
        LCTradePriceReceiptTypes.REGISTER.register(bus);
        LCTradeRuleTypes.REGISTER.register(bus);
        LCPermissionTypes.REGISTER.register(bus);
        LCPermissions.REGISTER.register(bus);
        LCSettingItemTransformers.REGISTER.register(bus);
        //Upgrades
        LCUpgrades.REGISTER.register(bus);
        LCNumberSources.REGISTER.register(bus);
        //Notifications
        LCNotificationTypes.REGISTER.register(bus);
        LCNotificationCategoryTypes.REGISTER.register(bus);
        //Misc
        LCMenuValidators.REGISTER.register(bus);
        LCIconTypes.REGISTER.register(bus);
        //Data
        LCFancyDataTypes.REGISTER.register(bus);
        LCStatTypes.REGISTER.register(bus);
        //Network
        LCFancyPacketTypes.REGISTER.register(bus);

    }

}
