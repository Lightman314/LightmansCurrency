package io.github.lightman314.lightmanscurrency.common.traders.paygate.tickets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;

public record TicketInfo(Item ticketItem,long ticketID,int ticketColor) {

    public static final Codec<TicketInfo> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(TicketInfo::ticketItem),
            Codec.LONG.fieldOf("id").forGetter(TicketInfo::ticketID),
            Codec.INT.fieldOf("color").forGetter(TicketInfo::ticketColor)
    ).apply(builder,TicketInfo::new));

    public static final StreamCodec<RegistryFriendlyByteBuf,TicketInfo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ITEM),TicketInfo::ticketItem,
            ByteBufCodecs.VAR_LONG,TicketInfo::ticketID,
            ByteBufCodecs.INT,TicketInfo::ticketColor,
            TicketInfo::new);

}
