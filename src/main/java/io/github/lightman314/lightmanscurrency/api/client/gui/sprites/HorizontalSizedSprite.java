package io.github.lightman314.lightmanscurrency.api.client.gui.sprites;

import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public interface HorizontalSizedSprite {

    Identifier sprite();
    int height();

    default SizedSprite build(int width) { return new Sized(this,width); }

    interface Template<T> {
        HorizontalSizedSprite buildSprite(T argument);
    }

    interface Builder extends Template<Void> {
        default HorizontalSizedSprite buildSprite(Void argument) { return this.buildSprite(); }
        HorizontalSizedSprite buildSprite();
    }

    interface WithContext extends VerticalSizedSprite {
        void defineContext(boolean active,boolean hovered);
    }

    record Simple(Supplier<Identifier> spriteSource,int height) implements HorizontalSizedSprite {
        public Simple(Identifier sprite,int height) { this(() -> sprite,height); }
        @Override
        public Identifier sprite() { return this.spriteSource.get(); }
    }

    record Sized(HorizontalSizedSprite hss,int width) implements SizedSprite.WithContext {
        @Override
        public Identifier sprite() { return this.hss.sprite(); }
        @Override
        public int height() { return this.hss.height(); }
        @Override
        public void defineContext(boolean active, boolean hovered) {
            if(this.hss instanceof WithContext c)
                c.defineContext(active,hovered);
        }
    }

}
