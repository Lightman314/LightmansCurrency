package io.github.lightman314.lightmanscurrency.api.ticket;

import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.util.ItemHandlerUtil;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.UnaryOperator;

public class TicketUtil {

    public static TicketQueryResult hasTicket(List<IItemHandler> handlers, long ticketID)
    {
        TicketQueryResult r = TicketQueryResult.FAIL;
        for(IItemHandler handler : handlers)
        {
            TicketQueryResult result = hasTicket(handler,ticketID);
            //If any are unlimited, then unlimited it is
            if(result.isUnlimited())
                return result;
            //Otherwise store the best result
            r = r.isPresent() ? r : result;
        }
        return r;
    }
    public static TicketQueryResult hasTicket(IItemHandler handler,long ticketID)
    {
        TicketQueryResult result = TicketQueryResult.FAIL;
        for(int i = 0; i < handler.getSlots(); ++i)
        {

        }
        return result;
    }

    public static TicketCollectionResult takeTicket(List<IItemHandler> handlers,long ticketID,@Nullable UnaryOperator<ItemStack> overflowHandler)
    {
        TicketQueryResult query = hasTicket(handlers,ticketID);
        if(query.isUnlimited()) //If we have unlimited durability on a ticket, don't bother taking anything
            return TicketCollectionResult.PASS;
        //Otherwise loop through each handler until we find one that we successfully took one from
        for(IItemHandler handler : handlers)
        {
            TicketCollectionResult result = takeTicket(handler,ticketID,handleDamagedTicketOverflow(handlers,overflowHandler),false);
            if(result.isSuccess())
                return result;
        }
        //If we didn't get any "pass" results, then this was a failure
        return TicketCollectionResult.FAIL;
    }

    private static UnaryOperator<ItemStack> handleDamagedTicketOverflow(List<IItemHandler> handlers,@Nullable UnaryOperator<ItemStack> overflowHandler)
    {
        return s -> {
            s = ItemHandlerUtil.insertItem(handlers,s,false);
            if(!s.isEmpty() && overflowHandler != null)
                s = overflowHandler.apply(s);
            return s;
        };
    }

    public static TicketCollectionResult takeTicket(IItemHandler handler,long ticketID, @Nullable UnaryOperator<ItemStack> overflowHandler) { return takeTicket(handler,ticketID,overflowHandler,true); }

    private static TicketCollectionResult takeTicket(IItemHandler handler, long ticketID, @Nullable UnaryOperator<ItemStack> overflowHandler, boolean unlimitedCheck)
    {
        //Only do the "unlimited check" when asked for, as the overload that takes a list of item handlers already performs this check
        if(unlimitedCheck && hasTicket(handler,ticketID).isUnlimited())
            return TicketCollectionResult.PASS;
        for(int i = 0; i < handler.getSlots(); ++i) {
            ItemStack stack = handler.getStackInSlot(i).copy();
            if(TicketItem.isTicket(stack))
            {
                long id = TicketItem.GetTicketID(stack);
                if(id == ticketID)
                {
                    ItemStack extracted = handler.extractItem(i,1,false);
                    if(!extracted.isEmpty())
                        return TicketCollectionResult.PASS_WITH_STUB;
                }
            }
            else if(TicketItem.isPass(stack))
            {
                long id = TicketItem.GetTicketID(stack);
                if(id == ticketID)
                {
                    ItemStack damagedTicket = TicketItem.damageTicket(stack);
                    boolean spawnStub = damagedTicket.isEmpty() && stack.isEmpty();
                    ItemStack extracted = handler.extractItem(i,1,false);
                    if(!spawnStub)
                    {
                        //If the ticket has durability, confirm that both the "extract" and "insert" will both function as intended.
                        if(!extracted.isEmpty())
                        {
                            //Insert the "damaged" ticket into the slot we extracted it from
                            ItemStack leftovers = handler.insertItem(i,damagedTicket,false);
                            //If it doesn't go back into that slot, attempt to put it into another slot
                            if(!leftovers.isEmpty())
                                leftovers = ItemHandlerHelper.insertItemStacked(handler,damagedTicket,false);
                            if(!leftovers.isEmpty() && overflowHandler != null)
                                leftovers = overflowHandler.apply(leftovers);
                            if(!leftovers.isEmpty())
                            {
                                //Force the undamaged ticket back into the items
                                leftovers = handler.insertItem(i,extracted.copyWithCount(1),false);
                                if(!leftovers.isEmpty() && overflowHandler != null)
                                    overflowHandler.apply(leftovers);
                                return TicketCollectionResult.FAIL;
                            }
                            //If the leftovers from the insertion is empty, return a pass with no ticket stub
                            return TicketCollectionResult.PASS;
                        }
                    }
                    else
                    {
                        //If damaging the pass will destroy it, simply extract it normally
                        if(!extracted.isEmpty())
                            return TicketCollectionResult.PASS_WITH_STUB;
                        //If we can't extract, continue on with the loop
                    }
                }
            }
        }
        return TicketCollectionResult.FAIL;
    }

}