package io.github.lightman314.lightmanscurrency.common.items;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.items.data.TicketData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DyedItemColor;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TicketItem extends Item {


	public TicketItem(Properties properties) { super(properties); }

    @Override
    @SuppressWarnings("deprecation")
    public void verifyComponentsAfterLoad(ItemStack stack) {
        if(stack.has(ModDataComponents.TICKET_DATA))
        {
            TicketData oldData = stack.get(ModDataComponents.TICKET_DATA);
            stack.remove(ModDataComponents.TICKET_DATA);
            stack.set(ModDataComponents.TICKET_ID,oldData.id());
            stack.set(DataComponents.DYED_COLOR,new DyedItemColor(oldData.color(),false));
        }
    }

    @Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag)
	{
		if(isPass(stack))
        {
            tooltip.add(LCText.TOOLTIP_PASS.get());
            if(stack.has(ModDataComponents.TICKET_USES))
                tooltip.add(LCText.TOOLTIP_TICKET_USES.get(stack.get(ModDataComponents.TICKET_USES)).withStyle(ChatFormatting.GRAY));
        }
		long ticketID = GetTicketID(stack);
		if(ticketID >= -2)
			tooltip.add(LCText.TOOLTIP_TICKET_ID.get(ticketID));
		super.appendHoverText(stack,context,tooltip,flag);
	}

	public static boolean isTicket(ItemStack ticket)
	{
		if(ticket.isEmpty() || !ticket.has(ModDataComponents.TICKET_ID))
			return false;
		return ticket.getItem() instanceof TicketItem && InventoryUtil.ItemHasTag(ticket, LCTags.Items.TICKETS_TICKET);
	}

	public static boolean isPass(ItemStack ticket)
	{
		if(ticket.isEmpty() || !ticket.has(ModDataComponents.TICKET_ID))
			return false;
		return ticket.getItem() instanceof TicketItem && InventoryUtil.ItemHasTag(ticket, LCTags.Items.TICKETS_PASS);
	}

    public static boolean isInfinitePass(ItemStack ticket)
    {
        return isPass(ticket) && !ticket.has(ModDataComponents.TICKET_USES);
    }

	public static boolean isTicketOrPass(ItemStack ticket)  { return isTicket(ticket) || isPass(ticket); }

	public static boolean isMasterTicket(ItemStack ticket)
	{
		if(ticket.isEmpty() || !ticket.has(ModDataComponents.TICKET_ID))
			return false;
		return ticket.getItem() instanceof TicketItem && InventoryUtil.ItemHasTag(ticket, LCTags.Items.TICKETS_MASTER);
	}

	public static long GetTicketID(ItemStack ticket)
	{
		//Get the ticket item
		if(ticket.isEmpty() || !(ticket.getItem() instanceof TicketItem) || !ticket.has(ModDataComponents.TICKET_ID))
			return Long.MIN_VALUE;
		return ticket.get(ModDataComponents.TICKET_ID);
	}

	public static int GetTicketColor(ItemStack ticket)
	{
		if(ticket.isEmpty() || !(ticket.getItem() instanceof TicketItem) || !ticket.has(ModDataComponents.TICKET_ID))
			return 0xFFFFFF;
		return ticket.getOrDefault(DataComponents.DYED_COLOR,new DyedItemColor(0xFFFFFF,false)).rgb();
	}

	public static int GetDefaultTicketColor(long ticketID) {
		if (ticketID == -1)
			return Color.YELLOW.hexColor;
		if(ticketID == -2)
			return Color.BLUE.hexColor;
		return Color.getFromIndex(ticketID).hexColor;
	}

	public static ItemStack CraftTicket(ItemStack master, Item item)
	{
		if(isMasterTicket(master))
			return CreateTicket(item, GetTicketID(master), GetTicketColor(master));
		return ItemStack.EMPTY;
	}

	public static ItemStack CreateTicket(Item item, long ticketID) { return CreateTicket(item, ticketID, TicketItem.GetDefaultTicketColor(ticketID)); }
	public static ItemStack CreateTicket(Item item, long ticketID, int color) { return CreateTicket(item, ticketID, color, 1); }
	public static ItemStack CreateTicket(Item item, long ticketID, int color, int count)
	{
		ItemStack ticket = new ItemStack(item, count);
        ticket.set(ModDataComponents.TICKET_ID,ticketID);
        ticket.set(DataComponents.DYED_COLOR,new DyedItemColor(color,false));
		return ticket;
	}

    public static int getUseCount(ItemStack ticket) { return ticket.getOrDefault(ModDataComponents.TICKET_USES,0); }
    public static void setUseCount(ItemStack ticket, int useCount) { ticket.set(ModDataComponents.TICKET_USES,useCount); }

    /**
     * Decrements the {@link ModDataComponents#TICKET_USES} value by one
     * @param ticket The Ticket or Coupon that should be damaged
     * @return Any additional items that were split from the ticket and should be returned back to the player
     */
    public static ItemStack damageTicket(ItemStack ticket)
    {
        ItemStack result = ItemStack.EMPTY;
        int uses = getUseCount(ticket);
        if(uses > 0)
        {
            if(ticket.getCount() > 1)
            {
                ticket = ticket.split(1);
                result = ticket;
            }
            if(uses == 1)
                ticket.shrink(1);
            else
                setUseCount(ticket,uses - 1);
        }
        return result;
    }

	public static ItemStack CreateExampleTicket(Item item, Color color)
	{
		ItemStack ticket = new ItemStack(item);
        ticket.set(ModDataComponents.TICKET_ID,Long.MIN_VALUE);
        ticket.set(DataComponents.DYED_COLOR,new DyedItemColor(color.hexColor,false));
		return ticket;
	}

	public static void SetTicketColor(ItemStack ticket, Color color) { SetTicketColor(ticket, color.hexColor); }

	public static void SetTicketColor(ItemStack ticket, int color) {
        ticket.set(DataComponents.DYED_COLOR,new DyedItemColor(color,false));
	}
	
}
