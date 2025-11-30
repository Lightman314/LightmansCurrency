package io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;

import javax.annotation.Nullable;

public interface IWidgetWrapper {

    Object getWrappedWidget();

    interface WrappingGuiEvents extends IWidgetWrapper, GuiEventListener
    {
        @Override
        GuiEventListener getWrappedWidget();

        @Override
        default void mouseMoved(double mouseX, double mouseY) { this.getWrappedWidget().mouseMoved(mouseX,mouseY); }
        @Override
        default boolean mouseClicked(double mouseX, double mouseY, int button) { return this.getWrappedWidget().mouseClicked(mouseX, mouseY, button); }
        @Override
        default boolean mouseReleased(double mouseX, double mouseY, int button) { return this.getWrappedWidget().mouseReleased(mouseX, mouseY, button); }
        @Override
        default boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) { return this.getWrappedWidget().mouseDragged(mouseX, mouseY, button, dragX, dragY); }
        @Override
        default boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) { return this.getWrappedWidget().mouseScrolled(mouseX, mouseY, scrollX, scrollY); }
        @Override
        default boolean keyPressed(int keyCode, int scanCode, int modifiers) { return this.getWrappedWidget().keyPressed(keyCode, scanCode, modifiers); }
        @Override
        default boolean keyReleased(int keyCode, int scanCode, int modifiers) { return this.getWrappedWidget().keyReleased(keyCode, scanCode, modifiers); }
        @Override
        default boolean charTyped(char codePoint, int modifiers) { return this.getWrappedWidget().charTyped(codePoint, modifiers); }
        @Nullable
        @Override
        default ComponentPath nextFocusPath(FocusNavigationEvent event) { return this.getWrappedWidget().nextFocusPath(event); }
        @Override
        default boolean isMouseOver(double mouseX, double mouseY) { return this.getWrappedWidget().isMouseOver(mouseX, mouseY); }
        default boolean isFocused() { return this.getWrappedWidget().isFocused(); }
        default void setFocused(boolean focused) { this.getWrappedWidget().setFocused(focused); }
        @Nullable
        @Override
        default ComponentPath getCurrentFocusPath() { return this.getWrappedWidget().getCurrentFocusPath(); }
        @Override
        default ScreenRectangle getRectangle() { return this.getWrappedWidget().getRectangle(); }
        @Override
        default int getTabOrderGroup() { return this.getWrappedWidget().getTabOrderGroup(); }
    }

}
