package io.github.lightman314.lightmanscurrency.integration.computercraft.pocket_upgrades;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.pocket.IPocketAccess;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.TerminalPeripheral;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class TerminalPocketUpgrade implements IPocketUpgrade {

    private final ResourceLocation id;
    private final ItemStack item;
    public TerminalPocketUpgrade(ResourceLocation id, ItemStack item) { this.id = id; this.item = item; }

    @Override
    public ResourceLocation getUpgradeID() { return this.id; }
    @Override
    public String getUnlocalisedAdjective() { return "Terminal"; }

    @Override
    public ItemStack getCraftingItem() { return this.item.copy(); }
    @Override
    @Nullable
    public IPeripheral createPeripheral(IPocketAccess pocketComputer) { return TerminalPeripheral.INSTANCE; }

}