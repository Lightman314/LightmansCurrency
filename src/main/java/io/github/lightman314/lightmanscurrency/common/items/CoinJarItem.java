package io.github.lightman314.lightmanscurrency.common.items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class CoinJarItem extends BlockItem {
	
	public CoinJarItem(Block block, Properties properties)
	{
		super(block, properties);
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable World level, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn)
	{
		super.appendHoverText(stack,  level,  tooltip,  flagIn);
		List<ItemStack> jarStorage = readJarData(stack);
		
		if(jarStorage.size() > 0)
		{
			if(Screen.hasShiftDown())
			{
				for (ItemStack coin : jarStorage) {
					if (coin.getCount() > 1)
						tooltip.add(EasyText.translatable("tooptip.lightmanscurrency.coinjar.storedcoins.multiple", coin.getCount(), coin.getHoverName()));
					else
						tooltip.add(EasyText.translatable("tooptip.lightmanscurrency.coinjar.storedcoins.single", coin.getHoverName()));
				}
			}
			else
			{
				tooltip.add(EasyText.translatable("tooptip.lightmanscurrency.coinjar.holdshift").withStyle(TextFormatting.YELLOW));
			}
		}

	}
	
	private static List<ItemStack> readJarData(ItemStack stack)
	{
		List<ItemStack> storage = new ArrayList<>();
		if(stack.hasTag())
		{
			CompoundNBT compound = stack.getTag();
			if(compound.contains("JarData", Constants.NBT.TAG_COMPOUND))
			{
				CompoundNBT jarData = compound.getCompound("JarData");
				if(jarData.contains("Coins"))
				{
					ListNBT storageList = jarData.getList("Coins", Constants.NBT.TAG_COMPOUND);
					for(int i = 0; i < storageList.size(); i++)
					{
						CompoundNBT thisItem = storageList.getCompound(i);
						storage.add(ItemStack.of(thisItem));
					}
				}
			}
		}
		return storage;
	}

	@Override
	public Collection<ItemGroup> getCreativeTabs() {
		List<ItemGroup> result = new ArrayList<>(super.getCreativeTabs());
		result.add(ItemGroup.TAB_DECORATIONS);
		return result;
	}
}
