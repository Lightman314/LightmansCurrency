package io.github.lightman314.lightmanscurrency.features.tickets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.NumberHelper;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.function.Consumer;

public record TicketUses(int uses,int maxUses) implements TooltipProvider {
    public TicketUses(int uses,int maxUses) { this.uses = Math.max(uses,0); this.maxUses = Math.max(maxUses,0); }
    public TicketUses(int uses) { this(uses,uses); }

    public static final int DEFAULT_USE_LIMIT = 99;

    public static final TicketUses UNLIMITED = new TicketUses(0,0);
    public static final TicketUses DEFAULT = new TicketUses(1);

    public static TicketUses get(DataComponentGetter item) { return item.getOrDefault(LCDataComponents.TICKET_USES,DEFAULT); }

    private static final Codec<TicketUses> MAP_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.intRange(0,Integer.MAX_VALUE).fieldOf("uses").forGetter(TicketUses::uses),
            Codec.intRange(0,Integer.MAX_VALUE).fieldOf("maxUses").forGetter(TicketUses::maxUses)
    ).apply(builder,TicketUses::new));
    public static final Codec<TicketUses> CODEC = MAP_CODEC.withAlternative(Codec.INT.xmap(TicketUses::new,TicketUses::uses)).withAlternative(MapCodec.unitCodec(UNLIMITED));
    public static final StreamCodec<ByteBuf,TicketUses> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,u -> u.uses,
            ByteBufCodecs.INT,TicketUses::maxUses,
            TicketUses::new);

    public static final TextEntry TOOLTIP = TextEntry.tooltip(LCApi.MODID,"ticket.uses");
    public static final TextEntry TOOLTIP_INFINITE = TextEntry.tooltip(LCApi.MODID,"ticket.uses.infinite");

    public int uses() { return this.isUnlimited() ? Integer.MAX_VALUE : this.uses; }
    public boolean isFinite() { return this.maxUses > 0; }
    public boolean isUnlimited() { return this.maxUses <= 0; }

    public TicketUses consume(int count) {
        if(this.isUnlimited())
            return this;
        return new TicketUses(this.uses() - count,this.maxUses);
    }

    private Component getDurabilityText() { return this.isUnlimited() ? TOOLTIP_INFINITE.get() : Component.literal(NumberHelper.prettyInteger(this.uses)); }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components) {
        consumer.accept(TOOLTIP.get(this.getDurabilityText()).withStyle(ChatFormatting.GRAY));
    }

}