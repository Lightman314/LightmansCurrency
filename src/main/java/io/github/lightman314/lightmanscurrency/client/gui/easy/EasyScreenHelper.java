package io.github.lightman314.lightmanscurrency.client.gui.easy;

import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipSource;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.common.easy.IEasyTickable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class EasyScreenHelper {

    @Nullable
    @Deprecated
    public static IEasyTickable getWidgetTicker(@Nullable Object widget)
    {
        if(widget instanceof IEasyTickable t)
            return t;
        return null;
    }

    public static void RenderTooltips(EasyGuiGraphics gui, List<ITooltipSource> tooltipSources)
    {
        for(ITooltipSource tooltipSource : tooltipSources)
            tooltipSource.renderTooltip(gui);
    }

    public static List<Component> getTooltipFromItem(ItemStack item) { return Screen.getTooltipFromItem(Minecraft.getInstance(), item); }

}
