package io.github.lightman314.lightmanscurrency.api.icon.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.IconType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;

import java.util.Optional;

public class ItemIcon extends IconData {

    private static final MapCodec<ItemIcon> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ItemStackTemplate.CODEC.fieldOf("item").forGetter(ItemIcon::item),
            Codec.STRING.optionalFieldOf("text").forGetter(ItemIcon::countOverride)
            ).apply(builder,ItemIcon::new));
    private static final StreamCodec<RegistryFriendlyByteBuf,ItemIcon> STREAM_CODEC = StreamCodec.composite(
            ItemStackTemplate.STREAM_CODEC,ItemIcon::item,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),ItemIcon::countOverride,
            ItemIcon::new);
    public static final IconType<ItemIcon> TYPE = new IconType<>(MAP_CODEC,STREAM_CODEC);

    private final ItemStackTemplate item;
    public final ItemStackTemplate item() { return this.item; }
    private final Optional<String> countOverride;
    public final Optional<String> countOverride() { return this.countOverride; }
    private ItemIcon(ItemStackTemplate item,Optional<String> countOverride) {
        this.item = item;
        this.countOverride = countOverride;
    }

    public static ItemIcon of(ItemLike item) { return of(new ItemStackTemplate(item.asItem())); }
    public static ItemIcon of(ItemStackTemplate item) { return new ItemIcon(item,Optional.empty()); }
    public static ItemIcon of(ItemStackTemplate item,String countOverride) { return new ItemIcon(item,Optional.of(countOverride)); }
    public static ItemIcon of(ItemStack stack) { return of(revertToTemplate(stack)); }
    public static ItemIcon of(ItemStack stack,String countOverride) { return of(revertToTemplate(stack),countOverride); }

    private static ItemStackTemplate revertToTemplate(ItemStack stack) { return new ItemStackTemplate(stack.getItem(),stack.getCount(),stack.getComponentsPatch()); }

    @Override
    public IconType<?> getType() { return TYPE; }

}