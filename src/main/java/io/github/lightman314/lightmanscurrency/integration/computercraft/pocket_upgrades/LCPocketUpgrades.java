package io.github.lightman314.lightmanscurrency.integration.computercraft.pocket_upgrades;

import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class LCPocketUpgrades {

    private static final DeferredRegister<UpgradeType<? extends IPocketUpgrade>> TYPE_REGISTRY = DeferredRegister.create(IPocketUpgrade.typeRegistry(), LightmansCurrency.MODID);
    public static final Supplier<UpgradeType<TerminalPocketUpgrade>> TERMINAL_UPGRADE;

    public static void init(IEventBus bus) {
        TYPE_REGISTRY.register(bus);
    }

    static {
        TERMINAL_UPGRADE = TYPE_REGISTRY.register("terminal",() -> UpgradeType.simpleWithCustomItem(TerminalPocketUpgrade::new));
    }

}
