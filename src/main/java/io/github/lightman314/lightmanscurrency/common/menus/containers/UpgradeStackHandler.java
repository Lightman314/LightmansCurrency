package io.github.lightman314.lightmanscurrency.common.menus.containers;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.LCItemStackHandler;
import io.github.lightman314.lightmanscurrency.api.upgrades.IUpgradeable;
import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public class UpgradeStackHandler extends LCItemStackHandler {

    public static final Codec<UpgradeStackHandler> CODEC = NonNullList.codecOf(ItemStack.OPTIONAL_CODEC)
            .xmap(UpgradeStackHandler::new,h -> h.stacks);

    public static final UpgradeStackHandler EMPTY = new UpgradeStackHandler(0);

    private IUpgradeable parent;
    public UpgradeStackHandler(NonNullList<ItemStack> contents) { super(contents); }
    public UpgradeStackHandler(int count) { super(count); }

    public UpgradeStackHandler setParent(IUpgradeable parent) { this.parent = parent; return this; }
    @Override
    public UpgradeStackHandler withListener(Runnable listener) { super.withListener(listener); return this; }

    @Override
    public int getSlotLimit(int slot) { return 1; }
    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if(this.stacks.isEmpty())
            return false;
        return stack.getItem() instanceof UpgradeItem upgrade && this.parent.allowUpgrade(upgrade) && UpgradeItem.noUniqueConflicts(upgrade, this);
    }

}
