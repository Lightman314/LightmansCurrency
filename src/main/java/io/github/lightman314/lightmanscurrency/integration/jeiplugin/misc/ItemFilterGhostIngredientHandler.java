package io.github.lightman314.lightmanscurrency.integration.jeiplugin.misc;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ItemFilterScreen;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemFilterGhostIngredientHandler implements IGhostIngredientHandler<ItemFilterScreen> {

    @Override
    public <I> List<Target<I>> getTargetsTyped(ItemFilterScreen screen, ITypedIngredient<I> ingredient, boolean doStart) {
        if(ingredient.getType() == VanillaTypes.ITEM_STACK)
            return List.of(new FilterTarget<>(screen));
        return List.of();
    }

    @Override
    public void onComplete() { }

    private record FilterTarget<T>(ItemFilterScreen screen) implements Target<T>
    {
        @Override
        public Rect2i getArea() {
            return new Rect2i(screen.getGuiLeft() + 8,screen.getGuiTop() + 102,16,16);
        }
        @Override
        public void accept(T ingredient) {
            if(ingredient instanceof ItemStack stack)
                this.screen.setFakeSlotItem(stack);
        }
    }

}