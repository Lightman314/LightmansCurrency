package io.github.lightman314.lightmanscurrency.features.tickets.data;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.function.Consumer;

public record TicketID(long ticketID) implements TooltipProvider {

    public static final Codec<TicketID> CODEC = CodecHelper.longRange(-1,Long.MAX_VALUE).xmap(TicketID::new,TicketID::ticketID);
    public static final StreamCodec<ByteBuf,TicketID> STREAM_CODEC = ByteBufCodecs.LONG.map(TicketID::new,TicketID::ticketID);

    public static final TextEntry TOOLTIP = TextEntry.tooltip(LCApi.MODID,"ticket.id");

    public static final TicketID NULL = new TicketID(-1);

    public static final TicketID CREATIVE_PAPER = new TicketID(0);
    public static final TicketID CREATIVE_GOLD = new TicketID(1);

    public static final long STARTING_ID = 16;

    public boolean isValid() { return this.ticketID >= 0; }

    @Override
    public void addToTooltip(Item.TooltipContext context,Consumer<Component> consumer,TooltipFlag flag,DataComponentGetter components) {
        if(this.ticketID < 0)
            return;
        consumer.accept(TOOLTIP.get(this.ticketID));
    }

}