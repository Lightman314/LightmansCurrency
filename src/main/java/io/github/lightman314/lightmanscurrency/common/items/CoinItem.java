package io.github.lightman314.lightmanscurrency.common.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.money.CoinData;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class CoinItem extends Item {

	public enum CoinItemTooltipType { DEFAULT, VALUE, NONE }
	
	public CoinItem(Properties properties)
	{
		super(properties);
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable World level, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn)
	{
		super.appendHoverText(stack,  level,  tooltip,  flagIn);
		addCoinTooltips(stack, tooltip);
	}
	
	public static void addCoinTooltips(ItemStack stack, List<ITextComponent> tooltip)
	{
		CoinData coinData = MoneyUtil.getData(stack.getItem());
		if(coinData != null)
		{
			switch(Config.SERVER.coinTooltipType.get())
			{
			case DEFAULT:
				if(coinData.convertsDownwards())
				{
					tooltip.add(EasyText.translatable("tooltip.lightmanscurrency.coinworth.down", coinData.getDownwardConversion().getSecond(), MoneyUtil.getPluralName(coinData.getDownwardConversion().getFirst()).getString()).withStyle(TextFormatting.YELLOW));
				}
				Pair<Item,Integer> upwardConversion = MoneyUtil.getUpwardConversion(stack.getItem());
				if(upwardConversion != null)
				{
					tooltip.add(EasyText.translatable("tooltip.lightmanscurrency.coinworth.up", upwardConversion.getSecond(), upwardConversion.getFirst().getName(new ItemStack(upwardConversion.getFirst())).getString()).withStyle(TextFormatting.YELLOW));
				}
				break;
			case VALUE:
				double value = coinData.getDisplayValue();
				tooltip.add(EasyText.translatable("tooltip.lightmanscurrency.coinworth.value", Config.formatValueDisplay(value)).withStyle(TextFormatting.YELLOW));
				if(stack.getCount() > 1)
					tooltip.add(EasyText.translatable("tooltip.lightmanscurrency.coinworth.value.stack", Config.formatValueDisplay(value * stack.getCount())).withStyle(TextFormatting.YELLOW));
				break;
				default: //Default is NONE
			}
		}
	}
	
}
