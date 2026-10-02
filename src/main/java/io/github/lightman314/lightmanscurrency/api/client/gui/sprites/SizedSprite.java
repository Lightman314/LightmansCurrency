package io.github.lightman314.lightmanscurrency.api.client.gui.sprites;

import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public interface SizedSprite {

    Identifier sprite();
    int width();
    int height();

    default ScreenArea getArea(ScreenPosition pos) { return ScreenArea.of(pos,this.width(),this.height()); }

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

    record Simple(Supplier<Identifier> spriteSource,int width,int height) implements SizedSprite {
        public Simple(Identifier sprite,int width,int height) { this(() -> sprite,width,height); }
        @Override
        public Identifier sprite() { return this.spriteSource.get(); }
    }

}