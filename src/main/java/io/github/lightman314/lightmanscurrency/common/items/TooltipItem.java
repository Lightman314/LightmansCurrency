package io.github.lightman314.lightmanscurrency.common.items;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.NonNullSupplier;

public class TooltipItem extends Item{

	public static final Style DEFAULT_STYLE = Style.EMPTY.withColor(ChatFormatting.GRAY);
	
	private final NonNullSupplier<List<Component>> tooltips;
	
	public TooltipItem(Properties properties, NonNullSupplier<List<Component>> tooltips) { super(properties); this.tooltips = tooltips; }
	
	public static List<Component> getTooltipLines(String tooltipTranslation) { return getTooltipLines(tooltipTranslation, DEFAULT_STYLE); }
	public static List<Component> getTooltipLines(String tooltipTranslation, @Nullable Style format) {
		List<Component> result = new ArrayList<>();
		int i = 0;
		
		while(true)
		{
			TranslatableComponent nextLine = getTooltipLine(tooltipTranslation, ++i);
			if(nextLine == null)
				return result;
			if(format != null)
				nextLine.withStyle(format);
			result.add(nextLine);
		}
	}
	
	private static TranslatableComponent getTooltipLine(String tooltipTranslation, int page) {
		String tt = (tooltipTranslation.endsWith(".") ? tooltipTranslation : tooltipTranslation + ".") + page;
		TranslatableComponent result = new TranslatableComponent(tt);
		//Returns null if the translated text is the translation key.
		if(result.getString().contentEquals(tt))
			return null;
		return result;
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn)
	{
		addTooltip(tooltip, this.tooltips);
		super.appendHoverText(stack, level, tooltip, flagIn);
	}
	
	public static void addTooltip(List<Component> tooltip, NonNullSupplier<List<Component>> tooltipSource) {
		List<Component> addableTooltips = tooltipSource.get();
		if(addableTooltips.size() == 0)
			return;
		if(Screen.hasShiftDown())
			tooltip.addAll(tooltipSource.get());
		else
			tooltip.add(new TranslatableComponent("tooltip.lightmanscurrency.tooltip").withStyle(DEFAULT_STYLE));
	}
	
	public static void addTooltipAlways(List<Component> tooltip, NonNullSupplier<List<Component>> tooltipSource) {
		tooltip.addAll(tooltipSource.get());
	}
	
	@SuppressWarnings("unchecked")
	public static NonNullSupplier<List<Component>> combine(NonNullSupplier<List<Component>>... tooltipSources) {
		return () -> {
			List<Component> result = new ArrayList<>();
			for(NonNullSupplier<List<Component>> source : tooltipSources)
				result.addAll(source.get());
			return result;
		};
	}
	
}
