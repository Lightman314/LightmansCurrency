package io.github.lightman314.lightmanscurrency.integration.computercraft.pocket_upgrades;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.pocket.IPocketAccess;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.TerminalPeripheral;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class TerminalPocketUpgrade implements IPocketUpgrade {

    private final ItemStack item;
    public TerminalPocketUpgrade(ItemStack item) { this.item = item; }

    @Override
    public UpgradeType<TerminalPocketUpgrade> getType() { return LCPocketUpgrades.TERMINAL_UPGRADE.get(); }
    @Override
    public Component getAdjective() { return this.getCraftingItem().getHoverName(); }
    @Override
    public ItemStack getCraftingItem() { return this.item.copy(); }
    @Override
    @Nullable
    public IPeripheral createPeripheral(IPocketAccess pocketComputer) { return new TerminalPeripheral(); }

}
