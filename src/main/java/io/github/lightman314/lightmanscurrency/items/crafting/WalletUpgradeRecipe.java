package io.github.lightman314.lightmanscurrency.items.crafting;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.core.ModRecipes;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class WalletUpgradeRecipe extends SpecialRecipe{
	
	private static final List<UpgradeData> UPGRADE_DATA = Lists.newArrayList(
			new UpgradeData(ModItems.WALLET_COPPER, ModItems.WALLET_IRON, ModItems.COIN_IRON),
			new UpgradeData(ModItems.WALLET_IRON, ModItems.WALLET_GOLD, ModItems.COIN_GOLD, Items.REDSTONE),
			new UpgradeData(ModItems.WALLET_GOLD, ModItems.WALLET_EMERALD, ModItems.COIN_EMERALD, Items.ENDER_PEARL),
			new UpgradeData(ModItems.WALLET_EMERALD, ModItems.WALLET_DIAMOND, ModItems.COIN_DIAMOND),
			new UpgradeData(ModItems.WALLET_DIAMOND, ModItems.WALLET_NETHERITE, ModItems.COIN_NETHERITE));
	
	public WalletUpgradeRecipe(ResourceLocation idIn) {
		super(idIn);
	}
	
	@Override
	public boolean matches(CraftingInventory inv, World worldIn) {
		
		ItemStack wallet = null;
		List<ItemStack> upgradeItems = Lists.newArrayList();
		
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack thisItem = inv.getStackInSlot(i);
			if(!thisItem.isEmpty())
			{
				if(thisItem.getItem() instanceof WalletItem)
				{
					if(wallet == null)
						wallet = thisItem;
					else //More than one wallet
						return false;
				}
				else
					upgradeItems.add(thisItem);
			}
		}
		
		UpgradeData data = getUpgradeData(wallet, upgradeItems);
		return data != null;
		
	}
	
	@Override
	public ItemStack getCraftingResult(CraftingInventory inv) {

		ItemStack wallet = null;
		List<ItemStack> upgradeItems = Lists.newArrayList();
		
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack thisItem = inv.getStackInSlot(i);
			if(!thisItem.isEmpty())
			{
				if(thisItem.getItem() instanceof WalletItem)
				{
					if(wallet == null)
						wallet = thisItem;
					else //More than one wallet
						return ItemStack.EMPTY;
				}
				else
					upgradeItems.add(thisItem);
			}
		}
		
		UpgradeData data = getUpgradeData(wallet, upgradeItems);
		
		if(data != null)
		{
			//Get the output wallet
			ItemStack walletOut = new ItemStack(data.walletOut);
			WalletItem.CopyWalletContents(wallet, walletOut);
			return walletOut;
		}
		
		return ItemStack.EMPTY;
	}

	@Nullable
	private static UpgradeData getUpgradeData(ItemStack wallet, List<ItemStack> upgradeItems)
	{
		for(int i = 0; i < UPGRADE_DATA.size(); i++)
		{
			if(UPGRADE_DATA.get(i).matches(wallet, upgradeItems))
				return UPGRADE_DATA.get(i);
		}
		return null;
	}
	
	@Override
	public boolean canFit(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return ModRecipes.WALLET_UPGRADE;
	}
	
	public static void addUpgradeData(WalletItem walletIn, WalletItem walletOut, Item... upgradeItems)
	{
		UPGRADE_DATA.add(new UpgradeData(walletIn, walletOut, upgradeItems));
	}

	private static class UpgradeData
	{
		private final WalletItem walletIn;
		public final WalletItem walletOut;
		private final List<Item> upgradeItems;
		
		public UpgradeData(WalletItem walletIn, WalletItem walletOut, Item... upgradeItems)
		{
			this.walletIn = walletIn;
			this.walletOut = walletOut;
			this.upgradeItems = Lists.newArrayList(upgradeItems);
		}
		
		public boolean matches(ItemStack walletIn, List<ItemStack> upgradeItems)
		{
			if(walletIn == null || upgradeItems == null)
				return false;
			if(this.walletIn == walletIn.getItem())
			{
				if(this.upgradeItems.size() == upgradeItems.size())
				{
					for(int i = 0; i < this.upgradeItems.size(); i++)
					{
						Item thisItem = this.upgradeItems.get(i);
						boolean foundMatch = false;
						for(int z = 0; z < upgradeItems.size() && !foundMatch; z++)
						{
							if(upgradeItems.get(z).getItem() == thisItem)
							{
								upgradeItems.remove(z);
								foundMatch = true;
							}
						}
						if(!foundMatch)
							return false;
					}
					//Found a match for every upgrade item. Passed the check
					return true;
				}
			}
			return false;
		}
		
	}
	
	
}
