package io.github.lightman314.lightmanscurrency.client.gui.screen.easy.tabs;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces.IEasyScreen;
import io.github.lightman314.lightmanscurrency.common.easy.IEasyTickable;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces.ITooltipSource;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class EasyTab<T extends IEasyScreen> implements IEasyScreen {

    protected final T screen;
    protected EasyTab(@NotNull T screen) { this.screen = screen; }

    public List<Component> getTooltip() {return null; }
    public int getTabColor() { return 0xFFFFFF; }
    public abstract @NotNull IconData getTabIcon();
    public boolean isTabVisible(Player player) { return true; }
    public boolean canOpenTab(Player player) { return true; }

    private final List<Object> tabObjects = new ArrayList<>();

    public final void openTab() {
        if(this instanceof GuiEventListener l)
            this.addGuiListener(l);
        if(this instanceof IMouseListener l)
            this.addMouseListener(l);
        if(this instanceof ITooltipSource t)
            this.addTooltipSource(t);
        if(this instanceof IEasyTickable t)
            this.addTicker(t::tick);
        LightmansCurrency.LogInfo("Opening tab.");
        this.initializeTab();
    }

    protected abstract void initializeTab();
    public final void closeTab() {
        for(Object widget : this.tabObjects)
            this.screen.removeChild(widget);
        this.tabObjects.clear();
        LightmansCurrency.LogInfo("Closing tab.");
        this.onTabClosed();
    }

    protected void onTabClosed() {}

    public abstract void renderTab(PoseStack pose, int mouseX, int mouseY, float partialTicks);
    public void renderTabAfterWidgets(PoseStack pose, int mouseX, int mouseY, float partialTicks) {}
    public void renderTabAfterTooltips(PoseStack pose, int mouseX, int mouseY, float partialTicks) {}


    //IEasyScreen overrides
    @Override
    public final int width() { return this.screen.width(); }
    @Override
    public final int height() { return this.screen.height(); }
    @Override
    public final int guiLeft() { return this.screen.guiLeft(); }
    @Override
    public final int guiTop() { return this.screen.guiTop(); }
    @Override
    public final Player getPlayer() { return this.screen.getPlayer(); }
    @Override
    public final Font getFont() { return this.screen.getFont(); }
    @Override
    public final <W extends Renderable> @NotNull W addRenderableOnly(@NotNull W renderable) {
        this.screen.addRenderableOnly(renderable);
        this.tabObjects.add(renderable);
        return renderable;
    }
    @Override
    public final <W extends Renderable & GuiEventListener> @NotNull W addRenderableWidget(@NotNull W widget) {
        this.screen.addRenderableWidget(widget);
        this.tabObjects.add(widget);
        return widget;
    }
    @Override
    public final <W extends GuiEventListener> @NotNull W addGuiListener(@NotNull W listener) {
        this.screen.addGuiListener(listener);
        this.tabObjects.add(listener);
        return listener;
    }
    @Override
    public final void addMouseListener(@NotNull IMouseListener listener) {
        this.screen.addMouseListener(listener);
        this.tabObjects.add(listener);
    }
    @Override
    public final void addTooltipSource(@NotNull ITooltipSource tooltipSource) {
        this.screen.addTooltipSource(tooltipSource);
        this.tabObjects.add(tooltipSource);
    }
    @Override
    public final void addTicker(@NotNull Runnable ticker) {
        this.screen.addTicker(ticker);
        this.tabObjects.add(ticker);
    }
    @Override
    public final void removeChild(@NotNull Object widget) {
        this.screen.removeChild(widget);
        this.tabObjects.remove(widget);
    }
}
