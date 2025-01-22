package io.github.lightman314.lightmanscurrency.common.menus.containers;

import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class CoinContainer extends SimpleContainer
{
    private final boolean allowSideChain;
    public CoinContainer(int size) { this(size, true); }
    public CoinContainer(int size, boolean allowSideChain) { super(size); this.allowSideChain = allowSideChain; }
    public CoinContainer(Container other) { this(other, true); }
    public CoinContainer(Container other, boolean allowSideChain) {
        this(other.getContainerSize(), allowSideChain);
        for(int i = 0; i < other.getContainerSize(); ++i)
            this.setItem(i, other.getItem(i));
    }
    @Override
    public boolean canPlaceItem(int slot, @Nonnull ItemStack stack) { return stack.getItem() == ModItems.COIN_ANCIENT.get() || CoinAPI.API.IsCoin(stack, this.allowSideChain); }

}
