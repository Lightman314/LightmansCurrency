package io.github.lightman314.lightmanscurrency.datagen.integration;

import dan200.computercraft.api.pocket.IPocketUpgrade;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.integration.computercraft.pocket_upgrades.ATMPocketUpgrade;
import io.github.lightman314.lightmanscurrency.integration.computercraft.pocket_upgrades.TerminalPocketUpgrade;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class LCComputerCraftProvider {

    public static void datapackAddon(RegistrySetBuilder builder)
    {
        builder.add(IPocketUpgrade.REGISTRY,LCComputerCraftProvider::bootstrap);
    }

    private static void bootstrap(@Nonnull BootstrapContext<IPocketUpgrade> context)
    {
        context.register(makeKey("terminal_block"),new TerminalPocketUpgrade(new ItemStack(ModBlocks.TERMINAL.get())));
        context.register(makeKey("terminal_portable"),new TerminalPocketUpgrade(new ItemStack(ModItems.PORTABLE_TERMINAL.get())));
        context.register(makeKey("gem_terminal_block"),new TerminalPocketUpgrade(new ItemStack(ModBlocks.GEM_TERMINAL.get())));
        context.register(makeKey("gem_terminal_portable"),new TerminalPocketUpgrade(new ItemStack(ModItems.PORTABLE_GEM_TERMINAL.get())));
        context.register(makeKey("atm_block"),new ATMPocketUpgrade(new ItemStack(ModBlocks.ATM.get())));
        context.register(makeKey("atm_portable"),new ATMPocketUpgrade(new ItemStack(ModItems.PORTABLE_ATM.get())));
    }

    private static ResourceKey<IPocketUpgrade> makeKey(String key) { return makeKey(VersionUtil.lcResource(key)); }
    private static ResourceKey<IPocketUpgrade> makeKey(ResourceLocation key) { return ResourceKey.create(IPocketUpgrade.REGISTRY,key); }

}
