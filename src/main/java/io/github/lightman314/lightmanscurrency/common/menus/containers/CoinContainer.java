package io.github.lightman314.lightmanscurrency.common.menus.containers;

import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
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
    public boolean canPlaceItem(int slot, ItemStack stack) { return CoinAPI.getApi().IsAllowedInCoinContainer(stack,this.allowSideChain); }

}
