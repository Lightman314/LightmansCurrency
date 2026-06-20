package io.github.lightman314.lightmanscurrency.api.icon.builtin;

import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.IconType;
import net.minecraft.resources.Identifier;

public class SpriteIcon extends IconData {

    public static final IconType<SpriteIcon> TYPE = new IconType<>(
            Identifier.CODEC.fieldOf("sprite").xmap(SpriteIcon::new,SpriteIcon::sprite),
            Identifier.STREAM_CODEC.map(SpriteIcon::new,SpriteIcon::sprite));

    private final Identifier sprite;
    public Identifier sprite() { return this.sprite; }
    private SpriteIcon(Identifier sprite) { this.sprite = sprite; }

    @Override
    public IconType<?> getType() { return TYPE; }

}