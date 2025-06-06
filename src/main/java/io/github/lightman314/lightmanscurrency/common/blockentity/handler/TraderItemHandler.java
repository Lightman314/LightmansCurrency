package io.github.lightman314.lightmanscurrency.common.blockentity.handler;

import java.util.HashMap;
import java.util.Map;

import io.github.lightman314.lightmanscurrency.common.traders.InputTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class TraderItemHandler<T extends InputTraderData & TraderItemHandler.IItemStorageProvider> {

	private final T trader;
	private final Map<Direction, IItemHandler> handlers = new HashMap<>();

	public TraderItemHandler(@Nonnull T trader)
	{
		this.trader = trader;
	}

	public IItemHandler getHandler(Direction side) {
		if(!this.handlers.containsKey(side))
			this.handlers.put(side, new TraderHandler<>(this.trader, side));
		return this.handlers.get(side);
	}

	private static class TraderHandler<T extends InputTraderData & IItemStorageProvider> implements IItemHandler
	{
		private final T trader;
		private final Direction side;

		protected TraderHandler(T trader, Direction side) { this.trader = trader; this.side = side; }

		protected final TraderItemStorage getStorage() { return this.trader.getStorage(); }

		protected final boolean allowsInputs() { return this.trader.allowInputSide(this.side); }
		protected final boolean allowsOutputs() { return this.trader.allowOutputSide(this.side); }

		protected final boolean isGhostSlot(int slot) { return slot >= this.getStorage().getContents().size(); }

		protected final void validateSlot(int slot) { if(slot < 0) throw new RuntimeException("Slot cannot be negative!"); }

		@Override
		public int getSlots() {
			//Return 1 more slot than we have so that we always have an empty slot that can accept new items.
			return this.getStorage().getContents().size() + 9;
		}

		@Nonnull
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
		public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
			return this.allowsInputs() && this.getStorage().allowItem(stack) && this.allowedInGhostSlot(slot,stack);
		}



		private boolean allowedInGhostSlot(int slot, @Nonnull ItemStack stack)
		{
			if(slot >= this.getStorage().getContents().size())
				return this.getStorage().getContents().stream().noneMatch(s -> InventoryUtil.ItemMatches(s,stack));
			//Not a bonus slot, so it's always allowed
			return true;
		}

		public boolean allowExtraction(@Nonnull ItemStack stack) { return this.trader.allowExtraction(stack); }

		@Nonnull
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
				{
					this.getStorage().tryAddItem(copyStack);
					this.trader.markStorageDirty();
				}
			}
			return copyStack;
		}

		@Nonnull
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
						result = this.getStorage().removeItem(stackInSlot);
					}
					this.trader.markStorageDirty();
					return result;
				}
			}
			return ItemStack.EMPTY;
		}

	}

	public interface IItemStorageProvider
	{
		@Nonnull
		TraderItemStorage getStorage();
		void markStorageDirty();
		boolean allowExtraction(@Nonnull ItemStack stack);
	}


}