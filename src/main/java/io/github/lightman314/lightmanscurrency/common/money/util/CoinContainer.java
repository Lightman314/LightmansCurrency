package io.github.lightman314.lightmanscurrency.common.money.util;

import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class CoinContainer extends SimpleContainer
{
    private final boolean allowHidden;
    public CoinContainer(int size) { this(size, true); }
    public CoinContainer(int size, boolean allowHidden) { super(size); this.allowHidden = allowHidden; }
    public CoinContainer(Container other) { this(other, true); }
    public CoinContainer(Container other, boolean allowHidden) {
        this(other.getContainerSize(), allowHidden);
        for(int i = 0; i < other.getContainerSize(); ++i)
            this.setItem(i, other.getItem(i));
    }
    @Override
    public boolean canPlaceItem(int slot, @Nonnull ItemStack stack) { return MoneyUtil.isCoin(stack, this.allowHidden); }
}
