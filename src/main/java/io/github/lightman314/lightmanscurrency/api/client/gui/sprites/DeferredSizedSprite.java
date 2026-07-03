package io.github.lightman314.lightmanscurrency.api.client.gui.sprites;

import net.minecraft.resources.Identifier;

import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class DeferredSizedSprite implements SizedSprite {

    private final Supplier<Identifier> sprite;
    private final int width;
    private final int height;
    public DeferredSizedSprite(Supplier<Identifier> sprite, int width, int height) {
        this.sprite = sprite;
        this.width = width;
        this.height = height;
    }
    @Override
    public Identifier sprite() { return this.sprite.get(); }
    @Override
    public int width() { return this.width; }
    @Override
    public int height() { return this.height; }

    public static SizedSprite.Template<BooleanSupplier> toggleSprite(Identifier onSprite,Identifier offSprite,int width,int height) {
        return supplier -> new DeferredSizedSprite(() -> supplier.getAsBoolean() ? onSprite : offSprite,width,height);
    }

    public static SizedSprite.Template<BooleanSupplier> toggleAndHoverToggleSprite(Identifier onSprite, Identifier offSprite, int width, int height) {
        return toggleAndHoverToggleSprite(onSprite,onSprite.withSuffix("_hovered"),offSprite,offSprite.withSuffix("_hovered"),width,height);
    }
    public static SizedSprite.Template<BooleanSupplier> toggleAndHoverToggleSprite(Identifier onSprite, Identifier onSpriteHovered, Identifier offSprite, Identifier offSpriteHovered, int width, int height) {
        return sup -> new WithContext((active, hovered) -> {
            if(sup.getAsBoolean())
                return hovered ? onSpriteHovered : onSprite;
            return hovered ? offSpriteHovered : offSprite;
        },width,height);
    }

    public static class WithContext implements SizedSprite.WithContext {

        private final BiFunction<Boolean,Boolean,Identifier> sprite;
        private final int width;
        private final int height;

        private boolean active = true;
        private boolean hovered = false;

        public WithContext(BiFunction<Boolean,Boolean,Identifier> sprite,int width,int height) {
            this.sprite = sprite;
            this.width = width;
            this.height = height;
        }

        @Override
        public Identifier sprite() { return this.sprite.apply(this.active,this.hovered); }
        @Override
        public int width() { return this.width; }
        @Override
        public int height() { return this.height; }
        @Override
        public void defineContext(boolean active, boolean hovered) {
            this.active = active;
            this.hovered = hovered;
        }

    }

}