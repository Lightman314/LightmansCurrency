package io.github.lightman314.lightmanscurrency.client.gui.easy;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IEasyScreen;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.ITab;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.misc.IEasyTickable;
import net.minecraft.client.gui.Font;
import net.minecraft.core.RegistryAccess;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class EasyTab implements ITab, IEasyTickable, LazyPacketData.IBuilderProvider {

    private final IEasyScreen screen;
    public final Font getFont() { return this.screen.getFont(); }
    private final List<Object> children = new ArrayList<>();

    @Nonnull
    protected final RegistryAccess registryAccess() { return this.screen.registryAccess(); }
    @Nonnull
    public final LazyPacketData.Builder builder() { return this.screen.builder(); }

    protected EasyTab(IEasyScreen screen) { this.screen = screen; }

    public boolean blockInventoryClosing() { return false; }

    private boolean wasOpen = false;

    //Not final so that we can override for sub-tab purposes
    public <T> T addChild(T child) {
        if(!this.children.contains(child))
            this.children.add(child);
        this.screen.addChild(child);
        return child;
    }

    //Not final so that we can override for sub-tab purposes
    public void removeChild(Object child) {
        this.children.remove(child);
        this.screen.removeChild(child);
    }

    public final void onOpen()
    {
        this.children.clear();
        this.addChild(this);
        this.initialize(this.screen.getArea(), !this.wasOpen);
        this.wasOpen = true;
    }

    protected abstract void initialize(ScreenArea screenArea, boolean firstOpen);

    public abstract void renderBG(@Nonnull EasyGuiGraphics gui);
    public void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {}

    public final void onClose()
    {
        for(Object child : new ArrayList<>(this.children))
        {
            //Use screen version so that we don't modify our own list by accident
            this.screen.removeChild(child);
        }
        this.children.clear();
        this.wasOpen = false;
        this.closeAction();
    }

    protected void closeAction() { }

    public void tick() {}

}
