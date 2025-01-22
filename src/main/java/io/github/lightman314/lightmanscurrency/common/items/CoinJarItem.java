package io.github.lightman314.lightmanscurrency.common.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.Block;

public class CoinJarItem extends BlockItem {
	
	public CoinJarItem(Block block, Properties properties) { super(block, properties); }

	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable TooltipContext level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, LCText.TOOLTIP_COIN_JAR);

		List<ItemStack> jarStorage = getJarContents(stack);

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
				tooltip.add(LCText.TOOLTIP_COIN_JAR_HOLD_SHIFT.get().withStyle(ChatFormatting.YELLOW));
		}

		if(InventoryUtil.ItemHasTag(stack, ItemTags.DYEABLE))
			tooltip.add(LCText.TOOLTIP_COIN_JAR_COLORED.getWithStyle(ChatFormatting.GRAY));

	}

	public boolean canDye(@Nonnull ItemStack stack) { return InventoryUtil.ItemHasTag(stack, ItemTags.DYEABLE); }

	/**
	 * Gets the contents of the Coin Jar<br>
	 * Note: List returned is immutable and cannot be edited as Coin Jars cannot be interacted with in item form<br>
	 * Returns an empty list of the given item stack is not for a Coin Jar item, even if it does contain the relevant data component.
	 */
	public static List<ItemStack> getJarContents(@Nonnull ItemStack stack)
	{
		if(!(stack.getItem() instanceof CoinJarItem))
			return ImmutableList.of();
		if(stack.has(ModDataComponents.COIN_JAR_CONTENTS))
			return stack.get(ModDataComponents.COIN_JAR_CONTENTS);
		return ImmutableList.of();
	}

	public static void setJarContents(@Nonnull ItemStack stack, @Nonnull List<ItemStack> jarContents)
	{
		if(!(stack.getItem() instanceof CoinJarItem))
			return;
		if(jarContents.isEmpty())
			stack.remove(ModDataComponents.COIN_JAR_CONTENTS);
		//Copy list & contents and then make them immutable just in case someone tries to edit the result of getJarContents later.
		stack.set(ModDataComponents.COIN_JAR_CONTENTS,ImmutableList.copyOf(InventoryUtil.copyList(jarContents)));
	}

	public static int getJarColor(@Nonnull ItemStack stack)
	{
		if(!(stack.getItem() instanceof CoinJarItem jar) || !jar.canDye(stack))
			return 0xFFFFFF;
		DyedItemColor c = stack.get(DataComponents.DYED_COLOR);
		return c == null ? 0xFFFFFF : c.rgb();
	}

	public static void setJarColor(@Nonnull ItemStack stack, int color)
	{
		if(!(stack.getItem() instanceof CoinJarItem jar) || !InventoryUtil.ItemHasTag(stack, ItemTags.DYEABLE))
			return;
		stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color,true));
	}

}
