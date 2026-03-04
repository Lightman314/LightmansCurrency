package io.github.lightman314.lightmanscurrency.api.ticket;

import com.google.common.collect.ImmutableList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TicketGroupData {

    private static final List<TicketGroupData> DATA = new ArrayList<>();

    public static List<TicketGroupData> allData() { return ImmutableList.copyOf(DATA); }

    public final Item masterTicket;
    public final Item ticket;
    public final Item ticketStub;
    public final TagKey<Item> material;

    private TicketGroupData(ItemLike masterTicket, ItemLike ticket, ItemLike ticketStub, TagKey<Item> material)
    {
        this.masterTicket = masterTicket.asItem();
        this.ticket = ticket.asItem();
        this.ticketStub = ticketStub.asItem();
        this.material = material;
    }

    public static void create(ItemLike masterTicket, ItemLike ticket, ItemLike ticketStub, TagKey<Item> material) { DATA.add(new TicketGroupData(masterTicket,ticket,ticketStub,material)); }

    @Nullable
    public static TicketGroupData getForMaster(ItemStack masterTicket)
    {
        for(TicketGroupData data : DATA)
        {
            if(data.masterTicket == masterTicket.getItem())
                return data;
        }
        return null;
    }

    @Nullable
    public static TicketGroupData getForTicket(ItemStack ticket)
    {
        for(TicketGroupData data : DATA)
        {
            if(data.ticket == ticket.getItem())
                return data;
        }
        return null;
    }

    @Nullable
    public static TicketGroupData getForMaterial(ItemStack material)
    {
        for(TicketGroupData data : DATA)
        {
            if(material.is(data.material))
                return data;
        }
        return null;
    }

}
