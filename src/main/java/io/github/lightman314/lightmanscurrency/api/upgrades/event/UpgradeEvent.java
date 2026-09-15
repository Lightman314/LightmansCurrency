package io.github.lightman314.lightmanscurrency.api.upgrades.event;

import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.upgrades.IUpgradeable;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import java.util.List;
import java.util.function.Supplier;

public abstract class UpgradeEvent extends Event {

    private final UpgradeType upgrade;
    public final UpgradeType getUpgrade() { return this.upgrade; }
    public final boolean isUpgrade(ItemStack upgradeItem) {
        if(upgradeItem.has(LCDataComponents.UPGRADE_TYPE))
            return upgradeItem.get(LCDataComponents.UPGRADE_TYPE).get().is(this.upgrade);
        return false;
    }
    public final boolean isUpgrade(Supplier<? extends UpgradeType> upgrade) { return this.isUpgrade(upgrade.get()); }
    public final boolean isUpgrade(UpgradeType upgrade) { return this.upgrade == upgrade; }
    protected UpgradeEvent(UpgradeType upgrade) { this.upgrade = upgrade; }

    /**
     * Event called whenever an {@link io.github.lightman314.lightmanscurrency.api.upgrades.world.UpgradeStorage UpgradeStorage} needs to check whether an upgrade can be inserted into the storage.<br>
     * Will not be called if the upgrade is unique and already present to avoid de-uniquifying an upgrade type.<br>
     * Cancelling the event will not alter the {@link #allowed} state, it will simply prevent other mods from listening to the event.
     */
    public static class AllowUpgradeEvent extends UpgradeEvent implements ICancellableEvent {

        private boolean allowed;
        public boolean isAllowed() { return this.allowed; }
        public void setAllowed(boolean allowed) { this.allowed = allowed; }

        private final IUpgradeable host;
        public IUpgradeable getHost() { return this.host; }
        private final DataComponentGetter itemState;
        public DataComponentGetter getItemState() { return this.itemState; }

        public AllowUpgradeEvent(IUpgradeable host,UpgradeType upgrade,DataComponentGetter itemState)
        {
            super(upgrade);
            this.host = host;
            this.itemState = itemState;
            this.allowed = host.allowUpgrade(upgrade);
        }

    }

    /**
     * Called the first time {@link UpgradeType#getTargets()} is called to collect the list of valid targets for the upgrade.<br>
     * Can be used to inform the upgrade type that it's usable on a machine added by an addon mod.
     */
    public static class CollectUpgradeTargetsEvent extends UpgradeEvent {

        private final List<Component> targets;
        public CollectUpgradeTargetsEvent(UpgradeType upgrade,List<Component> targets)
        {
            super(upgrade);
            this.targets = targets;
        }

        public void addTarget(Component target) { this.targets.add(target.copy()); }
        public void addTarget(TextEntry target) { this.targets.add(target.get()); }

        public void addTarget(ItemLike item) { this.addTarget(new ItemStack(item).getItemName()); }
        public void addTarget(Block block) { this.addTarget(block.getName()); }
        public void addTarget(Supplier<? extends ItemLike> item) { this.addTarget(item.get()); }

    }
}
