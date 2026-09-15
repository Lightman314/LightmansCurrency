package io.github.lightman314.lightmanscurrency.integration.jei;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.IngredientResult;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.ScreenHelper;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.FancyMenuScreen;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.runtime.IClickableIngredient;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FancyGuiHandlerArea implements IGuiContainerHandler<FancyMenuScreen<?>> {

    public static final FancyGuiHandlerArea INSTANCE = new FancyGuiHandlerArea();

    protected FancyGuiHandlerArea() {}

    @Override
    public List<Rect2i> getGuiExtraAreas(FancyMenuScreen<?> containerScreen) {
        ScreenArea screenArea = containerScreen.getArea();
        List<Rect2i> result = new ArrayList<>();
        for(Renderable r : containerScreen.renderables) {
            if(r instanceof AbstractWidget w && w.visible) {
                ScreenArea area = ScreenHelper.getWidgetArea(w);
                if(area.isOutsideOf(screenArea))
                    result.add(ScreenHelper.asRect(area));
            }
        }
        for(ScreenArea area : containerScreen.getAreaClaims()) {
            if(area.isOutsideOf(screenArea))
                result.add(ScreenHelper.asRect(area));
        }
        return result;
    }

    @Override
    public Optional<? extends IClickableIngredient<?>> getClickableIngredientUnderMouse(IClickableIngredientFactory builder,FancyMenuScreen<?> screen,double mouseX,double mouseY) {
        ScreenPosition mousePos = ScreenPosition.of(mouseX,mouseY);
        Optional<IngredientResult> optional = screen.getHoveredIngredient(mousePos);
        if(optional.isPresent()) {
            IngredientResult result = optional.get();
            if(result.ingredient() instanceof ItemStack item)
                return builder.createBuilder(item).buildWithArea(ScreenHelper.asRect(result.area()));
            if(result.ingredient() instanceof FluidStack fluid)
                return builder.createBuilder(NeoForgeTypes.FLUID_STACK,fluid).buildWithArea(ScreenHelper.asRect(result.area()));
        }
        return Optional.empty();
    }

}