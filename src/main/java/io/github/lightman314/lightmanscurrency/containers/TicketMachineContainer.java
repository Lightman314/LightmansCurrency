package io.github.lightman314.lightmanscurrency.containers;

import io.github.lightman314.lightmanscurrency.containers.slots.TicketMaterialSlot;

import java.util.UUID;

import io.github.lightman314.lightmanscurrency.containers.slots.OutputSlot;
import io.github.lightman314.lightmanscurrency.containers.slots.TicketMasterSlot;
import io.github.lightman314.lightmanscurrency.core.ModContainers;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.items.TicketItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;

public class TicketMachineContainer extends AbstractContainerMenu{

	private final int SLOTCOUNT = 3;
	
	private final Container objectInputs = new SimpleContainer(SLOTCOUNT);
	
	public TicketMachineContainer(int windowId, Inventory inventory)
	{
		super(ModContainers.TICKET_MACHINE, windowId);
		
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
	public boolean stillValid(Player playerIn)
	{
		//return this.callable.applyOrElse((world,pos) -> playerIn.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0, true);
		return true;
	}
	
	@Override
	public void removed(Player playerIn)
	{
		super.removed(playerIn);
		this.clearContainer(playerIn, this.objectInputs);
	}
	
	@Override
	public ItemStack quickMoveStack(Player playerEntity, int index)
	{
		
		ItemStack clickedStack = ItemStack.EMPTY;
		
		Slot slot = this.slots.get(index);
		
		if(slot != null && slot.hasItem())
		{
			ItemStack slotStack = slot.getItem();
			clickedStack = slotStack.copy();
			if(index < this.objectInputs.getContainerSize())
			{
				if(!this.moveItemStackTo(slotStack, this.objectInputs.getContainerSize(), this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(!this.moveItemStackTo(slotStack, 0, this.objectInputs.getContainerSize() - 1, true))
			{
				return ItemStack.EMPTY;
			}
			
			if(slotStack.isEmpty())
			{
				slot.set(ItemStack.EMPTY);
			}
			else
			{
				slot.setChanged();
			}
		}
		
		return clickedStack;
		
	}

	
	public boolean validInputs()
	{
		return !this.objectInputs.getItem(1).isEmpty();
	}
	
	public boolean validOutputs()
	{
		ItemStack outputStack = this.objectInputs.getItem(2);
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
		ItemStack masterTicket = this.objectInputs.getItem(0);
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
				count = objectInputs.getItem(1).getCount();
			
			//Create a normal ticket
			ItemStack outputStack = this.objectInputs.getItem(2);
			if(outputStack.isEmpty())
			{
				//Create a new ticket stack
				ItemStack newTicket = new ItemStack(ModItems.TICKET, count);
				CompoundTag compound = new CompoundTag();
				compound.putUUID("TicketID", this.getTicketID());
				newTicket.setTag(compound);
				this.objectInputs.setItem(2, newTicket);
			}
			else
			{
				//Limit the added count by stack size
				count = outputStack.getMaxStackSize() - outputStack.getCount();
				//Increase the stack size
				outputStack.setCount(outputStack.getCount() + count);
			}
			
			//Remove the crafting materials
			this.objectInputs.removeItem(1, count);
		}
		else
		{
			//Create a master ticket
			ItemStack newTicket = new ItemStack(ModItems.TICKET, 1);
			CompoundTag compound = new CompoundTag();
			compound.putBoolean("Master", true);
			compound.putUUID("TicketID", UUID.randomUUID());
			newTicket.setTag(compound);
			
			this.objectInputs.setItem(2, newTicket);
			
			//Remove the crafting materials
			this.objectInputs.removeItem(1, 1);
		}
		
		
	}
	
	public UUID getTicketID()
	{
		ItemStack masterTicket = this.objectInputs.getItem(0);
		if(TicketItem.isMasterTicket(masterTicket))
		{
			return TicketItem.GetTicketID(masterTicket);
		}
		return null;
	}
	
}
