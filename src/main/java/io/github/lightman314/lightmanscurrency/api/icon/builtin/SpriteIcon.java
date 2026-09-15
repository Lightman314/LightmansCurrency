package io.github.lightman314.lightmanscurrency.api.icon.builtin;

import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.IconType;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class SpriteIcon extends IconData {

    public static final IconType<SpriteIcon> TYPE = new IconType<>(
            Identifier.CODEC.fieldOf("sprite").xmap(SpriteIcon::new,SpriteIcon::sprite),
            Identifier.STREAM_CODEC.map(SpriteIcon::new,SpriteIcon::sprite));

    private final Identifier sprite;
    public Identifier sprite() { return this.sprite; }
    private SpriteIcon(Identifier sprite) { this.sprite = sprite; }

    public static SpriteIcon of(Identifier sprite) { return new SpriteIcon(sprite); }

    public static MultiIcon ofMulti(Identifier... sprites) {
        List<IconData> icons = new ArrayList<>();
        for(Identifier sprite : sprites) {
            icons.add(of(sprite));
        }
        return MultiIcon.of(icons);
    }

    @Override
    public IconType<?> getType() { return TYPE; }

}