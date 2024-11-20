package io.github.lightman314.lightmanscurrency.integration.jeiplugin.util;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IClickableIngredient;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JEIScreenArea<T extends EasyMenuScreen<?>> implements IGuiContainerHandler<T> {

    public static <T extends EasyMenuScreen<?>> JEIScreenArea<T> create(Class<T> clazz) { return new JEIScreenArea<>(); }

    private JEIScreenArea() { }

    @Nonnull
    @Override
    public List<Rect2i> getGuiExtraAreas(@Nonnull T screen) {
        List<Rect2i> list = new ArrayList<>();
        ScreenArea screenArea = screen.getArea();
        for(var child  : screen.children())
        {
            if(child instanceof EasyWidget widget && widget.visible)
            {
                ScreenArea area = widget.getArea();
                if(screenArea.isOutside(area))
                    list.add(new Rect2i(area.x,area.y,area.width,area.height));
            }
        }
        return list;
    }

    @Nonnull
    @Override
    public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(@Nonnull T screen, double mouseX, double mouseY) {
        ScreenPosition mousePos = ScreenPosition.of(mouseX,mouseY);
        //Check for hovered items
        Pair<ItemStack,ScreenArea> item = screen.getHoveredItem(mousePos);
        if(item != null && !item.getFirst().isEmpty())
            return Optional.of(new ClickableIngredient<>(new Ingredient<>(VanillaTypes.ITEM_STACK,item.getFirst()),item.getSecond()));
        //Check for hovered fluids
        Pair<FluidStack,ScreenArea> fluid = screen.getHoveredFluid(mousePos);
        if(fluid != null && !fluid.getFirst().isEmpty())
            return Optional.of(new ClickableIngredient<>(new Ingredient<>(ForgeTypes.FLUID_STACK,fluid.getFirst()),fluid.getSecond()));
        //I know nothing :(
        return IGuiContainerHandler.super.getClickableIngredientUnderMouse(screen, mouseX, mouseY);
    }

    @MethodsReturnNonnullByDefault
    private record ClickableIngredient<T>(ITypedIngredient<T> ingredient,ScreenArea area) implements IClickableIngredient<T>
    {
        @Override
        public ITypedIngredient<T> getTypedIngredient() { return this.ingredient; }
        @Override
        public Rect2i getArea() { return new Rect2i(area.x,area.y,area.width,area.height); }
    }

    @MethodsReturnNonnullByDefault
    private record Ingredient<T>(IIngredientType<T> type, T ingredient) implements ITypedIngredient<T>
    {
        @Override
        public IIngredientType<T> getType() { return this.type; }
        @Override
        public T getIngredient() { return this.ingredient; }
    }

}