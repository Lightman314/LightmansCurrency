package io.github.lightman314.lightmanscurrency.items;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.money.CoinData;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class CoinItem extends Item{

	public enum CoinItemTooltipType { DEFAULT, VALUE, NONE }
	
	public CoinItem(Properties properties)
	{
		super(properties);
	}
	
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flagIn)
	{
		super.appendHoverText(stack,  level,  tooltip,  flagIn);
		addCoinTooltips(stack, tooltip);
	}
	
	public static void addCoinTooltips(ItemStack stack, List<Component> tooltip)
	{
		CoinData coinData = MoneyUtil.getData(stack.getItem());
		if(coinData != null)
		{
			switch(Config.SERVER.coinTooltipType.get())
			{
			case DEFAULT:
				if(coinData.convertsDownwards())
				{
					tooltip.add(Component.translatable("tooltip.lightmanscurrency.coinworth.down", coinData.getDownwardConversion().getSecond(), MoneyUtil.getPluralName(coinData.getDownwardConversion().getFirst()).getString()).withStyle(ChatFormatting.YELLOW));
				}
				Pair<Item,Integer> upwardConversion = MoneyUtil.getUpwardConversion(stack.getItem());
				if(upwardConversion != null)
				{
					tooltip.add(Component.translatable("tooltip.lightmanscurrency.coinworth.up", upwardConversion.getSecond(), "§e" + upwardConversion.getFirst().getName(new ItemStack(upwardConversion.getFirst())).getString()).withStyle(ChatFormatting.YELLOW));
				}
				break;
			case VALUE:
				tooltip.add(Component.literal(Config.formatValueDisplay(coinData.getDisplayValue())).withStyle(ChatFormatting.YELLOW));
				break;
				default: //Default is NONE
			}
		}
	}
	
}
