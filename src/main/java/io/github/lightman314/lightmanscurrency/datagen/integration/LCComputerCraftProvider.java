package io.github.lightman314.lightmanscurrency.datagen.integration;

import dan200.computercraft.api.pocket.PocketUpgradeDataProvider;
import dan200.computercraft.api.pocket.PocketUpgradeSerialiser;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.integration.computercraft.pocket_upgrades.LCPocketUpgrades;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class LCComputerCraftProvider extends PocketUpgradeDataProvider {

    public LCComputerCraftProvider(PackOutput output) { super(output); }

    @Override
    protected void addUpgrades(Consumer<Upgrade<PocketUpgradeSerialiser<?>>> consumer) {
        consumer.accept(createTerminalUpgrade("terminal_block",ModBlocks.TERMINAL));
        consumer.accept(createTerminalUpgrade("terminal_portable",ModItems.PORTABLE_TERMINAL));
        consumer.accept(createTerminalUpgrade("gem_terminal_block",ModBlocks.GEM_TERMINAL));
        consumer.accept(createTerminalUpgrade("gem_terminal_portable",ModItems.PORTABLE_GEM_TERMINAL));
    }

    private static Upgrade<PocketUpgradeSerialiser<?>> createTerminalUpgrade(String id, Supplier<? extends ItemLike> item)
    {
        return new Upgrade<>(VersionUtil.lcResource(id),LCPocketUpgrades.TERMINAL_UPGRADE.get(),json -> json.addProperty("item",ForgeRegistries.ITEMS.getKey(item.get().asItem()).toString()));
    }

}
