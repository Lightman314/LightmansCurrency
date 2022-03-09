package io.github.lightman314.lightmanscurrency.blockentity.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blockentity.UniversalItemTraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.menus.containers.LockableContainer;
import io.github.lightman314.lightmanscurrency.menus.containers.LockableContainer.IExternalInputOutputRules;
import io.github.lightman314.lightmanscurrency.menus.containers.LockableContainer.LockData;
import io.github.lightman314.lightmanscurrency.trader.interfacing.handlers.ConfigurableSidedHandler;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ItemInterfaceHandler extends ConfigurableSidedHandler<IItemHandler> {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "item_interface");
	
	protected final UniversalItemTraderInterfaceBlockEntity blockEntity;
	
	private final Supplier<LockableContainer> itemBufferSource;
	protected final LockableContainer getItemBuffer() { return this.itemBufferSource.get(); }
	
	private final Map<Direction,Handler> handlers = new HashMap<Direction, Handler>();
	
	public ItemInterfaceHandler(UniversalItemTraderInterfaceBlockEntity blockEntity, Supplier<LockableContainer> itemBufferSource) {
		this.blockEntity = blockEntity;
		this.itemBufferSource = itemBufferSource;
	}
	
	@Override
	public ResourceLocation getType() { return TYPE; }
	
	@Override
	public String getTag() { return "ItemData"; }
	
	@Override
	public IItemHandler getHandler(Direction side) {
		if(this.allowInput(side) || this.outputSides.get(side))
		{
			if(!this.handlers.containsKey(side))
				this.handlers.put(side, new Handler(this, side));
			return this.handlers.get(side);
		}
		return null;
	}
	
	public boolean allowInput(Direction side) {
		return this.inputSides.get(side) && this.blockEntity.allowAnyInput();
	}
	
	public boolean allowOutput(Direction side) {
		return this.outputSides.get(side) && this.blockEntity.allowAnyOutput();
	}
	
	private static class Handler implements IItemHandler, IExternalInputOutputRules{

		final ItemInterfaceHandler handler;
		final Direction side;
		
		Handler(ItemInterfaceHandler handler, Direction side) { this.handler = handler; this.side = side; }
		
		@Override
		public int getSlots() {
			return this.handler.getItemBuffer().getSlots();
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return this.handler.getItemBuffer().getStackInSlot(slot);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			this.handler.getItemBuffer().setAdditionalRules(this);
			return this.handler.getItemBuffer().insertItem(slot, stack, simulate);
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			this.handler.getItemBuffer().setAdditionalRules(this);
			return this.handler.getItemBuffer().extractItem(slot, amount, simulate);
		}

		@Override
		public int getSlotLimit(int slot) {
			return this.handler.getItemBuffer().getSlotLimit(slot);
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			this.handler.getItemBuffer().setAdditionalRules(this);
			return this.handler.getItemBuffer().isItemValid(slot, stack);
		}

		@Override
		public boolean allowInput(int slot, LockData lock, ItemStack inputStack) {
			return this.handler.allowInput(this.side) && this.handler.blockEntity.allowItemInput(inputStack);
		}

		@Override
		public boolean allowOutput(int slot, LockData lock, ItemStack outputStack) {
			return this.handler.allowOutput(this.side) && this.handler.blockEntity.allowItemOutput(outputStack);
		}
		
	}
	
	

}
