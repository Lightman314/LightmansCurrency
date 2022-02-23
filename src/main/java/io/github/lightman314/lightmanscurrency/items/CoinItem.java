package io.github.lightman314.lightmanscurrency.items;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinData;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class CoinItem extends Item{

	public enum CoinItemTooltipType { DEFAULT, VALUE, NONE }
	
	public CoinItem(Properties properties)
	{
		super(properties);
	}
	
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
	{
		super.addInformation(stack,  worldIn,  tooltip,  flagIn);
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
					tooltip.add(new TranslationTextComponent("tooltip.lightmanscurrency.coinworth.down", "§e" + coinData.getDownwardConversion().getSecond(), "§e" + coinData.getDownwardConversion().getFirst().getName().getString()));
				}
				Pair<Item,Integer> upwardConversion = MoneyUtil.getUpwardConversion(stack.getItem());
				if(upwardConversion != null)
				{
					tooltip.add(new TranslationTextComponent("tooltip.lightmanscurrency.coinworth.up", "§e" + upwardConversion.getSecond(), "§e" + upwardConversion.getFirst().getName().getString()));
				}
				break;
			case VALUE:
				tooltip.add(new StringTextComponent("§e" + Config.formatValueDisplay(coinData.getDisplayValue())));
				break;
				default:
			}
			
		}
	}
	
}
