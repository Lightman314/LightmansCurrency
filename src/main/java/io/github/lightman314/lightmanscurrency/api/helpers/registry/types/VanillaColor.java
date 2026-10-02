package io.github.lightman314.lightmanscurrency.api.helpers.registry.types;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

public enum VanillaColor {
    WHITE(DyeColor.WHITE),
    ORANGE(DyeColor.ORANGE),
    MAGENTA(DyeColor.MAGENTA),
    LIGHT_BLUE(DyeColor.LIGHT_BLUE),
    YELLOW(DyeColor.YELLOW),
    LIME(DyeColor.LIME),
    PINK(DyeColor.PINK),
    GRAY(DyeColor.GRAY),
    LIGHT_GRAY(DyeColor.LIGHT_GRAY),
    CYAN(DyeColor.CYAN),
    PURPLE(DyeColor.PURPLE),
    BLUE(DyeColor.BLUE),
    BROWN(DyeColor.BROWN),
    GREEN(DyeColor.GREEN),
    RED(DyeColor.RED),
    BLACK(DyeColor.BLACK),;

    public static final Codec<VanillaColor> CODEC = EnumHelper.buildCodec(VanillaColor.class,"Vanilla Color");
    public static final StreamCodec<ByteBuf,VanillaColor> STREAM_CODEC = EnumHelper.buildStreamCodec(VanillaColor.class,"Vanilla Color");

    public final String getResourceSafeName() { return EnumHelper.resourceSafeName(this); }

    public TagKey<Item> getDyeTag() { return this.color.getTag(); }
    public TagKey<Item> getDyedTag() { return this.color.getDyedTag(); }

    public final DyeColor color;
    VanillaColor(DyeColor color) { this.color = color; }

}
