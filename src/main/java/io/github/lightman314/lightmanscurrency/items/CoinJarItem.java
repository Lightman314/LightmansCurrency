package io.github.lightman314.lightmanscurrency.items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class CoinJarItem extends BlockItem {
	
	public CoinJarItem(Block block, Properties properties)
	{
		super(block, properties);
	}
	
	@Override
	public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn)
	{
		super.appendHoverText(stack,  level,  tooltip,  flagIn);
		List<ItemStack> jarStorage = readJarData(stack);
		
		if(jarStorage.size() > 0)
		{
			if(Screen.hasShiftDown())
			{
				for (ItemStack coin : jarStorage) {
					if (coin.getCount() > 1)
						tooltip.add(new TranslatableComponent("tooptip.lightmanscurrency.coinjar.storedcoins.multiple", coin.getCount(), coin.getHoverName()));
					else
						tooltip.add(new TranslatableComponent("tooptip.lightmanscurrency.coinjar.storedcoins.single", coin.getHoverName()));
				}
			}
			else
			{
				tooltip.add(new TranslatableComponent("tooptip.lightmanscurrency.coinjar.holdshift").withStyle(ChatFormatting.YELLOW));
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

	@Override
	public Collection<CreativeModeTab> getCreativeTabs() {
		List<CreativeModeTab> result = new ArrayList<>(super.getCreativeTabs());
		result.add(CreativeModeTab.TAB_DECORATIONS);
		return result;
	}
}
