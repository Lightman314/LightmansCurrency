package io.github.lightman314.lightmanscurrency.api.misc.menus.slots;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

/**
 * Copied from {@link EasyItemHandlerSlot}, but extending the EasySlot class for easy toggling
 */
public class EasyItemHandlerSlot extends EasySlot {
    private final IItemHandlerModifiable itemHandler;
    protected final int index;

    public EasyItemHandlerSlot(IItemHandlerModifiable itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler,index, xPosition, yPosition);
        this.itemHandler = itemHandler;
        this.index = index;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        return super.mayPlace(stack) && this.itemHandler.isItemValid(index, stack);
    }

    @Override
    public ItemStack getItem() { return this.getItemHandler().getStackInSlot(this.index); }

    // Override if your IItemHandler does not implement IItemHandlerModifiable
    @Override
    public void set(ItemStack stack) {
        this.getItemHandler().setStackInSlot(this.index, stack);
        this.setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return this.itemHandler.getSlotLimit(this.index);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Math.min(stack.getMaxStackSize(), this.itemHandler.getSlotLimit(this.index));
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return !this.getItemHandler().extractItem(index, 1, true).isEmpty();
    }

    @Override
    public ItemStack remove(int amount) {
        return this.getItemHandler().extractItem(index, amount, false);
    }

    public IItemHandlerModifiable getItemHandler() {
        return itemHandler;
    }

}