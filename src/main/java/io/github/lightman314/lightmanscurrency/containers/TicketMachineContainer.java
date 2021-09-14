package io.github.lightman314.lightmanscurrency.containers;

import io.github.lightman314.lightmanscurrency.containers.slots.TicketMaterialSlot;

import java.util.UUID;

import io.github.lightman314.lightmanscurrency.containers.slots.OutputSlot;
import io.github.lightman314.lightmanscurrency.containers.slots.TicketMasterSlot;
import io.github.lightman314.lightmanscurrency.core.ModContainers;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.items.TicketItem;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IWorldPosCallable;

public class TicketMachineContainer extends Container{

	private final int SLOTCOUNT = 3;
	
	private final IInventory objectInputs = new Inventory(SLOTCOUNT);
	private final IWorldPosCallable callable;
	
	public TicketMachineContainer(int windowId, PlayerInventory inventory)
	{
		this(windowId, inventory, IWorldPosCallable.DUMMY);
	}
	
	public TicketMachineContainer(int windowId, PlayerInventory inventory, final IWorldPosCallable callable)
	{
		super(ModContainers.TICKET_MACHINE, windowId);
		this.callable = callable;
		
		//Slots
		this.addSlot(new TicketMasterSlot(this.objectInputs, 0, 20, 21));
		this.addSlot(new TicketMaterialSlot(this.objectInputs, 1, 56, 21));
		
		this.addSlot(new OutputSlot(this.objectInputs, 2, 116, 21));
		
		//Player inventory
		for(int y = 0; y < 3; y++)
		{
			for(int x = 0; x < 9; x++)
			{
				this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 56 + y * 18));
			}
		}
		//Player hotbar
		for(int x = 0; x < 9; x++)
		{
			this.addSlot(new Slot(inventory, x, 8 + x * 18, 114));
		}
	}
	
	@Override
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return this.callable.applyOrElse((world,pos) -> playerIn.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0, true);
	}
	
	@Override
	public void onContainerClosed(PlayerEntity playerIn)
	{
		super.onContainerClosed(playerIn);
		this.callable.consume((world,pos) ->
		{
			this.clearContainer(playerIn,  world,  this.objectInputs);
		});
	}
	
	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerEntity, int index)
	{
		
		ItemStack clickedStack = ItemStack.EMPTY;
		
		Slot slot = this.inventorySlots.get(index);
		
		if(slot != null && slot.getHasStack())
		{
			ItemStack slotStack = slot.getStack();
			clickedStack = slotStack.copy();
			if(index < this.objectInputs.getSizeInventory())
			{
				if(!this.mergeItemStack(slotStack, this.objectInputs.getSizeInventory(), this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(!this.mergeItemStack(slotStack, 0, this.objectInputs.getSizeInventory() - 1, true))
			{
				return ItemStack.EMPTY;
			}
			
			if(slotStack.isEmpty())
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else
			{
				slot.onSlotChanged();
			}
		}
		
		return clickedStack;
		
	}

	
	public boolean validInputs()
	{
		return !this.objectInputs.getStackInSlot(1).isEmpty();
	}
	
	public boolean validOutputs()
	{
		ItemStack outputStack = this.objectInputs.getStackInSlot(2);
		if(outputStack.isEmpty())
			return true;
		if(hasMasterTicket() && outputStack.getItem() == ModItems.TICKET)
		{
			//Confirm that the output item has the same ticket id as the master ticket
			UUID ticketID = getTicketID();
			UUID outputTicketID = TicketItem.GetTicketID(outputStack);
			if(outputTicketID != null && ticketID.equals(outputTicketID))
				return true;
			return false;
		}
		else //Not empty, and no master ticket in the slot means that no new master ticket can be placed in the output slot
		{
			return false;
		}
	}
	
	public boolean hasMasterTicket()
	{
		ItemStack masterTicket = this.objectInputs.getStackInSlot(0);
		if(masterTicket.isEmpty() || !TicketItem.isMasterTicket(masterTicket))
			return false;
		return true;
	}
	
	public void craftTickets(boolean fullStack)
	{
		if(!validInputs())
		{
			LightmansCurrency.LogWarning("Inputs for the Ticket Machine are not valid. Cannot craft tickets.");
			return;
		}
		else if(!validOutputs())
		{
			LightmansCurrency.LogWarning("No room for Ticket Machine outputs. Cannot craft tickets.");
			return;
		}
		if(hasMasterTicket())
		{
			int count = 1;
			if(fullStack)
				count = objectInputs.getStackInSlot(1).getCount();
			
			//Create a normal ticket
			ItemStack outputStack = this.objectInputs.getStackInSlot(2);
			if(outputStack.isEmpty())
			{
				//Create a new ticket stack
				ItemStack newTicket = new ItemStack(ModItems.TICKET, count);
				CompoundNBT compound = new CompoundNBT();
				compound.putUniqueId("TicketID", this.getTicketID());
				newTicket.setTag(compound);
				this.objectInputs.setInventorySlotContents(2, newTicket);
			}
			else
			{
				//Limit the added count by stack size
				count = outputStack.getMaxStackSize() - outputStack.getCount();
				//Increase the stack size
				outputStack.setCount(outputStack.getCount() + count);
			}
			
			//Remove the crafting materials
			this.objectInputs.decrStackSize(1, count);
		}
		else
		{
			//Create a master ticket
			ItemStack newTicket = new ItemStack(ModItems.TICKET, 1);
			CompoundNBT compound = new CompoundNBT();
			compound.putBoolean("Master", true);
			compound.putUniqueId("TicketID", UUID.randomUUID());
			newTicket.setTag(compound);
			
			this.objectInputs.setInventorySlotContents(2, newTicket);
			
			//Remove the crafting materials
			this.objectInputs.decrStackSize(1, 1);
		}
		
		
	}
	
	public UUID getTicketID()
	{
		ItemStack masterTicket = this.objectInputs.getStackInSlot(0);
		if(TicketItem.isMasterTicket(masterTicket))
		{
			return TicketItem.GetTicketID(masterTicket);
		}
		return null;
	}
	
}
