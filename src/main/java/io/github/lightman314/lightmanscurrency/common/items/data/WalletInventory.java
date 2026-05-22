package io.github.lightman314.lightmanscurrency.common.items.data;

import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.LCItemStackHandler;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class WalletInventory extends LCItemStackHandler {

    public WalletInventory(int size) { super(size); }
    public WalletInventory(List<ItemStack> items) { super(items); }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) { return CoinAPI.getApi().IsAllowedInCoinContainer(stack.getItem(),true); }

    @Override
    public WalletInventory copy() { return new WalletInventory(this.copyStacks()); }
}
