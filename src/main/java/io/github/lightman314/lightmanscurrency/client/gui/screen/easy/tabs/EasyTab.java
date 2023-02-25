package io.github.lightman314.lightmanscurrency.client.gui.screen.easy.tabs;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces.IEasyScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import net.minecraft.client.gui.Font;
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
        this.addChild(this);
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
    public final <W> @NotNull W addChild(@NotNull W child) {
        this.screen.addChild(child);
        this.tabObjects.add(child);
        return child;
    }
    @Override
    public final <W> void removeChild(@NotNull W widget) {
        this.screen.removeChild(widget);
        this.tabObjects.remove(widget);
    }
}
