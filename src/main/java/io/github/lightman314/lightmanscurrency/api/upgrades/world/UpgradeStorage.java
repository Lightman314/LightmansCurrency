package io.github.lightman314.lightmanscurrency.api.upgrades.world;

import io.github.lightman314.lightmanscurrency.api.helpers.resource.NormalItemStorage;
import io.github.lightman314.lightmanscurrency.api.upgrades.IUpgradeable;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeReference;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

public class UpgradeStorage extends NormalItemStorage implements Iterable<UpgradeReference> {

    private final IUpgradeable upgradeable;
    public UpgradeStorage(int size,IUpgradeable upgradeable) {
        super(size);
        this.upgradeable = upgradeable;
    }

    //Only allow 1 upgrade in each slot
    @Override
    public long getCapacityAsLong(int index,ItemResource resource) { return 1; }

    public boolean hasUpgrade(Holder<UpgradeType> upgrade) {
        try { return this.hasUpgrade(upgrade.value());
        } catch (IllegalStateException ignored) { return false; }
    }
    public boolean hasUpgrade(UpgradeType upgrade)
    {
        for(ItemStack s : this.storage)
        {
            if(s.has(LCDataComponents.UPGRADE_TYPE))
            {
                if(s.get(LCDataComponents.UPGRADE_TYPE).get() == upgrade)
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean isValid(int index,ItemResource resource) {
        //Check if upgradeable supports the upgrade
        if(resource.has(LCDataComponents.UPGRADE_TYPE))
        {
            UpgradeType upgrade = resource.get(LCDataComponents.UPGRADE_TYPE).get();
            return IUpgradeable.isUpgradeAllowed(this.upgradeable,upgrade,resource);
        }
        return false;
    }

    @Override
    public Iterator<UpgradeReference> iterator() { return new UpgradeIterator(this); }

    private static final class UpgradeIterator implements Iterator<UpgradeReference>
    {
        private final List<ItemStack> storage;
        private int lastIndex = -1;
        private UpgradeIterator(UpgradeStorage storage) { this.storage = storage.storage; }

        @Nullable
        private UpgradeReference getNext(boolean updateLast) {
            for(int i = this.lastIndex + 1;i < this.storage.size(); ++i)
            {
                ItemStack stack = this.storage.get(i);
                if(stack.has(LCDataComponents.UPGRADE_TYPE))
                {
                    if(updateLast)
                        this.lastIndex = i;
                    return new UpgradeReference(stack.get(LCDataComponents.UPGRADE_TYPE).get(),stack);
                }
            }
            return null;
        }
        @Override
        public boolean hasNext() { return this.getNext(false) != null; }
        @Override
        public UpgradeReference next() { return this.getNext(true); }

    }

}