package io.github.lightman314.lightmanscurrency.integration.computercraft.pocket_upgrades;

import dan200.computercraft.api.pocket.PocketUpgradeSerialiser;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class LCPocketUpgrades {

    private static final DeferredRegister<PocketUpgradeSerialiser<?>> TYPE_REGISTRY = DeferredRegister.create(PocketUpgradeSerialiser.registryId(), LightmansCurrency.MODID);
    public static final Supplier<PocketUpgradeSerialiser<TerminalPocketUpgrade>> TERMINAL_UPGRADE;
    public static final Supplier<PocketUpgradeSerialiser<ATMPocketUpgrade>> ATM_UPGRADE;

    public static void init(IEventBus bus) {
        TYPE_REGISTRY.register(bus);
    }

    static {
        TERMINAL_UPGRADE = TYPE_REGISTRY.register("terminal",() -> PocketUpgradeSerialiser.simpleWithCustomItem(TerminalPocketUpgrade::new));
        ATM_UPGRADE = TYPE_REGISTRY.register("atm",() -> PocketUpgradeSerialiser.simpleWithCustomItem(ATMPocketUpgrade::new));
    }

}