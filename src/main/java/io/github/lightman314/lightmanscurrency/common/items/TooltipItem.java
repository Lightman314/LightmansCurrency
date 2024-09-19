package io.github.lightman314.lightmanscurrency.common.items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.common.text.MultiLineTextEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;


public class TooltipItem extends Item{
	
	private final Supplier<List<Component>> tooltips;
	
	public TooltipItem(Properties properties, Supplier<List<Component>> tooltips) { super(properties); this.tooltips = tooltips; }
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable Item.TooltipContext context, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag)
	{
		addTooltip(tooltip, this.tooltips);
		super.appendHoverText(stack, context, tooltip, flag);
	}
	
	public static void addTooltip(List<Component> tooltip, MultiLineTextEntry entry) { addTooltip(tooltip, entry.asTooltip()); }
	public static void addTooltip(List<Component> tooltip, Supplier<List<Component>> tooltipSource) {
		List<Component> addableTooltips = tooltipSource.get();
		if(addableTooltips.isEmpty())
			return;
		if(Screen.hasShiftDown())
			tooltip.addAll(tooltipSource.get());
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
