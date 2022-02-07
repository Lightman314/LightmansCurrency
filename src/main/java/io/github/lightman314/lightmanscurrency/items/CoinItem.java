package io.github.lightman314.lightmanscurrency.items;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

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
			switch(Config.getCoinTooltipType())
			{
			case DEFAULT:
				if(coinData.convertsDownwards())
				{
					tooltip.add(new TranslatableComponent("tooltip.lightmanscurrency.coinworth.down", "§e" + coinData.getDownwardConversion().getSecond(), "§e" + coinData.getDownwardConversion().getFirst().getName(new ItemStack(coinData.getDownwardConversion().getFirst())).getString()));
				}
				Pair<Item,Integer> upwardConversion = MoneyUtil.getUpwardConversion(stack.getItem());
				if(upwardConversion != null)
				{
					tooltip.add(new TranslatableComponent("tooltip.lightmanscurrency.coinworth.up", "§e" + upwardConversion.getSecond(), "§e" + upwardConversion.getFirst().getName(new ItemStack(upwardConversion.getFirst())).getString()));
				}
				break;
			case VALUE:
				tooltip.add(new TextComponent("§e" + Config.formatValueDisplay(coinData.getDisplayValue())));
				break;
				default: //Default is NONE
			}
			
		}
	}
	
}
