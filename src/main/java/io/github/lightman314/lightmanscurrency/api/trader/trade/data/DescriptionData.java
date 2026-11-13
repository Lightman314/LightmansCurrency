package io.github.lightman314.lightmanscurrency.api.trader.trade.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class DescriptionData implements FancyPacketMap.FancyPacketWriter {

    public static DescriptionData create() { return new DescriptionData("",""); }

    public static final MapCodec<DescriptionData> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.STRING.optionalFieldOf("description","").forGetter(d -> d.description),
            Codec.STRING.optionalFieldOf("tooltip","").forGetter(d -> d.tooltip)
    ).apply(builder,DescriptionData::new));
    public static final Codec<DescriptionData> CODEC = MAP_CODEC.codec();

    public static final StreamCodec<ByteBuf,DescriptionData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,d -> d.description,
            ByteBufCodecs.STRING_UTF8,d -> d.tooltip,
            DescriptionData::new);


    public String description;
    public String tooltip;
    private DescriptionData(String description,String tooltip) {
        this.description = description;
        this.tooltip = tooltip;
    }

    public List<Component> getTooltipLines() {
        if(!this.tooltip.isBlank()) {
            List<Component> lines = new ArrayList<>();
            for(String line : this.tooltip.split("\\\\n"))
                lines.add(Component.literal(line));
            return lines;
        }
        return List.of(Component.literal(this.description));
    }

    public void copyFrom(DescriptionData other) {
        this.description = other.description;
        this.tooltip = other.tooltip;
    }

    public Optional<DescriptionData> optional() {
        if(this.description.isBlank() && this.tooltip.isBlank())
            return Optional.empty();
        return Optional.of(this);
    }

    @Override
    public void writeToPacket(FancyPacketMap.Mutable packet) {
        packet.setString("description",this.description)
                .setString("tooltip",this.tooltip);
    }

    public static DescriptionData readFromPacket(FancyPacketMap packet) { return new DescriptionData(packet.getString("description"),packet.getString("tooltip")); }

}
