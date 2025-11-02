package io.github.lightman314.lightmanscurrency.integration.reiplugin.util;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.easy.GhostSlot;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IEasyScreen;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class EasyDraggableStackVisitor<T extends Screen & IEasyScreen> implements DraggableStackVisitor<T> {

    private final Class<T> clazz;
    public EasyDraggableStackVisitor(Class<T> clazz) { this.clazz = clazz; }

    @Override
    public <R extends Screen> boolean isHandingScreen(R screen) { return screen.getClass() == this.clazz; }

    @Override
    public DraggedAcceptorResult acceptDraggedStack(DraggingContext<T> context, DraggableStack stack) {
        if(this.isHandingScreen(context.getScreen()))
        {
            IEasyScreen screen = context.getScreen();
            Object value = stack.getStack().getValue();
            Class<?> valueType = value.getClass();
            ScreenPosition mousePos = ScreenPosition.of(context.getCurrentPosition().x,context.getCurrentPosition().y);
            for(GhostSlot<?> slot : screen.getGhostSlots())
            {
                if(slot.clazz() == valueType && slot.area().isMouseInArea(mousePos))
                {
                    try {
                        slot.tryAccept(value);
                    } catch (Exception e) {
                        LightmansCurrency.LogDebug("Error casing DraggableStack value!");
                    }
                }
            }
        }
        return DraggableStackVisitor.super.acceptDraggedStack(context, stack);
    }

    @Override
    public Stream<BoundsProvider> getDraggableAcceptingBounds(DraggingContext<T> context, DraggableStack stack) {

        IEasyScreen screen = context.getScreen();
        if(this.isHandingScreen(context.getScreen()))
        {

            List<BoundsProvider> validSlots = new ArrayList<>();
            Class<?> valueType = stack.getStack().getValue().getClass();
            for(GhostSlot<?> slot : screen.getGhostSlots())
            {
                if(slot.clazz() == valueType)
                    validSlots.add(BoundsProvider.ofRectangle(new Rectangle(slot.area().x,slot.area().y,slot.area().width,slot.area().height)));
            }
            return validSlots.stream();
        }
        return DraggableStackVisitor.super.getDraggableAcceptingBounds(context, stack);
    }

}