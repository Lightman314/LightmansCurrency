package io.github.lightman314.lightmanscurrency.integration.jeiplugin.util;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.input.ClickableIngredient;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JEIScreenArea<T extends EasyMenuScreen<?>> implements IGuiContainerHandler<T> {

    public static <T extends EasyMenuScreen<?>> JEIScreenArea<T> create(Class<T> clazz, IIngredientManager manager) { return new JEIScreenArea<>(manager); }

    private final IIngredientManager manager;
    private JEIScreenArea(IIngredientManager manager) { this.manager = manager; }

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
            return this.createClickable(item.getFirst(),item.getSecond());
        //Check for hovered fluids
        Pair<FluidStack,ScreenArea> fluid = screen.getHoveredFluid(mousePos);
        if(fluid != null && !fluid.getFirst().isEmpty())
            return this.createClickable(fluid.getFirst(),fluid.getSecond());
        //I know nothing :(
        return IGuiContainerHandler.super.getClickableIngredientUnderMouse(screen, mouseX, mouseY);
    }

    private Optional<IClickableIngredient<?>> createClickable(ItemStack item,ScreenArea area) {
        return this.manager.createTypedIngredient(VanillaTypes.ITEM_STACK,item)
                .map(t -> new ClickableIngredient<>(t,asRect(area)));
    }
    private Optional<IClickableIngredient<?>> createClickable(FluidStack fluid,ScreenArea area) {
        return this.manager.createTypedIngredient(ForgeTypes.FLUID_STACK,fluid)
                .map(t -> new ClickableIngredient<>(t,asRect(area)));
    }

    private static ImmutableRect2i asRect(ScreenArea area) { return new ImmutableRect2i(area.x,area.y,area.width,area.height); }

}