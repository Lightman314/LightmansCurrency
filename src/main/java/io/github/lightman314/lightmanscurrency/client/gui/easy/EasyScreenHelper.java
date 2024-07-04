package io.github.lightman314.lightmanscurrency.client.gui.easy;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipSource;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.IEasyTickable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EasyScreenHelper {

    @Nullable
    @Deprecated
    public static IEasyTickable getWidgetTicker(@Nullable Object widget)
    {
        if(widget instanceof IEasyTickable t)
            return t;
        return null;
    }

    public static void RenderTooltips(@Nonnull EasyGuiGraphics gui, @Nonnull List<ITooltipSource> tooltipSources)
    {
        for(ITooltipSource tooltipSource : tooltipSources)
            tooltipSource.renderTooltip(gui);
    }

    public static List<Component> getTooltipFromItem(ItemStack item) { return item.isEmpty() ? new ArrayList<>() : Screen.getTooltipFromItem(Minecraft.getInstance(), item); }


    public static void RenderItemTooltip(@Nonnull EasyGuiGraphics gui, @Nonnull ItemStack item) { RenderItemTooltip(gui,item,getTooltipFromItem(item)); }

    public static void RenderItemTooltipWithModifiers(@Nonnull EasyGuiGraphics gui, @Nonnull ItemStack item, @Nonnull Consumer<List<Component>> tooltipEditor)
    {
        List<Component> text = getTooltipFromItem(item);
        tooltipEditor.accept(text);
        RenderItemTooltip(gui,item,text);
    }

    public static void RenderItemTooltipWithCount(@Nonnull EasyGuiGraphics gui, @Nonnull ItemStack item, int maxCount, ChatFormatting... formatting)
    {
        List<Component> text = getTooltipFromItem(item);
        text.add(formatItemCount(item.getCount(),maxCount,formatting));
        RenderItemTooltip(gui,item,text);
    }

    public static void RenderItemTooltip(@Nonnull EasyGuiGraphics gui, @Nonnull ItemStack item, @Nonnull List<Component> text) {
        //Copy mojang's item-based tooltip rendering
        gui.getGui().renderTooltip(gui.font, text, item.getTooltipImage(), item, gui.mousePos.x, gui.mousePos.y);
    }

    public static Component formatItemCount(int count, int maxCount, ChatFormatting... formatting)
    {
        DecimalFormat f = new DecimalFormat();
        MutableComponent c = EasyText.literal(f.format(count));
        if(count == maxCount)
            c.withStyle(ChatFormatting.GOLD);
        else if(count > maxCount)
            c.withStyle(ChatFormatting.DARK_RED);
        return LCText.TOOLTIP_ITEM_COUNT.get(c,f.format(maxCount)).withStyle(formatting);
    }

}
