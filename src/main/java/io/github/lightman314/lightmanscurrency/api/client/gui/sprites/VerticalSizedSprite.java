package io.github.lightman314.lightmanscurrency.api.client.gui.sprites;

import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public interface VerticalSizedSprite {

    Identifier sprite();
    int width();

    default SizedSprite build(int height) { return new Sized(this,height); }


    interface Template<T> {
        VerticalSizedSprite buildSprite(T argument);
    }

    interface Builder extends Template<Void> {
        default VerticalSizedSprite buildSprite(Void argument) { return this.buildSprite(); }
        VerticalSizedSprite buildSprite();
    }

    interface WithContext extends VerticalSizedSprite {
        void defineContext(boolean active,boolean hovered);
    }

    record Simple(Supplier<Identifier> spriteSource,int width) implements VerticalSizedSprite {
        public Simple(Identifier sprite,int width) { this(() -> sprite,width); }
        @Override
        public Identifier sprite() { return this.spriteSource.get(); }
    }

    record Sized(VerticalSizedSprite vss,int height) implements SizedSprite.WithContext{
        @Override
        public Identifier sprite() { return this.vss.sprite(); }
        @Override
        public int width() { return this.vss.width(); }
        @Override
        public void defineContext(boolean active, boolean hovered) {
            if(this.vss instanceof WithContext wc)
                wc.defineContext(active,hovered);
        }
    }

}
