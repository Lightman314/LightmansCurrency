package io.github.lightman314.lightmanscurrency.common.traders.item.storage;

import java.util.HashMap;
import java.util.Map;

import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.InputNode;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class TraderItemHandler<T extends TraderData> {

	private final T trader;
	private final Map<Direction, IItemHandler> handlers = new HashMap<>();
	
	public TraderItemHandler(T trader) { this.trader = trader; }
	
	public IItemHandler getHandler(Direction side) {
		if(!this.handlers.containsKey(side))
			this.handlers.put(side,new TraderHandler<>(this.trader, side));
		return this.handlers.get(side);
	}
	
	private static class TraderHandler<T extends TraderData> implements IItemHandler
	{
		private final T trader;
		private final Direction side;
		
		protected TraderHandler(T trader, Direction side) { this.trader = trader; this.side = side; }

		protected final TraderItemStorage getStorage() {
            for(TraderNode node : this.trader.getNodeIterable())
            {
                if(node instanceof IItemStorageSource source)
                    return source.getStorage();
            }
            return new TraderItemStorage();
        }
		
		protected final boolean allowsInputs() { return this.trader.findNodeValue(InputNode.TYPE,n -> n.allowInputSide(this.side),false); }
		protected final boolean allowsOutputs() { return this.trader.findNodeValue(InputNode.TYPE,n -> n.allowOutputSide(this.side),false); }

		protected final boolean isGhostSlot(int slot) { return slot >= this.getStorage().getContents().size(); }

		protected final void validateSlot(int slot) { if(slot < 0) throw new RuntimeException("Slot cannot be negative!"); }

		@Override
		public int getSlots() {
			//Return 1 more slot than we have so that we always have an empty slot that can accept new items.
			return this.getStorage().getContents().size() + 9;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			if(this.isGhostSlot(slot))
				return ItemStack.EMPTY;
			this.validateSlot(slot);
			//Return the item in that slot
			return this.getStorage().getContents().get(slot);
		}

		@Override
		public int getSlotLimit(int slot) { return this.getStorage().getMaxAmount(); }

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return this.allowsInputs() && this.getStorage().allowItem(stack) && this.allowedInGhostSlot(slot,stack);
		}

		private boolean allowedInGhostSlot(int slot, ItemStack stack)
		{
			if(slot >= this.getStorage().getContents().size())
				return this.getStorage().getContents().stream().noneMatch(s -> ItemStack.isSameItemSameComponents(s,stack));
			//Not a bonus slot, so it's always allowed
			return true;
		}
		
		public boolean allowExtraction(ItemStack stack) {
            for(TraderNode node : this.trader.getNodeIterable())
            {
                if(node instanceof IItemExtractionFilter filter && filter.allowExtraction(stack))
                    return true;
            }
            return false;
        }
		
		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			ItemStack copyStack = stack.copy();
			if(this.allowsInputs() && this.getStorage().allowItem(stack))
			{
				if(simulate)
				{
					int inputAmount = Math.min(this.getStorage().getFittableAmount(copyStack), copyStack.getCount());
					copyStack.shrink(inputAmount);
				}
				else
					this.getStorage().tryAddItem(copyStack);
            }
            return copyStack;
        }
		
		
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if(this.allowsOutputs())
			{
				ItemStack stackInSlot = this.getStackInSlot(slot).copy();
				if(stackInSlot.isEmpty() || !this.allowExtraction(stackInSlot))
					return ItemStack.EMPTY;
				int amountToRemove = Math.min(amount, Math.min(stackInSlot.getCount(), stackInSlot.getMaxStackSize()));
				if(amountToRemove > 0)
				{
					ItemStack result = stackInSlot.copy();
					result.setCount(amountToRemove);
					if(!simulate)
					{
						stackInSlot.setCount(amountToRemove);
						result = this.getStorage().removeItemLimited(stackInSlot);
					}
					return result;
				}
			}
			return ItemStack.EMPTY;
		}
		
	}
	
	
}
