package io.github.lightman314.lightmanscurrency.integration.jeiplugin.util;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.easy.GhostSlot;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IEasyScreen;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EasyGhostIngredientHandler<T extends Screen & IEasyScreen> implements IGhostIngredientHandler<T> {

    public EasyGhostIngredientHandler(Class<T> clazz) {}

    @Override
    public <I> List<Target<I>> getTargetsTyped(T screen, ITypedIngredient<I> ingredient, boolean doStart) {
        List<GhostSlot<?>> ghostSlots = screen.getGhostSlots();
        List<Target<I>> targets = new ArrayList<>();
        Class<?> ingredientType = ingredient.getIngredient().getClass();
        for(GhostSlot<?> slot : ghostSlots)
        {
            if(slot.clazz() == ingredientType)
            {
                try {
                    targets.add(new GhostTarget<>((GhostSlot<I>)slot));
                } catch (Exception e) {
                    LightmansCurrency.LogDebug("Error casting ghost slot!",e);
                }
            }
        }
        return targets;
    }

    @Override
    public void onComplete() { }

    private static class GhostTarget<T> implements IGhostIngredientHandler.Target<T>
    {
        private final GhostSlot<T> slot;
        public GhostTarget(GhostSlot<T> slot) { this.slot = slot; }
        @Override
        public Rect2i getArea() { return new Rect2i(this.slot.area().x,this.slot.area().y,this.slot.area().width,this.slot.area().height); }
        @Override
        public void accept(T ingredient) { this.slot.handler().accept(ingredient); }
    }

}