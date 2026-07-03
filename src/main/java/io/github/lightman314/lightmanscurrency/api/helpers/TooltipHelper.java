package io.github.lightman314.lightmanscurrency.api.helpers;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class TooltipHelper {
    private TooltipHelper() {}

    public static final int DEFAULT_TOOLTIP_WIDTH = 256;

    public static List<Component> splitTooltips(List<Component> list,ChatFormatting... formatting) { return splitTooltips(list,DEFAULT_TOOLTIP_WIDTH,formatting); }
    public static List<Component> splitTooltips(List<Component> list,int lineWidth,ChatFormatting... formatting) {
        List<Component> result = new ArrayList<>();
        for(Component c : list)
            result.addAll(splitTooltips(c,lineWidth,formatting));
        return result;
    }

    public static List<Component> splitTooltips(Component component,ChatFormatting... formatting) { return splitTooltips(component,DEFAULT_TOOLTIP_WIDTH,formatting); }
    public static List<Component> splitTooltips(Component component,int lineWidth,ChatFormatting... formatting) {
        String s = component.getString();
        //Split words
        List<String> words = new ArrayList<>();
        BreakIterator iterator = BreakIterator.getLineInstance(Minecraft.getInstance().getLocale());
        iterator.setText(s);
        int start = iterator.first();
        for(int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next())
            words.add(s.substring(start,end));

        //Assemble lines
        List<String> lines = getLines(lineWidth,words);
        //Format
        List<Component> formattedLines = new ArrayList<>(lines.size());
        for(String string : lines)
            formattedLines.add(Component.literal(string).withStyle(formatting));
        return formattedLines;
    }

    private static List<String> getLines(int lineWidth,List<String> words) {
        Font font = Minecraft.getInstance().font;
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        int width = 0;
        for(String word : words)
        {
            int newWidth = font.width(hideFormatting(word));
            if(width + newWidth > lineWidth)
            {
                if(width > 0)
                {
                    String line = currentLine.toString();
                    lines.add(line);
                    currentLine = new StringBuilder();
                    width = 0;
                }
                else {
                    lines.add(word);
                    continue;
                }
                currentLine.append(word);
                width += newWidth;
            }
        }
        if(width > 0)
            lines.add(currentLine.toString());
        return lines;
    }

    private static String hideFormatting(String word) {
        int index = word.indexOf(ChatFormatting.PREFIX_CODE);
        while(index >= 0)
        {
            if(index == 0)
                word = word.substring(2);
            else if(index >= word.length() - 2)
                word = word.substring(0,index);
            else
                word = word.substring(0,index) + word.substring(index + 2);
            index = word.indexOf(ChatFormatting.PREFIX_CODE);
        }
        return word;
    }

    public static String lazyFormat(Component word,ChatFormatting... formatting) { return lazyFormat(word.getString(),formatting); }
    public static String lazyFormat(String word,ChatFormatting... formatting) {
        StringBuilder builder = new StringBuilder();
        for(ChatFormatting f : formatting)
            builder.append(ChatFormatting.PREFIX_CODE).append(f.getChar());
        return builder.append(word).append(ChatFormatting.PREFIX_CODE).append(ChatFormatting.RESET.getChar()).toString();
    }

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