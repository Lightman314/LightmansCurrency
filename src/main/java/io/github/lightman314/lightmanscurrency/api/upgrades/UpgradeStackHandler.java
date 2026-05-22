package io.github.lightman314.lightmanscurrency.api.upgrades;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.LCItemStackHandler;
import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.util.ItemHandlerUtil;
import io.github.lightman314.lightmanscurrency.util.ListUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class UpgradeStackHandler extends LCItemStackHandler implements Iterable<Pair<UpgradeType,UpgradeData>> {

    public static final Codec<UpgradeStackHandler> CODEC = NonNullList.codecOf(ItemStack.OPTIONAL_CODEC)
            .xmap(UpgradeStackHandler::new,h -> h.stacks);

    public static final UpgradeStackHandler EMPTY = new UpgradeStackHandler(0);

    private IUpgradeable parent;
    public UpgradeStackHandler(NonNullList<ItemStack> contents) { super(contents); }
    public UpgradeStackHandler(int count) { super(count); }

    public boolean hasUpgrade(UpgradeType upgrade) {
        for(var entry : this)
        {
            if(entry.getFirst() == upgrade)
                return true;
        }
        return false;
    }

    public UpgradeStackHandler setParent(IUpgradeable parent) { this.parent = parent; return this; }
    @Override
    public UpgradeStackHandler withListener(Runnable listener) { super.withListener(listener); return this; }

    public final void load(List<ItemStack> items) { this.stacks = ItemHandlerUtil.loadList(items); }

    @Override
    public int getSlotLimit(int slot) { return 1; }
    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if(this.stacks.isEmpty())
            return false;
        return stack.getItem() instanceof UpgradeItem upgrade && this.parent.allowUpgrade(upgrade) && UpgradeItem.noUniqueConflicts(upgrade, this);
    }

    @Override
    public Iterator<Pair<UpgradeType,UpgradeData>> iterator() { return new UpgradeIterator(); }

    private class UpgradeIterator implements Iterator<Pair<UpgradeType,UpgradeData>>
    {
        private int nextIndex = 0;
        @Nullable
        private Pair<UpgradeType,UpgradeData> nextCache = null;

        @Nullable
        private Pair<UpgradeType,UpgradeData> findNext()
        {
            if(this.nextCache != null)
                return this.nextCache;
            for(int i = this.nextIndex; i < UpgradeStackHandler.this.stacks.size(); ++i)
            {
                ItemStack stack = UpgradeStackHandler.this.stacks.get(i);
                if(!stack.isEmpty() && stack.getItem() instanceof IUpgradeItem upgradeItem)
                {
                    this.nextCache = Pair.of(upgradeItem.getUpgradeType(),UpgradeItem.getUpgradeData(stack));
                    this.nextIndex = i + 1;
                    return this.nextCache;
                }
            }
            return null;
        }

        @Override
        public boolean hasNext() { return this.findNext() != null; }

        @Override
        public Pair<UpgradeType, UpgradeData> next() {
            Pair<UpgradeType,UpgradeData> next = this.findNext();
            if(next == null)
                throw new NoSuchElementException();
            //Clear the cache so that we'll find the next value after this
            this.nextCache = null;
            return next;
        }
    }

}
