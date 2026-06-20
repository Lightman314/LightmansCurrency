package io.github.lightman314.lightmanscurrency.api.helpers;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class TooltipHelper {
    private TooltipHelper() {}

    public static <T extends TooltipProvider> void addToTooltip(List<Component> tooltip,DataComponentGetter item,Supplier<DataComponentType<T>> type,Item.TooltipContext context,TooltipFlag flag) {
        addToTooltip(tooltip,item,type.get(),context,flag);
    }
    public static <T extends TooltipProvider> void addToTooltip(List<Component> tooltip, DataComponentGetter item, DataComponentType<T> type,Item.TooltipContext context, TooltipFlag flag) {
        if(item.has(type) && item.getOrDefault(DataComponents.TOOLTIP_DISPLAY,TooltipDisplay.DEFAULT).shows(type))
            item.get(type).addToTooltip(context,endOfTooltipBuilder(tooltip),flag,item);
    }

    public static Consumer<Component> endOfTooltipBuilder(List<Component> tooltip) {
        if(tooltip.isEmpty())
            return tooltip::add;
        for(int i = 0; i < tooltip.size(); ++i)
        {
            TextColor color = tooltip.get(i).getStyle().getColor();
            if(color != null && color.getValue() == ChatFormatting.DARK_GRAY.getColor())
                return new TooltipInjector(tooltip,i);
        }
        return tooltip::add;
    }

    private static class TooltipInjector implements Consumer<Component>
    {
        private final List<Component> list;
        private int injectIndex;
        private TooltipInjector(List<Component> list,int injectIndex)
        {
            this.list = list;
            this.injectIndex = injectIndex;
        }
        @Override
        public void accept(Component component) { this.list.add(this.injectIndex++,component); }
    }

}