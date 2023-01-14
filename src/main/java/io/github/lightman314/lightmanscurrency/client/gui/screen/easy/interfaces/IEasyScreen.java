package io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces;

import io.github.lightman314.lightmanscurrency.common.easy.IEasyTickable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public interface IEasyScreen {

    //Screen Size access
    int width();
    int height();
    int guiLeft();
    int guiTop();
    //Player access
    default Player getPlayer() { return Minecraft.getInstance().player; }
    Font getFont();

    //Add Widget Functions
    <T extends Widget> @NotNull T addRenderableOnly(@NotNull T renderable);
    <T extends Widget & GuiEventListener> @NotNull T addRenderableWidget(@NotNull T widget);
    <T extends GuiEventListener> @NotNull T addGuiListener(@NotNull T listener);

    void addMouseListener(@NotNull IMouseListener listener);
    void addTooltipSource(@NotNull ITooltipSource tooltipSource);
    void addTicker(@NotNull Runnable ticker);

    //Remove Widget Functions
    void removeChild(@NotNull Object widget);

    @Nullable
    static Runnable CollectTicker(Object widget) {
        if(widget instanceof Runnable r)
            return r;
        if(widget instanceof IEasyTickable tickable)
            return tickable::tick;
        if(widget instanceof EditBox editBox)
            return editBox::tick;
        return null;
    }

}