package io.github.lightman314.lightmanscurrency.menus.traderinterface.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.blockentity.ItemTraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.item.ItemStorageClientTab;
import io.github.lightman314.lightmanscurrency.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.menus.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.menus.slots.UpgradeInputSlot;
import io.github.lightman314.lightmanscurrency.menus.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.menus.traderinterface.TraderInterfaceTab;
import io.github.lightman314.lightmanscurrency.trader.common.TraderItemStorage;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemStorageTab extends TraderInterfaceTab{

	public ItemStorageTab(TraderInterfaceMenu menu) { super(menu); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public TraderInterfaceClientTab<?> createClientTab(TraderInterfaceScreen screen) { return new ItemStorageClientTab(screen, this); }

	@Override
	public boolean canOpen(Player player) { return true; }
	
	//Eventually will add upgrade slots
	List<SimpleSlot> slots = new ArrayList<>();
	public List<? extends Slot> getSlots() { return this.slots; }
	
	@Override
	public void onTabOpen() { SimpleSlot.SetActive(this.slots); }

	@Override
	public void onTabClose() { SimpleSlot.SetInactive(this.slots); }
	
	@Override
	public void addStorageMenuSlots(Function<Slot,Slot> addSlot) {
		for(int i = 0; i < this.menu.getBE().getUpgradeInventory().getContainerSize(); ++i)
		{
			SimpleSlot upgradeSlot = new UpgradeInputSlot(this.menu.getBE().getUpgradeInventory(), i, 176, 18 + 18 * i, this.menu.getBE(), this::onUpgradeModified);
			upgradeSlot.active = false;
			addSlot.apply(upgradeSlot);
			this.slots.add(upgradeSlot);
		}
	}
	
	private void onUpgradeModified() {
		this.menu.getBE().setUpgradeSlotsDirty();
	}
	
	@Override
	public boolean quickMoveStack(ItemStack stack) {
		if(this.menu.getBE() instanceof ItemTraderInterfaceBlockEntity) {
			ItemTraderInterfaceBlockEntity be = (ItemTraderInterfaceBlockEntity)this.menu.getBE();
			TraderItemStorage storage = be.getItemBuffer();
			if(storage.getFittableAmount(stack) > 0)
			{
				storage.tryAddItem(stack);
				be.setItemBufferDirty();
				return true;
			}
		}
		return super.quickMoveStack(stack);
	}
	
	public void clickedOnSlot(int storageSlot, boolean isShiftHeld, boolean leftClick) {
		if(this.menu.getBE().isOwner(this.menu.player) && this.menu.getBE() instanceof ItemTraderInterfaceBlockEntity)
		{
			ItemTraderInterfaceBlockEntity be = (ItemTraderInterfaceBlockEntity)this.menu.getBE();
			TraderItemStorage storage = be.getItemBuffer();
			ItemStack heldItem = this.menu.getCarried();
			if(heldItem.isEmpty())
			{
				//Move item out of storage
				List<ItemStack> storageContents = storage.getContents();
				if(storageSlot >= 0 && storageSlot < storageContents.size())
				{
					ItemStack stackToRemove = storageContents.get(storageSlot).copy();
					ItemStack removeStack = stackToRemove.copy();
					
					//Assume we're moving a whole stack for now
					int tempAmount = Math.min(stackToRemove.getMaxStackSize(), stackToRemove.getCount());
					stackToRemove.setCount(tempAmount);
					int removedAmount = 0;
					
					//Right-click, attempt to cut the stack in half
					if(!leftClick)
					{
						if(tempAmount > 1)
							tempAmount = tempAmount / 2;
						stackToRemove.setCount(tempAmount);
					}
					
					if(isShiftHeld)
					{
						//Put the item in the players inventory. Will not throw overflow on the ground, so it will safely stop if the players inventory is full
						this.menu.player.getInventory().add(stackToRemove);
						//Determine the amount actually added to the players inventory
						removedAmount = tempAmount - stackToRemove.getCount();
					}
					else
					{
						//Put the item into the players hand
						this.menu.setCarried(stackToRemove);
						removedAmount = tempAmount;
					}
					//Remove the correct amount from storage
					if(removedAmount > 0)
					{
						removeStack.setCount(removedAmount);
						storage.removeItem(removeStack);
						//Mark the storage dirty
						be.setItemBufferDirty();
					}
				}
			}
			else
			{
				//Move from hand to storage
				if(leftClick)
				{
					storage.tryAddItem(heldItem);
					//Mark the storage dirty
					be.setItemBufferDirty();
				}
				else
				{
					//Right click, only attempt to add 1 from the hand
					ItemStack addItem = heldItem.copy();
					addItem.setCount(1);
					if(storage.addItem(addItem))
					{
						heldItem.shrink(1);
						if(heldItem.isEmpty())
							this.menu.setCarried(ItemStack.EMPTY);
					}
					//Mark the storage dirty
					be.setItemBufferDirty();
				}
			}
			if(this.menu.isClient())
			{
				CompoundTag message = new CompoundTag();
				message.putInt("ClickedSlot", storageSlot);
				message.putBoolean("HeldShift", isShiftHeld);
				message.putBoolean("LeftClick", leftClick);
				this.menu.sendMessage(message);
			}
		}
	}
	
	public void toggleInputSlot(Direction side) {
		if(this.menu.getBE().isOwner(this.menu.player) && this.menu.getBE() instanceof ItemTraderInterfaceBlockEntity) {
			ItemTraderInterfaceBlockEntity be = (ItemTraderInterfaceBlockEntity)this.menu.getBE();
			be.getItemHandler().toggleInputSide(side);
			be.setHandlerDirty(be.getItemHandler());
			/*if(this.menu.isClient())
			{
				CompoundTag message = new CompoundTag();
				message.putInt("ToggleInput", side.get3DDataValue());
				message.putBoolean("NewValue", be.getItemHandler().getInputSides().get(side));
				this.menu.sendMessage(message);
			}*/
		}
	}
	
	public void toggleOutputSlot(Direction side) {
		if(this.menu.getBE().isOwner(this.menu.player) && this.menu.getBE() instanceof ItemTraderInterfaceBlockEntity) {
			ItemTraderInterfaceBlockEntity be = (ItemTraderInterfaceBlockEntity)this.menu.getBE();
			be.getItemHandler().toggleOutputSide(side);
			be.setHandlerDirty(be.getItemHandler());
			/*if(this.menu.isClient())
			{
				CompoundTag message = new CompoundTag();
				message.putInt("ToggleOutput", side.get3DDataValue());
				message.putBoolean("NewValue", be.getItemHandler().getOutputSides().get(side));
				this.menu.sendMessage(message);
			}*/
		}
	}
	
	@Override
	public void receiveMessage(CompoundTag message) { 
		if(message.contains("ClickedSlot", Tag.TAG_INT))
		{
			int storageSlot = message.getInt("ClickedSlot");
			boolean isShiftHeld = message.getBoolean("HeldShift");
			boolean leftClick = message.getBoolean("LeftClick");
			this.clickedOnSlot(storageSlot, isShiftHeld, leftClick);
		}
		/*else if(message.contains("ToggleInput"))
		{
			Direction side = Direction.from3DDataValue(message.getInt("ToggleInput"));
			boolean newValue = message.getBoolean("NewValue");
			if(this.menu.getBE() instanceof ItemTraderInterfaceBlockEntity)
			{
				ItemTraderInterfaceBlockEntity be = (ItemTraderInterfaceBlockEntity)this.menu.getBE();
				if(be.getItemHandler().getInputSides().get(side) == newValue)
					return;
				this.toggleInputSlot(side);
			}
		}
		else if(message.contains("ToggleOutput"))
		{
			Direction side = Direction.from3DDataValue(message.getInt("ToggleInput"));
			boolean newValue = message.getBoolean("NewValue");
			if(this.menu.getBE() instanceof ItemTraderInterfaceBlockEntity)
			{
				ItemTraderInterfaceBlockEntity be = (ItemTraderInterfaceBlockEntity)this.menu.getBE();
				if(be.getItemHandler().getOutputSides().get(side) == newValue)
					return;
				this.toggleOutputSlot(side);
			}
		}*/
	}

}
