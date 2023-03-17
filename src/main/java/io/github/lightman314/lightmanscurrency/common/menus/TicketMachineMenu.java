package io.github.lightman314.lightmanscurrency.common.menus;

import io.github.lightman314.lightmanscurrency.common.menus.slots.ticket.*;
import io.github.lightman314.lightmanscurrency.common.tickets.TicketSaveData;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.menus.slots.OutputSlot;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.TicketMachineBlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class TicketMachineMenu extends LazyMessageMenu{
	
	private final Container output = new SimpleContainer(1);
	
	private final TicketMachineBlockEntity blockEntity;
	
	public TicketMachineMenu(int windowId, Inventory inventory, TicketMachineBlockEntity blockEntity)
	{
		super(ModMenus.TICKET_MACHINE.get(), windowId, inventory);
		this.blockEntity = blockEntity;
		
		//Slots
		this.addSlot(new TicketModifierSlot(this.blockEntity.getStorage(), 0, 20, 21));
		this.addSlot(new TicketMaterialSlot(this.blockEntity.getStorage(), 1, 56, 21));
		
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
	public boolean stillValid(@NotNull Player playerIn)
	{
		return true;
	}
	
	@Override
	public void removed(@NotNull Player playerIn)
	{
		super.removed(playerIn);
		this.clearContainer(playerIn,  this.output);
	}
	
	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index)
	{
		
		ItemStack clickedStack = ItemStack.EMPTY;
		
		Slot slot = this.slots.get(index);
		
		if(slot != null && slot.hasItem())
		{
			ItemStack slotStack = slot.getItem();
			clickedStack = slotStack.copy();
			int totalSize = this.blockEntity.getStorage().getContainerSize() + this.output.getContainerSize();
			if(index < totalSize)
			{
				if(!this.moveItemStackTo(slotStack, totalSize, this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(!this.moveItemStackTo(slotStack, 0, this.blockEntity.getStorage().getContainerSize(), false))
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
		return !this.blockEntity.getStorage().getItem(1).isEmpty();
	}
	
	public boolean roomForOutput()
	{
		ItemStack outputStack = this.output.getItem(0);
		if(outputStack.isEmpty())
			return true;
		if(hasMasterTicket() && outputStack.getItem() == ModItems.TICKET.get())
		{
			//Confirm that the output item has the same ticket id as the master ticket
			long ticketID = getTicketID();
			long outputTicketID = TicketItem.GetTicketID(outputStack);
			return ticketID == outputTicketID;
		}
		else //Not empty, and no master ticket in the slot means that no new master ticket can be placed in the output slot
		{
			return false;
		}
	}
	
	public boolean hasMasterTicket()
	{
		ItemStack masterTicket = this.blockEntity.getStorage().getItem(0);
		return TicketItem.isMasterTicket(masterTicket);
	}
	
	public void craftTickets(boolean fullStack)
	{
		if(!validInputs())
		{
			LightmansCurrency.LogDebug("Inputs for the Ticket Machine are not valid. Cannot craft tickets.");
			return;
		}
		else if(!roomForOutput())
		{
			LightmansCurrency.LogDebug("No room for Ticket Machine outputs. Cannot craft tickets.");
			return;
		}
		if(hasMasterTicket())
		{
			int count = 1;
			if(fullStack)
				count = this.blockEntity.getStorage().getItem(1).getCount();
			
			//Create a normal ticket
			ItemStack outputStack = this.output.getItem(0);
			if(outputStack.isEmpty())
			{
				//Create a new ticket stack
				ItemStack newTicket = TicketItem.CreateTicket(this.getTicketID(), this.getTicketColor(), count);
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
			this.blockEntity.getStorage().removeItem(1, count);
		}
		else
		{
			//Create a master ticket
			Color dye = getDyeColor();
			ItemStack newTicket = TicketItem.CreateMasterTicket(TicketSaveData.createNextID());
			if(dye != null)
			{
				TicketItem.SetTicketColor(newTicket, dye);
				//Consume the dye
				this.blockEntity.getStorage().removeItem(0, 1);
			}

			this.output.setItem(0, newTicket);
			
			//Remove the crafting materials
			this.blockEntity.getStorage().removeItem(1, 1);
		}
		
		
	}
	
	public long getTicketID()
	{
		ItemStack masterTicket = this.blockEntity.getStorage().getItem(0);
		if(TicketItem.isMasterTicket(masterTicket))
			return TicketItem.GetTicketID(masterTicket);
		return Long.MIN_VALUE;
	}

	public int getTicketColor()
	{
		ItemStack stack = this.blockEntity.getStorage().getItem(0);
		if(TicketItem.isMasterTicket(stack))
			return TicketItem.GetTicketColor(stack);
		return 0xFFFFFF;
	}

	@Nullable
	public Color getDyeColor()
	{
		ItemStack stack = this.blockEntity.getStorage().getItem(0);
		return TicketModifierSlot.getColorFromDye(stack);
	}

	public void SendCraftTicketsMessage(boolean fullStack)
	{
		this.SendMessageToServer(LazyPacketData.builder().setBoolean("CraftTickets", fullStack));
	}

	@Override
	public void HandleMessage(LazyPacketData message) {
		if(message.contains("CraftTickets"))
			this.craftTickets(message.getBoolean("CraftTickets"));
	}

}
