package io.github.lightman314.lightmanscurrency.items;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;

public class CoinItem extends Item{

	public CoinItem(Properties properties)
	{
		super(properties);
	}
	
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
	{
		super.appendHoverText(stack,  worldIn,  tooltip,  flagIn);
		CoinData coinData = MoneyUtil.getData(this);
		if(coinData != null)
		{
			if(coinData.convertsDownwards())
			{
				tooltip.add(new TranslationTextComponent("tooltip.lightmanscurrency.coinworth.down", "§e" + coinData.getDownwardConversion().getSecond(), "§e" + coinData.getDownwardConversion().getFirst().getName().getString()));
			}
			Pair<Item,Integer> upwardConversion = MoneyUtil.getUpwardConversion(this);
			if(upwardConversion != null)
			{
				tooltip.add(new TranslationTextComponent("tooltip.lightmanscurrency.coinworth.up", "§e" + upwardConversion.getSecond(), "§e" + upwardConversion.getFirst().getName().getString()));
			}
		}
	}
	
}
