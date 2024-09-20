package io.github.lightman314.lightmanscurrency.common.items;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.common.items.colored.ColoredItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class CoinJarItem extends BlockItem {
	
	public CoinJarItem(Block block, Properties properties) { super(block, properties); }
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, LCText.TOOLTIP_COIN_JAR);

		List<ItemStack> jarStorage = readJarData(stack);

		if(!jarStorage.isEmpty())
		{
			if(Screen.hasShiftDown())
			{
				for (ItemStack coin : jarStorage) {
					if (coin.getCount() > 1)
						tooltip.add(LCText.TOOLTIP_COIN_JAR_CONTENTS_MULTIPLE.get(coin.getCount(), coin.getHoverName()));
					else
						tooltip.add(LCText.TOOLTIP_COIN_JAR_CONTENTS_SINGLE.get(coin.getHoverName()));
				}
			}
			else
				tooltip.add(LCText.TOOLTIP_COIN_JAR_HOLD_SHIFT.getWithStyle(ChatFormatting.YELLOW));
		}

	}
	
	private static List<ItemStack> readJarData(ItemStack stack)
	{
		List<ItemStack> storage = new ArrayList<>();
		if(stack.hasTag())
		{
			CompoundTag compound = stack.getTag();
			if(compound.contains("JarData", Tag.TAG_COMPOUND))
			{
				CompoundTag jarData = compound.getCompound("JarData");
				if(jarData.contains("Coins"))
				{
					ListTag storageList = jarData.getList("Coins", Tag.TAG_COMPOUND);
					for(int i = 0; i < storageList.size(); i++)
					{
						CompoundTag thisItem = storageList.getCompound(i);
						storage.add(ItemStack.of(thisItem));
					}
				}
			}
		}
		return storage;
	}

	public static class Colored extends CoinJarItem implements ColoredItem
	{

		public Colored(Block block, Properties properties) { super(block, properties); }

		@Override
		public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn) {
			tooltip.add(LCText.TOOLTIP_COIN_JAR_COLORED.get());
			super.appendHoverText(stack, level, tooltip, flagIn);
		}
	}

}
