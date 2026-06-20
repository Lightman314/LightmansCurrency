package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import io.github.lightman314.lightmanscurrency.api.upgrades.CapacityUpgradeType;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class LCUpgrades {
    private LCUpgrades() {}

    public static final DeferredRegister<UpgradeType> REGISTER = DeferredRegister.create(LCRegistries.Upgrades.UPGRADES,LCApi.MODID);

    public static final DeferredHolder<UpgradeType, CapacityUpgradeType> ITEM_CAPACITY = register("item_capacity",() -> new CapacityUpgradeType(LCText.Items.TOOLTIP_UPGRADE_ITEM_CAPACITY));

    public static <T extends UpgradeType> DeferredHolder<UpgradeType,T> register(String name, Supplier<T> factory) { return REGISTER.register(name,factory); }

}