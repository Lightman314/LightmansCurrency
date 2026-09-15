package io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.PreeditEvent;
import org.jspecify.annotations.Nullable;

public interface IWidgetWrapper {

    Object getWrappedWidget();

    interface WrappingGuiEvents extends IWidgetWrapper, GuiEventListener {
        @Override
        GuiEventListener getWrappedWidget();

        @Override
        default void mouseMoved(double x, double y) { this.getWrappedWidget().mouseMoved(x, y); }
        @Override
        default boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) { return this.getWrappedWidget().mouseClicked(event, doubleClick); }
        @Override
        default boolean mouseReleased(MouseButtonEvent event) { return this.getWrappedWidget().mouseReleased(event); }
        @Override
        default boolean mouseDragged(MouseButtonEvent event, double dx, double dy) { return this.getWrappedWidget().mouseDragged(event, dx, dy); }
        @Override
        default boolean mouseScrolled(double x, double y, double scrollX, double scrollY) { return this.getWrappedWidget().mouseScrolled(x, y, scrollX, scrollY); }
        @Override
        default boolean keyPressed(KeyEvent event) { return this.getWrappedWidget().keyPressed(event); }
        @Override
        default boolean keyReleased(KeyEvent event) { return this.getWrappedWidget().keyReleased(event); }
        @Override
        default boolean charTyped(CharacterEvent event) { return this.getWrappedWidget().charTyped(event); }
        @Override
        default boolean preeditUpdated(@Nullable PreeditEvent event) { return this.getWrappedWidget().preeditUpdated(event); }
        @Override
        default @Nullable ComponentPath nextFocusPath(FocusNavigationEvent navigationEvent) { return this.getWrappedWidget().nextFocusPath(navigationEvent); }
        @Override
        default boolean isMouseOver(double mouseX, double mouseY) { return this.getWrappedWidget().isMouseOver(mouseX, mouseY); }
        @Override
        default void setFocused(boolean focused) { this.getWrappedWidget().setFocused(focused); }
        @Override
        default boolean isFocused() { return this.getWrappedWidget().isFocused(); }
        @Override
        default boolean shouldTakeFocusAfterInteraction() { return this.getWrappedWidget().shouldTakeFocusAfterInteraction(); }
        @Override
        @Nullable
        default ComponentPath getCurrentFocusPath() { return this.getWrappedWidget().getCurrentFocusPath(); }
        @Override
        default ScreenRectangle getRectangle() { return this.getWrappedWidget().getRectangle(); }
        @Override
        default ScreenRectangle getBorderForArrowNavigation(ScreenDirection opposite) { return this.getWrappedWidget().getBorderForArrowNavigation(opposite); }
        @Override
        default int getTabOrderGroup() { return this.getWrappedWidget().getTabOrderGroup(); }

    }

}
