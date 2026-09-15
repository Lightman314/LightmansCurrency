package io.github.lightman314.lightmanscurrency.integration.jei;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.GhostSlot;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.ScreenHelper;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.FancyMenuScreen;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;

import java.util.ArrayList;
import java.util.List;

public class FancyGhostIngredientHandler<T extends FancyMenuScreen<?>> implements IGhostIngredientHandler<T> {

    public FancyGhostIngredientHandler() {}

    @Override
    public <I> List<Target<I>> getTargetsTyped(T screen,ITypedIngredient<I> ingredient,boolean doStart) {
        List<GhostSlot<?>> ghostSlots = screen.getGhostSlots();
        List<Target<I>> targets = new ArrayList<>();
        Class<?> ingredientType = ingredient.getType().getIngredientClass();
        for(GhostSlot<?> slot : ghostSlots) {
            if(slot.clazz() == ingredientType) {
                try {
                    targets.add(new GhostTarget<>((GhostSlot<I>)slot));
                } catch (Exception ignored) {}
            }
        }
        return targets;
    }

    @Override
    public void onComplete() { }

    private record GhostTarget<T>(GhostSlot<T> slot) implements Target<T> {
        @Override
        public Rect2i getArea() { return ScreenHelper.asRect(this.slot.area()); }
        @Override
        public void accept(T ingredient) { this.slot.handler().accept(ingredient); }
    }

}
