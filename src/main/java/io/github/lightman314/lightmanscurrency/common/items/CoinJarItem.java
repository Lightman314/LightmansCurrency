package io.github.lightman314.lightmanscurrency.common.items;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class CoinJarItem extends BlockItem {
	
	public CoinJarItem(Block block, Properties properties)
	{
		super(block, properties);
	}
	
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flagIn)
	{
		super.appendHoverText(stack,  level,  tooltip,  flagIn);
		List<ItemStack> jarStorage = readJarData(stack);
		
		if(jarStorage.size() > 0)
		{
			if(Screen.hasShiftDown())
			{
				for(int i = 0; i < jarStorage.size(); i++)
				{
					ItemStack coin = jarStorage.get(i);
					if(coin.getCount() > 1)
						tooltip.add(Component.translatable("tooptip.lightmanscurrency.coinjar.storedcoins.multiple", coin.getCount(), coin.getHoverName()));
					else
						tooltip.add(Component.translatable("tooptip.lightmanscurrency.coinjar.storedcoins.single", coin.getHoverName()));
				}
			}
			else
			{
				tooltip.add(Component.translatable("tooptip.lightmanscurrency.coinjar.holdshift").withStyle(ChatFormatting.YELLOW));
			}
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

}
