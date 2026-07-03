package io.github.lightman314.lightmanscurrency.api.client.gui.sprites;

import net.minecraft.resources.Identifier;

public interface SizedSprite {

    Identifier sprite();
    int width();
    int height();

    interface Template<T> {
        SizedSprite buildSprite(T argument);
    }

    interface Builder extends Template<Void> {
        default SizedSprite buildSprite(Void argument) { return this.buildSprite(); }
        SizedSprite buildSprite();
    }

    interface WithContext extends SizedSprite {
        void defineContext(boolean active,boolean hovered);
    }

}