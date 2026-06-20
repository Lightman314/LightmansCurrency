package io.github.lightman314.lightmanscurrency.api.upgrades.event;

import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.upgrades.IUpgradeable;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
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
    protected UpgradeEvent(UpgradeType upgrade) { this.upgrade = upgrade; }

    /**
     * Event ca
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
