package io.github.lightman314.lightmanscurrency.api.client;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipProvider;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TooltipUtil {

    public static <T extends TooltipProvider> void addToTooltip(ItemStack stack, Consumer<Component> adder, ItemTooltipEvent event, Supplier<DataComponentType<T>> type) { addToTooltip(stack,adder,event,type.get()); }
    public static <T extends TooltipProvider> void addToTooltip(ItemStack stack, Consumer<Component> adder, ItemTooltipEvent event, DataComponentType<T> type)
    {
        if(stack.has(type))
            stack.addToTooltip(type,event.getContext(),adder,event.getFlags());
    }

    public static Consumer<Component> createInjection(ItemTooltipEvent event) { return createInjection(event.getToolTip()); }
    public static Consumer<Component> createInjection(List<Component> tooltip)
    {
        if(tooltip.isEmpty())
            return tooltip::add;
        for(int i = 0; i < tooltip.size(); ++i)
        {
            Component line = tooltip.get(i);
            TextColor color = line.getStyle().getColor();
            if(color != null && color.getValue() == ChatFormatting.DARK_GRAY.getColor())
                return new TooltipInjector(tooltip,i);
        }
        return tooltip::add;
    }

    private static class TooltipInjector implements Consumer<Component>
    {
        private final List<Component> tooltips;
        private int injectIndex;
        TooltipInjector(List<Component> tooltips,int injectIndex) { this.tooltips = tooltips; this.injectIndex = injectIndex; }
        @Override
        public void accept(Component component) { this.tooltips.add(this.injectIndex++,component); }
    }

}
