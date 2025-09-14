package io.github.lightman314.lightmanscurrency.integration.reiplugin.misc;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ItemFilterScreen;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

public class ItemFilterDraggableStackVisitor implements DraggableStackVisitor<ItemFilterScreen> {
    @Override
    public <R extends Screen> boolean isHandingScreen(R screen) {
        return screen instanceof ItemFilterScreen;
    }

    @Override
    public DraggedAcceptorResult acceptDraggedStack(DraggingContext<ItemFilterScreen> context, DraggableStack stack) {

        ItemFilterScreen screen = context.getScreen();
        if(stack.getStack().getValue() instanceof ItemStack item)
        {
            Point mousePos = context.getCurrentPosition();
            if(ScreenArea.of(screen.getCorner().offset(8,102),16,16).isMouseInArea(mousePos.x,mousePos.y))
                screen.setFakeSlotItem(item);
        }
        return DraggableStackVisitor.super.acceptDraggedStack(context, stack);
    }
}