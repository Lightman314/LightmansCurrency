package io.github.lightman314.lightmanscurrency.api.misc.menus.item_handlers;

import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.LCItemStackHandler;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class CoinInventory extends LCItemStackHandler
{
    private final boolean allowSideChain;
    public CoinInventory(int size) { this(size, true); }
    public CoinInventory(int size, boolean allowSideChain) { super(size); this.allowSideChain = allowSideChain; }
    public CoinInventory(Container other) { this(other, true); }
    public CoinInventory(Container other, boolean allowSideChain) {
        this(other.getContainerSize(), allowSideChain);
        for(int i = 0; i < other.getContainerSize(); ++i)
            this.setStackInSlot(i, other.getItem(i));
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) { return CoinAPI.getApi().IsAllowedInCoinContainer(stack,this.allowSideChain); }

}
