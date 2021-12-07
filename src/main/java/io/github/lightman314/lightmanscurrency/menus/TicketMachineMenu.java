package io.github.lightman314.lightmanscurrency.menus;

import java.util.UUID;

import io.github.lightman314.lightmanscurrency.core.ModContainers;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.items.TicketItem;
import io.github.lightman314.lightmanscurrency.menus.slots.OutputSlot;
import io.github.lightman314.lightmanscurrency.menus.slots.TicketMasterSlot;
import io.github.lightman314.lightmanscurrency.menus.slots.TicketMaterialSlot;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blockentity.TicketMachineBlockEntity;

public class TicketMachineMenu extends AbstractContainerMenu{
	
	private final Container output = new SimpleContainer(1);
	
	private final TicketMachineBlockEntity tileEntity;
	
	public TicketMachineMenu(int windowId, Inventory inventory, TicketMachineBlockEntity tileEntity)
	{
		super(ModContainers.TICKET_MACHINE, windowId);
		this.tileEntity = tileEntity;
		
		//Slots
		this.addSlot(new TicketMasterSlot(this.tileEntity.getStorage(), 0, 20, 21));
		this.addSlot(new TicketMaterialSlot(this.tileEntity.getStorage(), 1, 56, 21));
		
		this.addSlot(new OutputSlot(this.output, 0, 116, 21));
		
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
		return true;
	}
	
	@Override
	public void removed(Player playerIn)
	{
		super.removed(playerIn);
		this.clearContainer(playerIn,  this.output);
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
			int totalSize = this.tileEntity.getStorage().getContainerSize() + this.output.getContainerSize();
			if(index < totalSize)
			{
				if(!this.moveItemStackTo(slotStack, totalSize, this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(!this.moveItemStackTo(slotStack, 0, this.tileEntity.getStorage().getContainerSize(), false))
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
		return !this.tileEntity.getStorage().getItem(1).isEmpty();
	}
	
	public boolean roomForOutput()
	{
		ItemStack outputStack = this.output.getItem(0);
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
		ItemStack masterTicket = this.tileEntity.getStorage().getItem(0);
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
		else if(!roomForOutput())
		{
			LightmansCurrency.LogWarning("No room for Ticket Machine outputs. Cannot craft tickets.");
			return;
		}
		if(hasMasterTicket())
		{
			int count = 1;
			if(fullStack)
				count = this.tileEntity.getStorage().getItem(1).getCount();
			
			//Create a normal ticket
			ItemStack outputStack = this.output.getItem(0);
			if(outputStack.isEmpty())
			{
				//Create a new ticket stack
				ItemStack newTicket = TicketItem.CreateTicket(this.getTicketID(), count);
				this.output.setItem(0, newTicket);
			}
			else
			{
				//Limit the added count by amount of space left in the output
				count = Math.min(count, outputStack.getMaxStackSize() - outputStack.getCount());
				//Increase the stack size
				outputStack.setCount(outputStack.getCount() + count);
			}
			//Remove the crafting materials
			this.tileEntity.getStorage().removeItem(1, count);
		}
		else
		{
			//Create a master ticket
			ItemStack newTicket = TicketItem.CreateMasterTicket(UUID.randomUUID());
			
			this.output.setItem(0, newTicket);
			
			//Remove the crafting materials
			this.tileEntity.getStorage().removeItem(1, 1);
		}
		
		
	}
	
	public UUID getTicketID()
	{
		ItemStack masterTicket = this.tileEntity.getStorage().getItem(0);
		if(TicketItem.isMasterTicket(masterTicket))
			return TicketItem.GetTicketID(masterTicket);
		return null;
	}
	
}
