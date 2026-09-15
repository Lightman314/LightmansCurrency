package io.github.lightman314.lightmanscurrency.features.colors;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.types.VanillaColor;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.Map;
import java.util.function.Consumer;

public final class ColorDisplay implements TooltipProvider {

    public static final TextEntry TOOLTIP_COLORED_ITEM = TextEntry.tooltip(LCApi.MODID,"colored_item");

    private static final Map<VanillaColor,ColorDisplay> CACHE;
    static {
        ImmutableMap.Builder<VanillaColor,ColorDisplay> builder = ImmutableMap.builderWithExpectedSize(DyeColor.values().length);
        for(VanillaColor c : VanillaColor.values())
            builder.put(c,new ColorDisplay(c));
        CACHE = builder.build();
    }
    public static ColorDisplay of(VanillaColor color) { return CACHE.get(color); }

    public static final Codec<ColorDisplay> CODEC = VanillaColor.CODEC.xmap(ColorDisplay::of,ColorDisplay::color);
    public static final StreamCodec<ByteBuf,ColorDisplay> STREAM_CODEC = VanillaColor.STREAM_CODEC.map(ColorDisplay::of,ColorDisplay::color);

    private final VanillaColor color;
    public VanillaColor color() { return this.color; }
    private ColorDisplay(VanillaColor color) { this.color = color; }
    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components) {
        consumer.accept(TOOLTIP_COLORED_ITEM.get(LCText.VANILLA_COLORS.getColored(this.color)));
    }

    @Override
    public int hashCode() { return this.color.hashCode(); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ColorDisplay other)
            return this.color == other.color;
        return false;
    }

    @Override
    public String toString() { return this.color.toString(); }
}