package io.github.lightman314.lightmanscurrency.common.util;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

public class TooltipHelper {

    public static final int DEFAULT_TOOLTIP_WIDTH = 256;

    public static List<Component> splitTooltips(@Nonnull List<Component> list, @Nonnull ChatFormatting... formatting) { return splitTooltips(list,DEFAULT_TOOLTIP_WIDTH,formatting); }
    public static List<Component> splitTooltips(@Nonnull List<Component> list, int lineWidth, @Nonnull ChatFormatting... formatting) {
        List<Component> result = new ArrayList<>();
        for(Component c : list)
            result.addAll(splitTooltips(c,lineWidth,formatting));
        return result;
    }
    public static List<Component> splitTooltips(@Nonnull Component component, @Nonnull ChatFormatting... formatting) { return splitTooltips(component, DEFAULT_TOOLTIP_WIDTH, formatting); }
    public static List<Component> splitTooltips(@Nonnull Component component, int lineWidth, @Nonnull ChatFormatting... formatting)
    {
        String s = component.getString();
        //Split words
        List<String> words = new ArrayList<>();
        BreakIterator iterator = BreakIterator.getLineInstance(Minecraft.getInstance().getLocale());
        iterator.setText(s);
        int start = iterator.first();
        for(int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next())
            words.add(s.substring(start,end));

        //Assemble lines
        List<String> lines = getLines(lineWidth, words);

        //Format
        List<Component> formattedLines = new ArrayList<>(lines.size());
        for(String string : lines)
            formattedLines.add(Component.literal(string).withStyle(formatting));
        return formattedLines;

    }

    /**
     * Returns a string formatted using {@link ChatFormatting#PREFIX_CODE} and the given formats {@link ChatFormatting#getChar()} code<br>
     * Automatically applies a format reset to the end as well so that it won't interfere with the base texts formatting
     */
    @Nonnull
    public static String lazyFormat(@Nonnull Component word, @Nonnull ChatFormatting... format) { return lazyFormat(word.getString(),format); }
    /**
     * Returns a string formatted using {@link ChatFormatting#PREFIX_CODE} and the given formats {@link ChatFormatting#getChar()} code<br>
     * Automatically applies a format reset to the end as well so that it won't interfere with the base texts formatting
     */
    @Nonnull
    public static String lazyFormat(@Nonnull String word, @Nonnull ChatFormatting... format) {
        StringBuilder builder = new StringBuilder();
        for(ChatFormatting f : format)
            builder.append(ChatFormatting.PREFIX_CODE).append(f.getChar());
        return builder.append(word).append(ChatFormatting.PREFIX_CODE).append(ChatFormatting.RESET.getChar()).toString();
    }

    @Nonnull
    private static List<String> getLines(int lineWidth, List<String> words) {
        Font font = Minecraft.getInstance().font;
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        int width = 0;
        for(String word : words)
        {
            int newWidth = font.width(hideFormatting(word));
            if(width + newWidth > lineWidth) {
                if(width > 0) {
                    String line = currentLine.toString();
                    lines.add(line);
                    currentLine = new StringBuilder();
                    width = 0;
                }
                else {
                    lines.add(word);
                    continue;
                }
            }
            currentLine.append(word);
            width += newWidth;
        }
        if(width > 0)
            lines.add(currentLine.toString());
        return lines;
    }

    @Nonnull
    private static String hideFormatting(@Nonnull String word)
    {
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

}
