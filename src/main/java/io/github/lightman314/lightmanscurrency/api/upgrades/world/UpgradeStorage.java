package io.github.lightman314.lightmanscurrency.api.upgrades.world;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.helpers.resource.NormalItemStorage;
import io.github.lightman314.lightmanscurrency.api.upgrades.IUpgradeable;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.Nullable;
import java.util.Iterator;

public class UpgradeStorage extends NormalItemStorage implements Iterable<Pair<UpgradeType,ItemStack>> {

    private final IUpgradeable upgradeable;
    public UpgradeStorage(int size, IUpgradeable upgradeable) {
        super(size);
        this.upgradeable = upgradeable;
    }

    //Only allow 1 upgrade in each slot
    @Override
    public long getCapacityAsLong(int index,ItemResource resource) { return 1; }

    public boolean hasUpgrade(UpgradeType upgrade)
    {
        for(ItemStack s : this.storage)
        {
            if(s.has(LCDataComponents.UPGRADE_TYPE))
            {
                if(s.get(LCDataComponents.UPGRADE_TYPE) == upgrade)
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        //Check if upgradeable supports the upgrade
        if(resource.has(LCDataComponents.UPGRADE_TYPE))
        {
            UpgradeType upgrade = resource.get(LCDataComponents.UPGRADE_TYPE);
            return IUpgradeable.isUpgradeAllowed(this.upgradeable,upgrade,resource);
        }
        return false;
    }

    @Override
    public Iterator<Pair<UpgradeType, ItemStack>> iterator() { return new UpgradeIterator(this); }

    private static final class UpgradeIterator implements Iterator<Pair<UpgradeType,ItemStack>>
    {
        private final NonNullList<ItemStack> storage;
        private int lastIndex = -1;
        private UpgradeIterator(UpgradeStorage storage) { this.storage = storage.storage; }

        @Nullable
        private Pair<UpgradeType,ItemStack> getNext(boolean updateLast) {
            for(int i = this.lastIndex + 1;i < this.storage.size(); ++i)
            {
                ItemStack stack = this.storage.get(i);
                if(stack.has(LCDataComponents.UPGRADE_TYPE))
                {
                    if(updateLast)
                        this.lastIndex = i;
                    return Pair.of(stack.get(LCDataComponents.UPGRADE_TYPE),stack);
                }
            }
            return null;
        }
        @Override
        public boolean hasNext() { return this.getNext(false) != null; }
        @Override
        public Pair<UpgradeType,ItemStack> next() { return this.getNext(true); }

    }

}