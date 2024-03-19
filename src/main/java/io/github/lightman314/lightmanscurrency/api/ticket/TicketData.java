package io.github.lightman314.lightmanscurrency.api.ticket;

import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TicketData {

    private static final List<TicketData> DATA = new ArrayList<>();

    public final Item masterTicket;
    public final Item ticket;
    public final Item ticketStub;
    public final TagKey<Item> material;

    private TicketData(@Nonnull ItemLike masterTicket, @Nonnull ItemLike ticket, @Nonnull ItemLike ticketStub, @Nonnull TagKey<Item> material)
    {
        this.masterTicket = masterTicket.asItem();
        this.ticket = ticket.asItem();
        this.ticketStub = ticketStub.asItem();
        this.material = material;
    }

    public static void create(@Nonnull ItemLike masterTicket, @Nonnull ItemLike ticket, @Nonnull ItemLike ticketStub, @Nonnull TagKey<Item> material) { DATA.add(new TicketData(masterTicket,ticket,ticketStub,material)); }

    @Nullable
    public static TicketData getForMaster(@Nonnull ItemStack masterTicket)
    {
        for(TicketData data : DATA)
        {
            if(data.masterTicket == masterTicket.getItem())
                return data;
        }
        return null;
    }

    @Nullable
    public static TicketData getForTicket(@Nonnull ItemStack ticket)
    {
        for(TicketData data : DATA)
        {
            if(data.ticket == ticket.getItem())
                return data;
        }
        return null;
    }

    @Nullable
    public static TicketData getForMaterial(@Nonnull ItemStack material)
    {
        for(TicketData data : DATA)
        {
            if(InventoryUtil.ItemHasTag(material, data.material))
                return data;
        }
        return null;
    }

}
