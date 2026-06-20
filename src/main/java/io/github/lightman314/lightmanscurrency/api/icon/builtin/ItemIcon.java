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
import net.minecraft.world.level.ItemLike;

import java.util.Optional;

public class ItemIcon extends IconData {

    private static final MapCodec<ItemIcon> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ItemStack.CODEC.fieldOf("item").forGetter(ItemIcon::item),
            Codec.STRING.optionalFieldOf("text").forGetter(ItemIcon::countOverride)
            ).apply(builder,ItemIcon::new));
    private static final StreamCodec<RegistryFriendlyByteBuf,ItemIcon> STREAM_CODEC = StreamCodec.composite(
            ItemStack.validatedStreamCodec(ItemStack.STREAM_CODEC),ItemIcon::item,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),ItemIcon::countOverride,
            ItemIcon::new);
    public static final IconType<ItemIcon> TYPE = new IconType<>(MAP_CODEC,STREAM_CODEC);

    private final ItemStack item;
    public final ItemStack item() { return this.item.copy(); }
    private final Optional<String> countOverride;
    public final Optional<String> countOverride() { return this.countOverride; }
    private ItemIcon(ItemStack item,Optional<String> countOverride) {
        this.item = item.copy();
        this.countOverride = countOverride;
    }

    public static ItemIcon of(ItemLike item) { return of(new ItemStack(item)); }
    public static ItemIcon of(ItemStack item) { return new ItemIcon(item,Optional.empty()); }
    public static ItemIcon of(ItemStack item,String countOverride) { return new ItemIcon(item,Optional.of(countOverride)); }

    @Override
    public IconType<?> getType() { return TYPE; }

}