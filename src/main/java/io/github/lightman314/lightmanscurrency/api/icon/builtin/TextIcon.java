package io.github.lightman314.lightmanscurrency.api.icon.builtin;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.IconType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;

public class TextIcon extends IconData {

    private static final MapCodec<TextIcon> MAP_CODEC = ComponentSerialization.CODEC.fieldOf("text").xmap(TextIcon::new,TextIcon::getText);
    private static final StreamCodec<RegistryFriendlyByteBuf,TextIcon> STREAM_CODEC = ComponentSerialization.STREAM_CODEC.map(TextIcon::new,TextIcon::getText);
    public static final IconType<TextIcon> TYPE = new IconType<>(MAP_CODEC,STREAM_CODEC);

    private final Component text;
    public Component getText() { return this.text; }
    private TextIcon(Component text) { this.text = text; }

    public static TextIcon of(String text) { return new TextIcon(Component.literal(text)); }
    public static TextIcon of(Component text) { return new TextIcon(text.copy()); }

    @Override
    public IconType<?> getType() { return TYPE; }
    @Override
    protected boolean equals(IconData icon) { return icon instanceof TextIcon i && i.text.equals(this.text); }
    @Override
    protected int hash() { return this.text.hashCode(); }

}
