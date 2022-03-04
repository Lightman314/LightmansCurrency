package io.github.lightman314.lightmanscurrency.blockentity.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blockentity.UniversalItemTraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.trader.interfacing.ItemTradeInteraction;
import io.github.lightman314.lightmanscurrency.trader.interfacing.handlers.ConfigurableSidedHandler;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ItemInterfaceHandler extends ConfigurableSidedHandler<ItemTradeInteraction,IItemHandler> {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "item_interface");
	
	protected final UniversalItemTraderInterfaceBlockEntity blockEntity;
	
	private final Supplier<Container> itemBufferSource;
	protected final Container getItemBuffer() { return this.itemBufferSource.get(); }
	
	private final Map<Direction,Handler> handlers = new HashMap<Direction, Handler>();
	
	public ItemInterfaceHandler(UniversalItemTraderInterfaceBlockEntity blockEntity, Supplier<Container> itemBufferSource) {
		this.blockEntity = blockEntity;
		this.itemBufferSource = itemBufferSource;
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
	
	protected boolean allowInput(int slot, ItemStack item) {
		List<ItemTradeInteraction> interactionList = this.getParent().getInteractions();
		if(interactionList == null)
			return true;
		for(int i = 0; i < interactionList.size(); ++i)
		{
			ItemTradeInteraction interaction = interactionList.get(i);
			//Only filter if the interaction is active & valid
			if(interaction.isActive() && interaction.isValid() && interaction.allowItemInput(slot, item))
				return true;
		}
		return false;
	}
	
	protected boolean allowOutput(int slot, ItemStack item) {
		List<ItemTradeInteraction> interactionList = this.getParent().getInteractions();
		if(interactionList == null)
			return true;
		for(int i = 0; i < interactionList.size(); ++i)
		{
			ItemTradeInteraction interaction = interactionList.get(i);
			//Only filter if the interaction is active & valid
			if(interaction.isActive() && interaction.isValid() && interaction.allowItemOutput(slot, item))
				return true;
		}
		return false;
	}
	
	private static class Handler implements IItemHandler{

		final ItemInterfaceHandler handler;
		final Direction side;
		
		Handler(ItemInterfaceHandler handler, Direction side) { this.handler = handler; this.side = side; }
		
		@Override
		public int getSlots() {
			return this.handler.getItemBuffer().getContainerSize();
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return this.handler.getItemBuffer().getItem(slot);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if(!this.handler.inputSides.get(this.side) || !this.handler.allowInput(slot, stack))
				return stack.copy();
			if(simulate)
			{
				for(int c = stack.getCount(); c > 0; --c)
				{
					ItemStack smallerStack = stack.copy();
					smallerStack.setCount(c);
					if(InventoryUtil.CanPutItemStack(this.handler.getItemBuffer(), smallerStack))
					{
						if(c == stack.getCount())
							return ItemStack.EMPTY;
						ItemStack remainderStack = stack.copy();
						remainderStack.shrink(c);
						return remainderStack;
					}
				}
				return stack.copy();
			}
			else
				return InventoryUtil.TryPutItemStack(this.handler.getItemBuffer(), stack.copy());
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if(!this.handler.outputSides.get(side))
				return ItemStack.EMPTY;
			
			ItemStack itemInSlot = this.getStackInSlot(slot);
			if(!this.handler.allowOutput(slot, itemInSlot))
				return ItemStack.EMPTY;
			int shrinkAmount = Math.min(amount, itemInSlot.getCount());
			if(shrinkAmount <= 0)
				return ItemStack.EMPTY;
			ItemStack resultStack = itemInSlot.copy();
			resultStack.setCount(shrinkAmount);
			if(!simulate)
			{
				itemInSlot.shrink(shrinkAmount);
				if(itemInSlot.isEmpty())
					this.handler.getItemBuffer().setItem(slot, ItemStack.EMPTY);
			}
			return resultStack;
		}

		@Override
		public int getSlotLimit(int slot) {
			return this.handler.getItemBuffer().getMaxStackSize();
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return true;
		}
		
	}
	
	

}
