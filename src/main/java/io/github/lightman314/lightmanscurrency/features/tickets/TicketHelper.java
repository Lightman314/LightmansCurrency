package io.github.lightman314.lightmanscurrency.features.tickets;

import io.github.lightman314.lightmanscurrency.api.LCDataMapTypes;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.features.tickets.data.TicketID;
import io.github.lightman314.lightmanscurrency.features.tickets.data.TicketUses;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;

import io.github.lightman314.lightmanscurrency.features.tickets.recipe.TicketStationRecipeInput;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.Nullable;

public final class TicketHelper {

    private TicketHelper() {}

    public static boolean isMasterTicket(ItemResource masterTicket) { return isMasterTicket(masterTicket.toStack()); }
    public static boolean isMasterTicket(ItemStack masterTicket) {
        //Items are considered a valid ticket if they:
        //1- Have a ticket id data component
        //2- Have master ticket set data
        return masterTicket.has(LCDataComponents.TICKET_ID) && getConsumableTicketFromMaster(masterTicket) != null;
    }

    public static boolean isConsumableTicket(ItemResource ticket) { return isConsumableTicket(ticket.toStack()); }
    public static boolean isConsumableTicket(ItemStack ticket) {
        //Items are considered a valid ticket if they:
        //1- They have a ticket id data component
        //2- Do NOT have master ticket set data
        //3- Have a ticket stub
        return ticket.has(LCDataComponents.TICKET_ID) && getConsumableTicketFromMaster(ticket) == null && getTicketStub(ticket) != null;
    }
    public static boolean isConsumableTicket(ItemResource ticket, long ticketID) { return isConsumableTicket(ticket.toStack(),ticketID); }
    public static boolean isConsumableTicket(ItemStack ticket, long ticketID) {
        return isConsumableTicket(ticket) && ticket.get(LCDataComponents.TICKET_ID).ticketID() == ticketID;
    }

    public static String getCodeKey(ISidedContext context) {
        TicketDataCache data = TicketDataCache.TYPE.get(context);
        if(data != null)
            return data.getCodeKey();
        //Use the unrandomized string as a fallback
        return TicketStationRecipeInput.VALID_CODE_CHARS;
    }

    public static boolean isCoupon(ItemResource coupon) { return isCoupon(coupon.toStack()); }
    public static boolean isCoupon(ItemStack coupon) {
        //Items are considered a coupon if they:
        //1- Have a coupon code component
        //2- Have a ticket stub
        return coupon.has(LCDataComponents.COUPON_CODE) && getTicketStub(coupon) != null;
    }

    public static boolean isCoupon(ItemResource coupon,String code,ISidedContext context) { return isCoupon(coupon,code,getCodeKey(context)); }
    public static boolean isCoupon(ItemStack coupon,String code,ISidedContext context) { return isCoupon(coupon,code,getCodeKey(context)); }
    public static boolean isCoupon(ItemResource coupon,String code,String key) { return isCoupon(coupon.toStack(),code,key); }
    public static boolean isCoupon(ItemStack coupon,String code,String key) {
        if(coupon.has(LCDataComponents.COUPON_CODE) && getTicketStub(coupon) != null)
            return coupon.get(LCDataComponents.COUPON_CODE).is(code,key);
        return false;
    }

    @Nullable
    @SuppressWarnings("deprecation")
    public static Item getConsumableTicketFromMaster(Item masterTicket) { return masterTicket.builtInRegistryHolder().getData(LCDataMapTypes.MASTER_TICKET_SET); }
    @Nullable
    public static Item getConsumableTicketFromMaster(TypedInstance<Item> masterTicket) { return masterTicket.getData(LCDataMapTypes.MASTER_TICKET_SET); }

    @Nullable
    @SuppressWarnings("deprecation")
    public static Item getTicketStub(Item destroyedTicket) { return destroyedTicket.builtInRegistryHolder().getData(LCDataMapTypes.TICKET_STUB); }
    @Nullable
    public static Item getTicketStub(TypedInstance<Item> destroyedTicket) { return destroyedTicket.getData(LCDataMapTypes.TICKET_STUB); }

    public static ItemStack createTicket(ItemLike ticket,long ticketID,int color) { return createTicket(ticket,new TicketID(ticketID),color); }
    public static ItemStack createTicket(ItemLike ticket,long ticketID,int color,TicketUses uses) { return createTicket(ticket,new TicketID(ticketID),color,uses); }
    public static ItemStack createTicket(ItemLike ticket,TicketID id,int color) { return createTicket(ticket,id,color,TicketUses.DEFAULT); }
    public static ItemStack createTicket(ItemLike ticket, TicketID id, int color,TicketUses uses) {
        ItemStack stack = new ItemStack(ticket);
        stack.set(LCDataComponents.TICKET_ID,id);
        stack.set(DataComponents.DYED_COLOR,new DyedItemColor(color));
        return stack;
    }

}