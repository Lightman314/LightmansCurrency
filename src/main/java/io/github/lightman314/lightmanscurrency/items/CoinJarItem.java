package io.github.lightman314.lightmanscurrency.items;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class CoinJarItem extends BlockItem {
	
	public CoinJarItem(Block block, Properties properties)
	{
		super(block, properties);
	}
	
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
	{
		super.addInformation(stack,  worldIn,  tooltip,  flagIn);
		List<ItemStack> jarStorage = readJarData(stack);
		
		if(jarStorage.size() > 0)
		{
			if(Screen.hasShiftDown())
			{
				for(int i = 0; i < jarStorage.size(); i++)
				{
					ItemStack coin = jarStorage.get(i);
					if(coin.getCount() > 1)
						tooltip.add(new TranslationTextComponent("tooptip.lightmanscurrency.coinjar.storedcoins.multiple", coin.getCount(), coin.getDisplayName()));
					else
						tooltip.add(new TranslationTextComponent("tooptip.lightmanscurrency.coinjar.storedcoins.single", coin.getDisplayName()));
				}
			}
			else
			{
				tooltip.add(new TranslationTextComponent("tooptip.lightmanscurrency.coinjar.holdshift"));
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
						storage.add(ItemStack.read(thisItem));
					}
				}
			}
		}
		return storage;
	}

}
