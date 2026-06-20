package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.*;
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
        LCMenuTypes.REGISTER.register(bus);



        //Lightman's Currency Registries
        //Money
        LCMoneyValueTypes.REGISTER.register(bus);
        LCMoneyValueHelpers.REGISTER.register(bus);
        //Coins
        LCCoinDisplaySerializers.REGISTER.register(bus);
        LCATMIconTypes.REGISTER.register(bus);
        LCATMCommandTypes.REGISTER.register(bus);
        //Ownership
        LCOwnerTypes.REGISTER.register(bus);
        LCOwnerProviders.REGISTER.register(bus);
        //Trader
        LCTraderTypes.REGISTER.register(bus);
        LCTraderNodeTypes.REGISTER.register(bus);
        LCPermissionTypes.REGISTER.register(bus);
        LCPermissions.REGISTER.register(bus);
        //Upgrades
        LCUpgrades.REGISTER.register(bus);
        LCNumberSources.REGISTER.register(bus);
        //Misc
        LCMenuValidators.REGISTER.register(bus);
        LCIconTypes.REGISTER.register(bus);
        //Data
        LCFancyDataTypes.REGISTER.register(bus);
        //Network
        LCFancyPacketTypes.REGISTER.register(bus);

    }

}
