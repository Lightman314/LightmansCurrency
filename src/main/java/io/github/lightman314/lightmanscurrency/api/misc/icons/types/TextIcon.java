package io.github.lightman314.lightmanscurrency.api.misc.icons.types;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.GsonHelper;

public class TextIcon extends IconData
{

    public static final IconType<TextIcon> TYPE = new Type();

    private static final MapCodec<TextIcon> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ComponentSerialization.CODEC.fieldOf("text").forGetter(i -> i.text),
            Codec.INT.fieldOf("color").forGetter(i -> i.textColor)
    ).apply(builder,TextIcon::new));

    private static final StreamCodec<RegistryFriendlyByteBuf,TextIcon> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC,i -> i.text,
            ByteBufCodecs.INT,i -> i.textColor,
            TextIcon::new);

    public final Component text;
    public final int textColor;
    private TextIcon(Component text, int textColor) {
        this.text = text;
        this.textColor = textColor;
    }

    public static IconData ofText(Component text) { return ofText(text,0xFFFFFF); }
    public static IconData ofText(Component text, int textColor) { return new TextIcon(text,textColor); }

    @Override
    public IconType<?> getType() { return TYPE; }

    private static TextIcon loadText(CompoundTag tag, HolderLookup.Provider lookup) {
        Component text = Component.Serializer.fromJson(tag.getString("Text"),lookup);
        int color = tag.getInt("Color");
        return new TextIcon(text,color);
    }

    private static TextIcon parseText(JsonObject json, HolderLookup.Provider lookup) {
        Component text = ComponentSerialization.CODEC.decode(RegistryOps.create(JsonOps.INSTANCE,lookup),json.get("Text")).getOrThrow(JsonSyntaxException::new).getFirst();
        int color = GsonHelper.getAsInt(json,"Color",0x404040);
        return new TextIcon(text,color);
    }

    private static class Type extends IconType<TextIcon>
    {
        @Override
        public TextIcon loadOld(CompoundTag tag, HolderLookup.Provider lookup) { return loadText(tag,lookup);}
        @Override
        public TextIcon parseOld(JsonObject json, HolderLookup.Provider lookup) { return parseText(json,lookup); }
        @Override
        public MapCodec<TextIcon> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TextIcon> streamCodec() { return STREAM_CODEC; }
    }

}