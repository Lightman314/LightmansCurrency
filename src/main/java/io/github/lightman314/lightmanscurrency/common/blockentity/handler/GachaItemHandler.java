package io.github.lightman314.lightmanscurrency.common.blockentity.handler;

import io.github.lightman314.lightmanscurrency.common.traders.gacha.GachaStorage;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.GachaTrader;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GachaItemHandler {

    private final GachaTrader trader;
    public GachaItemHandler(GachaTrader trader) { this.trader = trader; }

    private final Map<Direction, IItemHandler> handlers = new HashMap<>();
    public IItemHandler getHandler(Direction side)
    {
        if(!this.handlers.containsKey(side))
            this.handlers.put(side,new GachaHandler(this.trader,side));
        return this.handlers.get(side);
    }

    private static class GachaHandler implements IItemHandler
    {
        private final GachaTrader trader;
        private final Direction side;
        private GachaHandler(GachaTrader trader, Direction side) { this.trader = trader; this.side = side; }

        protected final boolean allowsInputs() { return this.trader.allowInputSide(this.side); }
        protected final boolean allowsOutputs() { return this.trader.allowOutputSide(this.side); }

        @Override
        public int getSlots() { return this.trader.getStorage().getContents().size() + 1; }

        @Override
        public ItemStack getStackInSlot(int slot) { return this.trader.getStorage().getStackInSlot(slot); }

        @Override
        public int getSlotLimit(int slot) { return this.trader.getMaxItems(); }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) { return this.allowsInputs(); }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            ItemStack copyStack = stack.copy();
            if(this.allowsInputs())
            {
                if(simulate)
                {
                    int inputAmount = Math.min(copyStack.getCount(),this.trader.getStorage().getSpace());
                    copyStack.shrink(inputAmount);
                }
                else
                {
                    if(this.trader.getStorage().insertItem(copyStack))
                        this.trader.markStorageDirty();
                }
            }
            return copyStack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if(this.allowsOutputs())
            {
                GachaStorage storage = this.trader.getStorage();
                if(simulate)
                {
                    ItemStack stack = storage.getStackInSlot(slot);
                    return stack.copyWithCount(Math.min(amount,stack.getCount()));
                }
                else
                {
                    ItemStack result = storage.removeItem(slot,amount);
                    if(!result.isEmpty())
                        this.trader.markStorageDirty();
                    return result;
                }
            }
            return ItemStack.EMPTY;
        }

    }

}
