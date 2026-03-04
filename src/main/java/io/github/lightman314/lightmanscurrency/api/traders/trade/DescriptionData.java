package io.github.lightman314.lightmanscurrency.api.traders.trade;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class DescriptionData {

    public static DescriptionData create() { return new DescriptionData("",""); }

    public static final Codec<DescriptionData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf("description").forGetter(d -> d.description),
            Codec.STRING.fieldOf("tooltip").forGetter(d -> d.tooltip)
    ).apply(builder,DescriptionData::new));

    public static final StreamCodec<ByteBuf,DescriptionData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,d -> d.description,
            ByteBufCodecs.STRING_UTF8,d -> d.tooltip,
            DescriptionData::new);

    public String description;
    public String tooltip;
    private DescriptionData(String description,String tooltip)
    {
        this.description = description;
        this.tooltip = tooltip;
    }

    public List<Component> getTooltipLines()
    {
        if(!this.tooltip.isBlank())
        {
            List<Component> lines = new ArrayList<>();
            for(String line : this.tooltip.split("\\\\n"))
                lines.add(EasyText.literal(line));
            return lines;
        }
        return ImmutableList.of(EasyText.literal(this.description));
    }

    public Optional<DescriptionData> optional() {
        if(this.description.isBlank() && this.tooltip.isBlank())
            return Optional.empty();
        return Optional.of(this);
    }

}