package io.github.lightman314.lightmanscurrency.common.blockentity.handler;

import io.github.lightman314.lightmanscurrency.common.blockentity.MoneyBagBlockEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MoneyBagItemViewer implements IItemHandler {

    private final MoneyBagBlockEntity be;
    public MoneyBagItemViewer(MoneyBagBlockEntity be) { this.be = be; }

    private List<ItemStack> contents() { return this.be.viewContents(); }

    @Override
    public int getSlots() { return this.contents().size(); }
    @Override
    public ItemStack getStackInSlot(int slot) {
        List<ItemStack> contents = this.contents();
        if(slot < 0 || slot >= contents.size())
            return ItemStack.EMPTY;
        return contents.get(slot);
    }
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return ItemStack.EMPTY; }
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
    @Override
    public int getSlotLimit(int slot) { return Integer.MAX_VALUE; }
    @Override
    public boolean isItemValid(int slot, ItemStack stack) { return false; }

}
