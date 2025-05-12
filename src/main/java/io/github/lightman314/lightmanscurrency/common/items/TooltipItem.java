package io.github.lightman314.lightmanscurrency.common.items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.common.text.MultiLineTextEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class TooltipItem extends Item{
	
	private final Supplier<List<Component>> tooltips;
	
	public TooltipItem(Properties properties, Supplier<List<Component>> tooltips) { super(properties); this.tooltips = tooltips; }
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn)
	{
		addTooltip(tooltip, this.tooltips);
		super.appendHoverText(stack, level, tooltip, flagIn);
	}

	public static void insertTooltip(List<Component> tooltip, Component line) { insertTooltip(tooltip, Lists.newArrayList(line)); }
	public static void insertTooltip(List<Component> tooltip, List<Component> lines) {
		List<Component> temp = new ArrayList<>();
		for(int i = tooltip.size() - 1; i >= 0; --i)
		{
			Component line = tooltip.get(i);
			TextColor color = line.getStyle().getColor();
			if(color == null || color.getValue() != ChatFormatting.DARK_GRAY.getColor())
			{
				//Add to line after this line
				i++;
				for(int l = lines.size() - 1; l >= 0; --l)
					tooltip.add(i,lines.get(l));
				return;
			}
		}
		if(tooltip.isEmpty())
			tooltip.addAll(lines);
		else
		{
			for(int l = lines.size() - 1; l >= 0; --l)
				tooltip.add(1,lines.get(l));
		}
	}

	public static void insertTooltip(List<Component> tooltip, MultiLineTextEntry entry)
	{
		List<Component> temp = new ArrayList<>();
		addTooltip(temp,entry);
		insertTooltip(tooltip,temp);
	}

	public static void addTooltip(List<Component> tooltip, MultiLineTextEntry entry) { addTooltip(tooltip,entry.asTooltip()); }
	public static void addTooltip(List<Component> tooltip, Supplier<List<Component>> tooltipSource) {
		List<Component> addableTooltips = tooltipSource.get();
		if(addableTooltips.isEmpty())
			return;
		if(Screen.hasShiftDown())
			tooltip.addAll(addableTooltips);
		else
			tooltip.add(LCText.TOOLTIP_INFO_BLURB.get().withStyle(ChatFormatting.GRAY));
	}

	@SuppressWarnings("unchecked")
	public static Supplier<List<Component>> combine(Supplier<List<Component>>... tooltipSources) {
		return () -> {
			List<Component> result = new ArrayList<>();
			for(Supplier<List<Component>> source : tooltipSources)
				result.addAll(source.get());
			return result;
		};
	}
	
}
