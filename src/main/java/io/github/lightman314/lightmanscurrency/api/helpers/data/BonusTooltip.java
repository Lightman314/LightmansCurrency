package io.github.lightman314.lightmanscurrency.api.helpers.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.TooltipHelper;
import io.github.lightman314.lightmanscurrency.api.text.MultiLineTextEntry;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.List;
import java.util.function.Consumer;

public record BonusTooltip(MultiLineTextEntry text, List<String> args) implements TooltipProvider {
    public BonusTooltip(MultiLineTextEntry text) { this(text,List.of()); }

    public static final TextEntry TOOLTIP_INFO_BLURB = TextEntry.tooltip(LCApi.MODID,"info_blurb");

    private static final Codec<BonusTooltip> FULL_CODEC =  RecordCodecBuilder.create(builder -> builder.group(
            MultiLineTextEntry.CODEC.fieldOf("text").forGetter(BonusTooltip::text),
            Codec.STRING.listOf().fieldOf("args").forGetter(BonusTooltip::args)
    ).apply(builder,BonusTooltip::new));

    public static final Codec<BonusTooltip> CODEC = Codec.withAlternative(FULL_CODEC,MultiLineTextEntry.CODEC.xmap(BonusTooltip::new,BonusTooltip::text));
    public static final StreamCodec<ByteBuf,BonusTooltip> STREAM_CODEC = StreamCodec.composite(
            MultiLineTextEntry.STREAM_CODEC,BonusTooltip::text,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),BonusTooltip::args,
            BonusTooltip::new);

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> builder, TooltipFlag flag, DataComponentGetter components) {
        if(flag.hasShiftDown())
            TooltipHelper.splitTooltips(this.text.get(this.args.toArray(Object[]::new)),ChatFormatting.GRAY).forEach(builder);
        else
            builder.accept(TOOLTIP_INFO_BLURB.getWithStyle(ChatFormatting.GRAY));
    }

}
