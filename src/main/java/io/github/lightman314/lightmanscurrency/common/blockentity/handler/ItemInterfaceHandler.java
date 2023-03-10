package io.github.lightman314.lightmanscurrency.common.blockentity.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.ItemTraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.traderinterface.handlers.ConfigurableSidedHandler;
import io.github.lightman314.lightmanscurrency.common.traders.item.TraderItemStorage;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class ItemInterfaceHandler extends ConfigurableSidedHandler<IItemHandler> {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "item_interface");
	
	protected final ItemTraderInterfaceBlockEntity blockEntity;
	
	protected final TraderItemStorage getItemBuffer() { return this.blockEntity.getItemBuffer(); }
	
	private final Map<Direction,Handler> handlers = new HashMap<Direction, Handler>();
	
	public ItemInterfaceHandler(ItemTraderInterfaceBlockEntity blockEntity, Supplier<TraderItemStorage> itemBufferSource) {
		this.blockEntity = blockEntity;
	}
	
	@Override
	public ResourceLocation getType() { return TYPE; }
	
	@Override
	public String getTag() { return "ItemData"; }
	
	@Override
	public IItemHandler getHandler(Direction side) {
		if(this.inputSides.get(side) || this.outputSides.get(side))
		{
			if(!this.handlers.containsKey(side))
				this.handlers.put(side, new Handler(this, side));
			return this.handlers.get(side);
		}
		return null;
	}
	
	private static class Handler implements IItemHandler {

		final ItemInterfaceHandler handler;
		final Direction side;
		
		Handler(ItemInterfaceHandler handler, Direction side) { this.handler = handler; this.side = side; }
		
		protected final boolean allowInputs() { return this.handler.inputSides.get(this.side); }
		protected final boolean allowOutputs() { return this.handler.outputSides.get(this.side); }
		
		@Override
		public int getSlots() {
			return this.handler.getItemBuffer().getSlots();
		}

		@Nonnull
		@Override
		public ItemStack getStackInSlot(int slot) {
			return this.handler.getItemBuffer().getStackInSlot(slot);
		}

		@Nonnull
		@Override
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
			if(this.allowInputs() && this.handler.blockEntity.allowInput(stack))
			{
				ItemStack result = this.handler.getItemBuffer().insertItem(slot, stack, simulate);
				if(!simulate)
					this.handler.blockEntity.setItemBufferDirty();
				return result;
			}
			return stack.copy();
		}

		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if(this.allowOutputs())
			{
				ItemStack stackInSlot = this.getStackInSlot(slot);
				if(this.handler.blockEntity.allowOutput(stackInSlot) && !stackInSlot.isEmpty())
				{
					ItemStack result = this.handler.getItemBuffer().extractItem(slot, amount, simulate);
					if(!simulate)
						this.handler.blockEntity.setItemBufferDirty();
					return result;
				}
			}
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot) {
			return this.handler.blockEntity.getStorageStackLimit();
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return this.allowInputs() && this.handler.blockEntity.allowInput(stack);
		}
		
	}
	
	

}
