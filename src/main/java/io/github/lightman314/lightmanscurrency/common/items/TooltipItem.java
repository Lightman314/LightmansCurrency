package io.github.lightman314.lightmanscurrency.common.items;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.NonNullSupplier;

public class TooltipItem extends Item {

	public static final Style DEFAULT_STYLE = Style.EMPTY.withColor(TextFormatting.GRAY);
	
	private final NonNullSupplier<List<ITextComponent>> tooltips;
	
	public TooltipItem(Properties properties, NonNullSupplier<List<ITextComponent>> tooltips) { super(properties); this.tooltips = tooltips; }
	
	public static List<ITextComponent> getTooltipLines(String tooltipTranslation) { return getTooltipLines(tooltipTranslation, DEFAULT_STYLE); }
	public static List<ITextComponent> getTooltipLines(String tooltipTranslation, @Nullable Style format) {
		List<ITextComponent> result = new ArrayList<>();
		int i = 0;
		
		while(true)
		{
			TranslationTextComponent nextLine = getTooltipLine(tooltipTranslation, ++i);
			if(nextLine == null)
				return result;
			if(format != null)
				nextLine.withStyle(format);
			result.add(nextLine);
		}
	}
	
	private static TranslationTextComponent getTooltipLine(String tooltipTranslation, int page) {
		String tt = (tooltipTranslation.endsWith(".") ? tooltipTranslation : tooltipTranslation + ".") + (page);
		TranslationTextComponent result = new TranslationTextComponent(tt);
		//Returns null if the translated text is the translation key.
		if(result.getString().contentEquals(tt))
			return null;
		return result;
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable World level, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn)
	{
		addTooltip(tooltip, this.tooltips);
		super.appendHoverText(stack, level, tooltip, flagIn);
	}
	
	public static void addTooltip(List<ITextComponent> tooltip, NonNullSupplier<List<ITextComponent>> tooltipSource) {
		List<ITextComponent> addableTooltips = tooltipSource.get();
		if(addableTooltips.size() == 0)
			return;
		if(Screen.hasShiftDown())
			tooltip.addAll(tooltipSource.get());
		else
			tooltip.add(new TranslationTextComponent("tooltip.lightmanscurrency.tooltip").withStyle(DEFAULT_STYLE));
	}
	
	public static void addTooltipAlways(List<ITextComponent> tooltip, NonNullSupplier<List<ITextComponent>> tooltipSource) {
		tooltip.addAll(tooltipSource.get());
	}
	
	@SuppressWarnings("unchecked")
	public static NonNullSupplier<List<ITextComponent>> combine(NonNullSupplier<List<ITextComponent>>... tooltipSources) {
		return () -> {
			List<ITextComponent> result = new ArrayList<>();
			for(NonNullSupplier<List<ITextComponent>> source : tooltipSources)
				result.addAll(source.get());
			return result;
		};
	}
	
}
