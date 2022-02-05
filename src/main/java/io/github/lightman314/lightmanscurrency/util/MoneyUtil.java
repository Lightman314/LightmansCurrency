package io.github.lightman314.lightmanscurrency.util;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue.CoinValuePair;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;

public class MoneyUtil {
	
	
	
	//Coin data list
	private static List<CoinData> coinList = new ArrayList<>();
	private static boolean coinListDirty = false;
	private static List<CoinData> publicCoinList = new ArrayList<>();
	//Whether the mod has been initialized
	private static boolean init = false;
	public static boolean initialized() { return init; }
	
	/**
     * Initializes the default master coin list from order of highest to lowest values.
     */
    public static void init()
    {
    	
    	LightmansCurrency.LogInfo("Initializing the Money Utilities.");
    	
    	if(init)
    		return;
    	
    	//Copper Coin
    	addCoinItem(CoinData.getBuilder(ModItems.COIN_COPPER)
    			.defineInitial("item.lightmanscurrency.coin_copper.initial"));
    			//.defineMintingMaterialTag(new ResourceLocation("forge","ingots/copper")));
    	//Iron Coin
    	addCoinItem(CoinData.getBuilder(ModItems.COIN_IRON)
    			.defineInitial("item.lightmanscurrency.coin_iron.initial")
    			//.defineMintingMaterial(Items.IRON_INGOT)
    			.defineConversion(ModItems.COIN_COPPER, Config.COMMON.ironCoinWorth.get()));
    	//Gold Coin
    	addCoinItem(CoinData.getBuilder(ModItems.COIN_GOLD)
    			.defineInitial("item.lightmanscurrency.coin_gold.initial")
    			//.defineMintingMaterial(Items.GOLD_INGOT)
    			.defineConversion(ModItems.COIN_IRON, Config.COMMON.goldCoinWorth.get()));
    	//Emerald Coin
    	addCoinItem(CoinData.getBuilder(ModItems.COIN_EMERALD)
    			.defineInitial("item.lightmanscurrency.coin_emerald.initial")
    			//.defineMintingMaterial(Items.EMERALD)
    			.defineConversion(ModItems.COIN_GOLD, Config.COMMON.emeraldCoinWorth.get()));
    	//Diamond Coin
    	addCoinItem(CoinData.getBuilder(ModItems.COIN_DIAMOND)
    			.defineInitial("item.lightmanscurrency.coin_diamond.initial")
    			//.defineMintingMaterial(Items.DIAMOND)
    			.defineConversion(ModItems.COIN_EMERALD, Config.COMMON.diamondCoinWorth.get()));
    	//Netherite Coin
    	addCoinItem(CoinData.getBuilder(ModItems.COIN_NETHERITE)
    			.defineInitial("item.lightmanscurrency.coin_netherite.initial")
    			//.defineMintingMaterial(Items.NETHERITE_INGOT)
    			.defineConversion(ModItems.COIN_DIAMOND, Config.COMMON.netheriteCoinWorth.get()));
    	
    	//Hidden coins
    	//Copper Coinpile
    	addCoinItem(CoinData.getBuilder(ModBlocks.COINPILE_COPPER.item)
    			.defineConversion(ModItems.COIN_COPPER, Config.COMMON.coinpileCopperWorth.get())
    			.setHidden());
    	//Copper Coin Block
    	addCoinItem(CoinData.getBuilder(ModBlocks.COINBLOCK_COPPER.item)
    			.defineConversion(ModBlocks.COINPILE_COPPER.item, Config.COMMON.coinBlockCopperWorth.get())
    			.setHidden());
    	
    	//Iron Coinpile
    	addCoinItem(CoinData.getBuilder(ModBlocks.COINPILE_IRON.item)
    			.defineConversion(ModItems.COIN_IRON, Config.COMMON.coinpileIronWorth.get())
    			.setHidden());
    	//Iron Coin Block
    	addCoinItem(CoinData.getBuilder(ModBlocks.COINBLOCK_IRON.item)
    			.defineConversion(ModBlocks.COINPILE_IRON.item, Config.COMMON.coinBlockIronWorth.get())
    			.setHidden());
    	
    	//Gold Coinpile
    	addCoinItem(CoinData.getBuilder(ModBlocks.COINPILE_GOLD.item)
    			.defineConversion(ModItems.COIN_GOLD, Config.COMMON.coinpileGoldWorth.get())
    			.setHidden());
    	//Gold Coin Block
    	addCoinItem(CoinData.getBuilder(ModBlocks.COINBLOCK_GOLD.item)
    			.defineConversion(ModBlocks.COINPILE_GOLD.item, Config.COMMON.coinBlockGoldWorth.get())
    			.setHidden());
    	
    	//Emerald Coinpile
    	addCoinItem(CoinData.getBuilder(ModBlocks.COINPILE_EMERALD.item)
    			.defineConversion(ModItems.COIN_EMERALD, Config.COMMON.coinpileEmeraldWorth.get())
    			.setHidden());
    	//Emerald Coin Block
    	addCoinItem(CoinData.getBuilder(ModBlocks.COINBLOCK_EMERALD.item)
    			.defineConversion(ModBlocks.COINPILE_EMERALD.item, Config.COMMON.coinBlockEmeraldWorth.get())
    			.setHidden());
    	
    	//Diamond Coinpile
    	addCoinItem(CoinData.getBuilder(ModBlocks.COINPILE_DIAMOND.item)
    			.defineConversion(ModItems.COIN_DIAMOND, Config.COMMON.coinpileDiamondWorth.get())
    			.setHidden());
    	//Diamond Coin Block
    	addCoinItem(CoinData.getBuilder(ModBlocks.COINBLOCK_DIAMOND.item)
    			.defineConversion(ModBlocks.COINPILE_DIAMOND.item, Config.COMMON.coinBlockDiamondWorth.get())
    			.setHidden());
    	
    	//Netherite Coinpile
    	addCoinItem(CoinData.getBuilder(ModBlocks.COINPILE_NETHERITE.item)
    			.defineConversion(ModItems.COIN_NETHERITE, Config.COMMON.coinpileNetheriteWorth.get())
    			.setHidden());
    	//Netherite Coin Block
    	addCoinItem(CoinData.getBuilder(ModBlocks.COINBLOCK_NETHERITE.item)
    			.defineConversion(ModBlocks.COINPILE_NETHERITE.item, Config.COMMON.coinBlockNetheriteWorth.get())
    			.setHidden());
    	
    	init = true;
    	sortCoinList();
		
    }
    
    private static void addCoinItem(CoinData.Builder newCoinDataBuilder)
    {
    	addCoinItem(newCoinDataBuilder, false);
    }
    
    /**
     * Adds a coin to the official coin list, thus allowing it to be acknowledged as a coin
     */
    public static void AddCoinItem(CoinData.Builder newCoinDataBuilder)
    {
    	addCoinItem(newCoinDataBuilder, false);
    }
    
    private static void addCoinItem(CoinData.Builder newCoinDataBuilder, boolean ignoreDuplicateWarning)
    {
    	if(coinList == null)
    		coinList = new ArrayList<>();
    	CoinData newCoinData = newCoinDataBuilder.build();
    	CoinData duplicateDependentOwner = null;
    	for(CoinData coinData : coinList)
    	{
    		//Confirm that there's no duplicate coin
    		if(coinData.getCoinItem() == newCoinData.getCoinItem())
    		{
    			LightmansCurrency.LogWarning("Attempted to add Duplicate Coin Item (" + newCoinData.getCoinItem().getRegistryName().toString() + ") to the master coin list.");
    			//LightmansCurrency.LOGGER.warn("Please use MoneyUtil.changeCoinValue if you wish to change a coins value.");
    			return;
    		}
    		//Confirm that there's no duplicate dependent (Ignore this if either party is hidden, as conversion will be ignored for that coin)
    		if(coinData.worthOtherCoin == newCoinData.worthOtherCoin && newCoinData.worthOtherCoin != null && !newCoinData.isHidden && !coinData.isHidden)
    		{
    			duplicateDependentOwner = coinData;
    			if(!ignoreDuplicateWarning)
    			{
    				LightmansCurrency.LogWarning("Attempted to add a new Coin Item '" + newCoinData.getCoinItem().getRegistryName() + "' with the same dependent (" + duplicateDependentOwner.worthOtherCoin.getRegistryName() + ") as another coin (" + coinData.coinItem.getRegistryName() + ").\nManually splicing the dependents to keep the chain clean.");
        			LightmansCurrency.LogWarning("To avoid this problem, run MoneyUtil.changeCoinConversion(Item changedCoin, Item otherCoin, int otherCoinCount) before adding the new coin type.");
    			}
    		}
    	}
    	if(duplicateDependentOwner != null)
    	{
    		//Change the most valuable of the two coins to use the same 
    		if(duplicateDependentOwner.worthOtherCoinCount > newCoinData.worthOtherCoinCount)
    		{
    			duplicateDependentOwner.worthOtherCoin = newCoinData.coinItem;
    			//Check if it comes out to a nice round number
    			float ratio = (float)duplicateDependentOwner.worthOtherCoinCount / (float)newCoinData.worthOtherCoinCount;
    			if(ratio % 1f == 0f)
    			{
    				//Ratio is exact, multiply the larger one by this number and all the values will be handled properly.
    				duplicateDependentOwner.worthOtherCoinCount = (int)ratio;
    				if(!ignoreDuplicateWarning)
    				{
    					LightmansCurrency.LogInfo("Duplicate dependent fits in, so changing the other coins dependency to this coin in a manner such that its total value remains the same.");
    					LightmansCurrency.LogInfo("You may safely register this coin while flagging it to ignore warnings if you're getting tired of seeing this message.");
    				}
    			}
    		}
    		else
    		{
    			newCoinData.worthOtherCoin = duplicateDependentOwner.coinItem;
    			//Check if it comes out to a nice round number
    			float ratio = (float)newCoinData.worthOtherCoinCount / (float)duplicateDependentOwner.worthOtherCoinCount;
    			if(ratio % 1f == 0f)
    			{
    				//Ratio is exact, multiply the larger one by this number and all the values will be handled properly.
    				newCoinData.worthOtherCoinCount = (int)ratio;
    				if(!ignoreDuplicateWarning)
    				{
    					LightmansCurrency.LogInfo("Duplicate dependent fits in, so changing this coins dependency to the other coin in a manner such that its total value remains the same.");
    					LightmansCurrency.LogInfo("You may safely register this coin while flagging it to ignore warnings if you're getting tired of seeing this message.");
    				}
    			}
    		}
    	}
    	LightmansCurrency.LogDebug("Adding " + newCoinData.getCoinItem().getRegistryName());
    	coinList.add(newCoinData);
    	coinListDirty = coinListDirty || !newCoinData.isHidden;
    	
    }
    
    /**
     * Adds a coin to the official coin list, thus allowing it to be acknowledged as a coin
     * @param ignoreDuplicateWarning Allows you to ignore the duplicate dependent warning should you intend to use the default method of compensating for this problem.
     */
    public static void AddCoinItem(CoinData.Builder newCoinDataBuilder, boolean ignoreDuplicateWarning)
    {
    	addCoinItem(newCoinDataBuilder, ignoreDuplicateWarning);
    	sortCoinList();
    }
    
    /**
     * Changes the defined value of an already registered coin.
     * @param coinItem The already registered coin item.
     * @param otherCoin The new coin it will define as its dependent.
     * @param otherCoinCount The amount of the other coin that the conversion will be defined as.
     */
    public static void changeCoinConversion(Item changedCoin, Item otherCoin, int otherCoinCount)
    {
    	CoinData changedData = getData(changedCoin);
    	if(changedData == null)
    	{
    		if(initialized()) //Only throw the coin conversion error if the coins have been initialized.
    			LightmansCurrency.LogError("Cannot change the coin conversion as '" + changedCoin.getRegistryName() + "' has not been registered as a coin.");
    		return;
    	}
    	//Confirm that nothing else uses the new dependent (unless this is a hidden coin)
    	if(!changedData.isHidden)
    	{
	    	for(CoinData coinData : coinList)
	    	{
	    		if(coinData.worthOtherCoin == otherCoin && coinData.coinItem != changedCoin && !coinData.isHidden)
	    		{
	    			LightmansCurrency.LogError("Cannot change the coin's dependent to a dependent (" + otherCoin.getRegistryName() + ") that is also being used by another coin (" + coinData.coinItem.getRegistryName() + ").");
	    			return;
	    		}
	    	}
    	}
    	if(changedData.overrideConversion(otherCoin, otherCoinCount));
			sortCoinList();
    }
    
    private static void sortCoinList()
    {
    	List<CoinData> newList = new ArrayList<>();
    	while(coinList.size() > 0)
    	{
    		int highestValueIndex = 0;
    		long highestValue = coinList.get(0).getValue();
    		for(int i = 1; i < coinList.size(); i++)
    		{
    			if(coinList.get(i).getValue() > highestValue)
    			{
    				highestValueIndex = i;
    				highestValue = coinList.get(i).getValue();
    			}
    		}
    		newList.add(coinList.get(highestValueIndex));
    		coinList.remove(highestValueIndex);
    	}
    	coinList = newList;
    }
    
    /**
     * Checks if the given item is in the master coin list.
     * @param item The item to check.
     */
    public static boolean isCoin(Item item)
    {
    	return isCoin(item, true);
    }
    
    /**
     * Checks if the given item is in the master coin list.
     * @param item The item to check.
     * @param allowHidden Whether hidden coins should return true.
     */
    public static boolean isCoin(Item item, boolean allowHidden)
    {
    	if(item == null)
    		return false;
    	for(CoinData coinData : coinList)
    	{
    		if(coinData.getCoinItem().equals(item))
    		{
				return allowHidden || !coinData.isHidden;
    		}
    	}
    	return false;
    }
    
    /**
     * Checks if the given item is both a coin & that the coin is considered hidden.
     * Used to blacklist "hidden" coins from certain slots (such as the ATM slots) while still letting them be considered coins in the default isCoin check
     */
    public static boolean isCoinHidden(Item item)
    {
    	if(item == null)
    		return false;
    	for(CoinData coinData : coinList)
    	{
    		if(coinData.coinItem == item)
    			return coinData.isHidden;
    	}
    	return false;
    }
    
    /**
     * Checks if the given item is in the master coin list.
     * @param stack The ItemStack to check.
     */
    public static boolean isCoin(@Nonnull ItemStack stack)
    {
    	return isCoin(stack, true);
    }
	
    /**
     * Checks if the given item is in the master coin list.
     * @param stack The ItemStack to check.
     * @param allowHidden Whether hidden coins should return true.
     */
    public static boolean isCoin(@Nonnull ItemStack stack, boolean allowHidden)
    {
    	return isCoin(stack.getItem(), allowHidden);
    }
    
    /**
     * Gets the value of the given item.
     * @param coinItem The coin to get the value of.
     */
    public static long getValue(Item coinItem)
    {
    	CoinData coinData = getData(coinItem);
    	if(coinData != null)
    		return coinData.getValue();
    	return 0;
    }
    
    /**
     * Gets the total value of the item stack.
     * @param coinStack The item stack to get the value of.
     */
    public static long getValue(ItemStack coinStack)
    {
    	return getValue(coinStack.getItem()) * coinStack.getCount();
    }
    
    /**
     * Gets the total value of the item stack.
     * @param coinStack The item stack to get the value of.
     */
    public static CoinValue getCoinValue(ItemStack coinStack) { return new CoinValue(getValue(coinStack)); }
    
    /**
	 * Gets the total value of the items in the given ItemStack list.
	 * @param inventory The list full of coins from which to get the value of.
	 */
    public static long getValue(NonNullList<ItemStack> inventory)
	{
    	long value = 0;
		for(int i = 0; i < inventory.size(); i++)
		{
			value += getValue(inventory.get(i));
		}
		return value;
	}
    
    /**
	 * Gets the total value of the items in the given ItemStack list.
	 * @param inventory The list full of coins from which to get the value of.
	 */
    public static CoinValue getCoinValue(NonNullList<ItemStack> inventory) { return new CoinValue(getValue(inventory)); }
    
    /**
	 * Gets the total value of the items in the given inventory.
	 * @param inventory The inventory full of coins with which to get the value of.
	 */
    public static long getValue(IInventory inventory)
	{
    	long value = 0;
		for(int i = 0; i < inventory.getSizeInventory(); i++)
		{
			value += getValue(inventory.getStackInSlot(i));
		}
		return value;
	}
    
    /**
	 * Gets the total value of the items in the given inventory.
	 * @param inventory The inventory full of coins with which to get the value of.
	 */
    public static CoinValue getCoinValue(IInventory inventory) { return new CoinValue(getValue(inventory)); }
    
    /**
     * Converts all coins in the inventory to as large a coin as humanly possible
     */
    public static void ConvertAllCoinsUp(IInventory inventory)
    {
    	for(int i = 1; i < coinList.size(); i++)
    	{
    		ConvertCoinsUp(inventory, coinList.get(i).getCoinItem());
    	}
    	for(int i = coinList.size() - 1; i > 0; i--)
    	{
    		ConvertCoinsUp(inventory, coinList.get(i).getCoinItem());
    	}
    }
    
    /**
     * Converts all coins in the inventory to as large a coin as humanly possible
     */
    public static NonNullList<ItemStack> ConvertAllCoinsUp(NonNullList<ItemStack> inventoryList)
    {
    	IInventory inventory = InventoryUtil.buildInventory(inventoryList);
    	ConvertAllCoinsUp(inventory);
    	return InventoryUtil.buildList(inventory);
    }
    
    /**
     * Converts as many of the small coin that it can into its next largest coin
     */
    public static void ConvertCoinsUp(IInventory inventory, Item smallCoin)
    {
    	//Get next-higher coin data
    	Pair<Item,Integer> upwardConversion = getUpwardConversion(smallCoin);
    	if(upwardConversion == null)
    		return;
    	Item largeCoin = upwardConversion.getFirst();
    	int smallCoinCount = upwardConversion.getSecond();
    	
    	if(!isCoin(largeCoin))
    		return;
    	while(InventoryUtil.GetItemCount(inventory, smallCoin) >= smallCoinCount)
		{
			//Remove the smaller coins
    		InventoryUtil.RemoveItemCount(inventory, smallCoin, smallCoinCount);
			//Put the new coin into the inventory
			ItemStack newCoinStack = new ItemStack(largeCoin, 1);
			if(!InventoryUtil.PutItemStack(inventory, newCoinStack))
			{
				//Could not merge the inventory. Re-add the smaller coins & break the loop;
				InventoryUtil.TryPutItemStack(inventory, new ItemStack(smallCoin, smallCoinCount));
				return;
			}
		}
    }
    
    /**
     * Converts all coins in the inventory to as small a coin as humanly possible
     */
    public static void ConvertAllCoinsDown(IInventory inventory)
    {
    	ConvertAllCoinsDown(inventory, 2);
    }
    
    /**
     * Converts all coins in the inventory to as small a coin as humanly possible
     * @param iterations The number of times to repeatedly convert to ensure that the available space is used. Default is 2.
     */
    private static void ConvertAllCoinsDown(IInventory inventory, int iterations)
    {
    	for(int x = 0; x < iterations; x++)
    	{
    		for(int i = 0; i < (coinList.size() - 1); i++)
    		{
    			if(!coinList.get(i).isHidden)
    				ConvertCoinsDown(inventory, coinList.get(i).getCoinItem());
    		}
    	}
    }
    
    /**
     * Converts as many of the large coin that it can into its defined smaller coin
     */
    public static void ConvertCoinsDown(IInventory inventory, Item largeCoin)
    {
    	CoinData coinData = getData(largeCoin);
    	Item smallCoin = coinData.worthOtherCoin;
    	int smallCoinCount = coinData.worthOtherCoinCount;
    	if(!isCoin(smallCoin))
    		return;
    	while(InventoryUtil.GetItemCount(inventory, largeCoin) > 0)
		{
			//Remove the large coin
    		InventoryUtil.RemoveItemCount(inventory, largeCoin, 1);
    		//Merge the new coins into the container
			ItemStack newCoinStack = new ItemStack(smallCoin, smallCoinCount);
			if(!InventoryUtil.PutItemStack(inventory, newCoinStack))
			{
				//Could not merge the inventory. Re-add the large coin & break the loop;
				InventoryUtil.TryPutItemStack(inventory, new ItemStack(largeCoin, 1));
				return;
			}
		}
    }
    
    public static void SortCoins(IInventory inventory)
	{
		
		InventoryUtil.MergeStacks(inventory);
		
		List<ItemStack> oldInventory = new ArrayList<>();
		for(int i = 0; i < inventory.getSizeInventory(); i++)
		{
			if(!inventory.getStackInSlot(i).isEmpty())
				oldInventory.add(inventory.getStackInSlot(i));
		}
		
		inventory.clear();
		
		int index = 0;
		while(oldInventory.size() > 0)
		{
			int highestIndex = 0;
			long highestIndividualValue = MoneyUtil.getValue(oldInventory.get(0).getItem());
			long highestWholeValue = highestIndividualValue * oldInventory.get(0).getCount();
			//LightmansCurrency.LOGGER.info("Starting sort values. HI: " + highestIndividualValue + " HW: " + highestWholeValue);
			
			for(int i = 1; i < oldInventory.size(); i++)
			{
				ItemStack stack = oldInventory.get(i);
				long thisIndividualValue = MoneyUtil.getValue(stack.getItem());
				long thisWholeValue = thisIndividualValue * stack.getCount();
				//Value is higher 
				if(thisIndividualValue > highestIndividualValue)
				{
					//LightmansCurrency.LOGGER.info("Larger Individual Value at index " + i + ": " + thisIndividualValue + " > " + highestIndividualValue + "Whole Values: " + thisWholeValue + " & " + highestWholeValue);
					highestIndex = i;
					highestIndividualValue = thisIndividualValue;
					highestWholeValue = thisWholeValue;
				}
				else if(thisIndividualValue == highestIndividualValue && thisWholeValue > highestWholeValue)
				{
					//LightmansCurrency.LOGGER.info("Same Individual Value but larger whole at index " + i + ": " + thisWholeValue + " > " + highestWholeValue + "Individual Values: " + thisIndividualValue + " = " + highestIndividualValue );
					highestIndex = i;
					highestWholeValue = thisWholeValue;
				}
			}
			
			inventory.setInventorySlotContents(index, oldInventory.get(highestIndex));
			index++;
			oldInventory.remove(highestIndex);
		}
		
	}
    
    public static NonNullList<ItemStack> SortCoins(NonNullList<ItemStack> inventory)
    {
    	IInventory tempInventory = InventoryUtil.buildInventory(inventory);
    	SortCoins(tempInventory);
    	return InventoryUtil.buildList(tempInventory);
    }
    
    /**
     * Process a payment from the given coin slot inventory & player.
     * @param inventory An inventory that may or may not contain coins that payment can be taken from. Can be null, but then payment will only be taken from the players wallet.
     * @param player The player making the payment. Required for item overflow, and wallet aquisition.
     * @param price The price of the payment that we are attempting to process.
     * @return Whether the payment went through. If false is returned, no money was taken from the wallet nor the inventory.
     */
    public static boolean ProcessPayment(@Nullable IInventory inventory, @Nonnull PlayerEntity player, @Nonnull CoinValue price)
    {
    	return ProcessPayment(inventory, player, price, false);
    }
    
    /**
     * Process a payment from the given coin slot inventory & player.
     * @param inventory An inventory that may or may not contain coins that payment can be taken from. Can be null, but then payment will only be taken from the players wallet.
     * @param player The player making the payment. Required for item overflow, and wallet aquisition.
     * @param price The price of the payment that we are attempting to process.
     * @param ignoreWallet Whether we should ignore the players wallet in terms of taking payment or giving change.
     * @return Whether the payment went through. If false is returned, no money was taken from the wallet nor the inventory.
     */
    public static boolean ProcessPayment(@Nullable IInventory inventory, @Nonnull PlayerEntity player, @Nonnull CoinValue price, boolean ignoreWallet)
    {
    	//Get the players wallet
    	ItemStack wallet = ignoreWallet ? ItemStack.EMPTY : LightmansCurrency.getWalletStack(player);
    	
    	long valueToTake = price.getRawValue();
    	//Get value from the wallet
    	long rawInventoryValue = 0;
    	if(inventory != null)
    		rawInventoryValue += getValue(inventory);
    	if(!wallet.isEmpty())
    		rawInventoryValue += getValue(WalletItem.getWalletInventory(wallet));
    	if(rawInventoryValue < valueToTake)
    		return false;
    	
    	//Otherwise take the payment
    	//Take from the inventory first
    	if(inventory != null)
    		valueToTake = takeObjectsOfValue(valueToTake, inventory, true);
    	//Then take from the wallet
    	if(valueToTake > 0 && !wallet.isEmpty())
    	{
    		NonNullList<ItemStack> walletInventory = WalletItem.getWalletInventory(wallet);
    		valueToTake = takeObjectsOfValue(valueToTake, walletInventory);
    		WalletItem.putWalletInventory(wallet, walletInventory);
    	}
    	
    	//Give change if necessary
    	if(valueToTake < 0)
    	{
    		List<ItemStack> change = getCoinsOfValue(Math.abs(valueToTake));
    		for(ItemStack coinStack : change)
    		{
    			//Put them in the wallet first
    			if(!wallet.isEmpty())
    			{
    				coinStack = WalletItem.PickupCoin(wallet, coinStack);
    			}
    			if(!coinStack.isEmpty() && inventory != null)
    			{
    				//TryPutItemStack allows partial placement unlike PutItemStack which is used for cancelable placements
    				coinStack = InventoryUtil.TryPutItemStack(inventory, coinStack);
    			}
    			//Out of room to place it, throw it at the player
    			if(!coinStack.isEmpty())
    			{
    				if(!player.addItemStackToInventory(coinStack))
    				{
    					//Spawn an item in the world at the player position
    					ItemEntity itemEntity = new ItemEntity(player.world, player.getPosX(), player.getPosY(), player.getPosZ(), coinStack);
    					itemEntity.setPickupDelay(40);
    					player.world.addEntity(itemEntity);
    				}
    			}
    		}
    	}
    	
    	return true;
    }
    
    /**
     * Put money into the coin slot inventory & player.
     * @param inventory An inventory in which to give the coins to by default should the player not have an equipped wallet. Can be null, but then payment will only be given to the players wallet/inventory.
     * @param player The player receiving the money. Required for item overflow, and wallet aquisition.
     * @param price The amount of money we're attempting to give.
     */
    public static void ProcessChange(@Nullable IInventory inventory, @Nonnull PlayerEntity player, @Nonnull CoinValue change)
    {
    	ProcessChange(inventory, player, change, false);
    }
    
    public static void ProcessChange(@Nullable IInventory inventory, @Nonnull PlayerEntity player, @Nonnull CoinValue change, boolean ignoreWallet)
    {
    	//Get the players wallet
    	ItemStack wallet = ignoreWallet ? ItemStack.EMPTY : LightmansCurrency.getWalletStack(player);
    	
    	List<ItemStack> changeCoins = getCoinsOfValue(change);
    	for(ItemStack coinStack : changeCoins)
		{
			//Put them in the wallet first
			if(!wallet.isEmpty())
			{
				coinStack = WalletItem.PickupCoin(wallet, coinStack);
			}
			if(!coinStack.isEmpty() && inventory != null)
			{
				//TryPutItemStack allows partial placement unlike PutItemStack which is used for cancelable placements
				coinStack = InventoryUtil.TryPutItemStack(inventory, coinStack);
			}
			//Out of room to place it, throw it at the player
			if(!coinStack.isEmpty())
			{
				if(!player.addItemStackToInventory(coinStack))
				{
					//Spawn an item in the world at the player position
					ItemEntity itemEntity = new ItemEntity(player.world, player.getPosX(), player.getPosY(), player.getPosZ(), coinStack);
					itemEntity.setPickupDelay(40);
					player.world.addEntity(itemEntity);
				}
			}
		}
    	
    }
    
    /**
     * Removes coins of the given value from the given inventory.
     * Used to process a payment.
     * @param value The amount of money to remove from the inventory.
     * @param inventory The inventory from which to take away the coins.
     * @param forceTake Whether we should take as much as we can should the inventory not contain enough money.
     * @return Returns the given value if there is not enough money to take.
     * Returns 0 if exact change was taken, and returns a negative value if more money was taken than what was requested so that change can be calculated separately.
     */
    public static long takeObjectsOfValue(long value, IInventory inventory, boolean forceTake)
	{
		//Check to ensure that the inventory has enough 'value' to remove
		if(MoneyUtil.getValue(inventory) < value && !forceTake)
			return value;
		else
		{
			//Remove objects from the inventory.
			for(CoinData coinData : coinList)
			{
				long coinValue = coinData.getValue();
				if(coinValue <= value)
				{
					//Search the inventory for this coin
					for(int i = 0; i < inventory.getSizeInventory() && coinValue <= value; i++)
					{
						ItemStack itemStack = inventory.getStackInSlot(i);
						if(inventory.getStackInSlot(i).getItem().equals(coinData.getCoinItem()))
						{
							//Remove the coins until they would be too much money or until the stack is empty.
							while(coinValue <= value && !itemStack.isEmpty())
							{
								value -= coinValue;
								itemStack.setCount(itemStack.getCount() - 1);
								if(itemStack.isEmpty())
									inventory.setInventorySlotContents(i, ItemStack.EMPTY);
							}
						}
					}
				}
			}
			//Took all we could without over-taking, so we'll just go through the coinList backwards until we have what we need.
			if(value > 0)
			{
				for(int c = coinList.size() - 1; c >= 0; c--)
				{
					Item coin = coinList.get(c).getCoinItem();
					long coinValue = coinList.get(c).getValue();
					//Search the inventory for this coin
					for(int i = 0; i < inventory.getSizeInventory() && value > 0; i++)
					{
						ItemStack itemStack = inventory.getStackInSlot(i);
						if(itemStack.getItem() == coin)
						{
							//Remove the coins until they would be too much money or until the stack is empty.
							while(value > 0 && !itemStack.isEmpty())
							{
								value -= coinValue;
								itemStack.setCount(itemStack.getCount() - 1);
								if(itemStack.isEmpty())
									inventory.setInventorySlotContents(i, ItemStack.EMPTY);
							}
						}
					}
				}
			}
			
			//Inform the user if we were exact, or if too many items were taken and a refund is required via the getObjectsOfValue function
			return value; 
			
		}
	}
    
    private static long takeObjectsOfValue(long value, NonNullList<ItemStack> inventory)
    {
    	return takeObjectsOfValue(value, inventory, false);
    }
    
    /**
     * Removes coins of the given value from the given itemstack list.
     * Used to process a payment from a wallet or other non-inventory source.
     * @param value The amount of money to remove from the inventory.
     * @param inventory The ItemStack list from which to take away the coins.
     * @return Returns the given value if there is not enough money to take.
     * Returns 0 if exact change was taken, and returns a negative value if more money was taken than what was requested so that change can be calculated separately.
     */
    private static long takeObjectsOfValue(long value, NonNullList<ItemStack> inventory, boolean forceTake)
	{
		//Check to ensure that the inventory has enough 'value' to remove
		if(MoneyUtil.getValue(inventory) < value && !forceTake)
			return value;
		else
		{
			//Remove objects from the inventory.
			for(CoinData coinData : coinList)
			{
				long coinValue = coinData.getValue();
				if(coinValue <= value)
				{
					//Search the inventory for this coin
					for(int i = 0; i < inventory.size() && coinValue <= value; i++)
					{
						ItemStack itemStack = inventory.get(i);
						if(inventory.get(i).getItem().equals(coinData.getCoinItem()))
						{
							//Remove the coins until they would be too much money or until the stack is empty.
							while(coinValue <= value && !itemStack.isEmpty())
							{
								value -= coinValue;
								itemStack.setCount(itemStack.getCount() - 1);
								if(itemStack.isEmpty())
									inventory.set(i, ItemStack.EMPTY);
							}
						}
					}
				}
			}
			//Took all we could without over-taking, so we'll just go through the coinList backwards until we have what we need.
			if(value > 0)
			{
				for(int c = coinList.size() - 1; c >= 0; c--)
				{
					Item coin = coinList.get(c).getCoinItem();
					long coinValue = coinList.get(c).getValue();
					//Search the inventory for this coin
					for(int i = 0; i < inventory.size() && value > 0; i++)
					{
						ItemStack itemStack = inventory.get(i);
						if(itemStack.getItem() == coin)
						{
							//Remove the coins until they would be too much money or until the stack is empty.
							while(value > 0 && !itemStack.isEmpty())
							{
								value -= coinValue;
								itemStack.setCount(itemStack.getCount() - 1);
								if(itemStack.isEmpty())
									inventory.set(i, ItemStack.EMPTY);
							}
						}
					}
				}
			}
			
			//Inform the user if we were exact, or if too many items were taken and a refund is required via the getObjectsOfValue function
			return value; 
			
		}
	}
    
    /**
     * Returns a list of coins in Item Stack format from the given value.
     * Used to convert from a stored value back into coins, or give change after processing a payment.
     */
    //@Deprecated //No longer used in favor of the new CoinValue data type
    public static List<ItemStack> getCoinsOfValue(long value)
	{
    	
		List<ItemStack> items = new ArrayList<>();
		if(value <= 0)
			return items;
		
		//Search through each coin in the coinList
		for(int i = 0; i < coinList.size(); i++)
		{
			if(!coinList.get(i).isHidden)
			{
				Item coin = coinList.get(i).getCoinItem();
				int coinsToGive = 0;
				long coinValue = coinList.get(i).getValue();
				while(coinValue <= value)
				{
					value -= coinValue;
					coinsToGive++;
				}
				while(coinsToGive > 0)
				{
					int giveCount = coinsToGive;
					if(giveCount > 64)
						giveCount = 64;
					coinsToGive -= giveCount;
					ItemStack newStack = new ItemStack(coin, giveCount);
					items.add(newStack);
				}
			}
		}
		
		return items;
	}
    
    /**
     * Returns a list of coins in Item Stack format from the given value.
     * Used to convert from a stored value back into coins, or give change after processing a payment.
     */
    public static List<ItemStack> getCoinsOfValue(CoinValue value)
    {
    	List<ItemStack> items = new ArrayList<>();
    	for(CoinValuePair pricePair : value.coinValues)
    	{
    		int amount = pricePair.amount;
    		while(amount > 0)
    		{
    			ItemStack newStack = new ItemStack(pricePair.coin);
        		int amountToAdd = MathUtil.clamp(amount, 0, newStack.getMaxStackSize());
        		newStack.setCount(amountToAdd);
        		
        		items.add(newStack);
        		amount -= amountToAdd;
    		}
    	}
    	return items;
    }
    
    /**
     * Gets a short display string for the given monetary value in the format of '1n2d..'
     * @param value The amount of monetary value to display.
     */
    public static String getStringOfValue(long value)
    {
    	String string = "";
    	if(value <= 0)
    		return string;
    	
    	for(int i = 0; i < coinList.size(); i++)
    	{
    		CoinData coinData = coinList.get(i);
    		if(!coinData.isHidden)
    		{
	    		int coinCount = 0;
	    		long coinValue = coinList.get(i).getValue();
	    		while(coinValue <= value)
	    		{
	    			value -= coinValue;
	    			coinCount++;
	    		}
	    		if(coinCount > 0)
	    		{
	    			string += String.valueOf(coinCount);
	    			string += coinData.getInitial().getString();
	    		}
    		}
    	}
    	
    	return string;
    	
    }
    
    /**
     * Gets a short display string for the given monetary value in the format of '1n2d..'
     * @param value The amount of monetary value to display.
     */
    public static String getStringOfValue(CoinValue value)
    {
    	return value.getString();
    }

    /**
     * Gets a list of all of the coin-minting recipes generated from the registered coins.
     */
    /*public static List<MintRecipe> getMintRecipes()
    {
    	if(mintRecipes == null || mintRecipesDirty)
    	{
    		mintRecipesDirty = false;
    		mintRecipes = new ArrayList<>();
        	for(CoinData coinData : coinList)
        	{
        		MintRecipe recipe = coinData.getMintRecipe();
        		if(recipe != null)
        			mintRecipes.add(recipe);
        	}
    	}
    	return mintRecipes;
    }*/
    
    /**
     * Gets a list of all of the coin-melting recipes generated from the registered coins.
     */
    /*public static List<MintRecipe> getMeltRecipes()
    {
    	if(meltRecipes == null || meltRecipesDirty)
    	{
    		meltRecipesDirty = false;
    		meltRecipes = new ArrayList<>();
        	for(CoinData coinData : coinList)
        	{
        		MintRecipe recipe = coinData.getMeltRecipe();
        		if(recipe != null)
        			meltRecipes.add(recipe);
        	}
    	}
    	return meltRecipes;
    }*/
    
    /**
     * Gets the coin data for the given coin item
     */
    public static CoinData getData(Item coinItem)
    {
    	for(CoinData coinData : coinList)
    	{
    		if(coinData.coinItem == coinItem)
    			return coinData;
    	}
    	return null;
    }
    
    /**
     * Gets a list of all of the registered coin items
     * By default, hidden coins will not be included in this list.
     */
    public static List<Item> getAllCoins()
    {
    	return getAllCoins(false);
    }
    
    /**
     * Gets a list of all of the registered coin items
     * @param Whether hidden coins will be included in the list.
     */
    public static List<Item> getAllCoins(boolean includeHidden)
    {
    	List<Item> coinItems = new ArrayList<>();
    	for(int i = 0; i < coinList.size(); i++)
    	{
    		if(!coinList.get(i).isHidden || includeHidden)
    			coinItems.add(coinList.get(i).coinItem);
    	}
    	return coinItems;
    }
    
    /**
     * Gets a sorted list of all of the coin data
     * By default CoinData for hidden coins will not be included in this list.
     */
    public static List<CoinData> getAllData()
    {
    	return getAllData(false);
    }
    
    /**
     * Gets a sorted list of all of the coin data
     * @param Whether hidden CoinData will be included in the list.
     */
    public static List<CoinData> getAllData(boolean includeHidden)
    {
    	if(publicCoinList == null || coinListDirty)
    	{
    		coinListDirty = false;
    		publicCoinList = new ArrayList<>();
    		for(int i = 0; i < coinList.size(); i++)
    		{
    			if(!coinList.get(i).isHidden)
    				publicCoinList.add(coinList.get(i));
    		}
    	}
    	if(includeHidden)
    		return coinList;
    	return publicCoinList;
    }
    
    private static Item getItemFromID(String coinID)
    {
    	for(CoinData coinData : coinList)
    	{
    		if(coinData.coinItem.getRegistryName().toString().equals(coinID))
    		{
    			//LightmansCurrency.LOGGER.info("Found coin with id " + coinID);
    			return coinData.coinItem;
    		}
    		//else
    		//	LightmansCurrency.LOGGER.info("Coin with id '" + coinData.coinItem.getRegistryName().toString() + "' does not equal '" + coinID + "'.");
    	}
    	LightmansCurrency.LogWarning("Could not find a coin with an id of " + coinID);
    	return Items.AIR;
    }
    
    public static Pair<Item,Integer> getUpwardConversion(Item coinItem)
    {
    	Item largeCoin = null;
    	int amount = Integer.MAX_VALUE;
    	for(CoinData coinData : coinList)
    	{
    		if(coinData.worthOtherCoin == coinItem && coinData.worthOtherCoinCount < amount && !coinData.isHidden) //Don't get upward conversion for hidden coins
    		{
    			largeCoin = coinData.coinItem;
    			amount = coinData.worthOtherCoinCount;
    		}
    	}
    	if(largeCoin != null)
    		return new Pair<Item,Integer>(largeCoin, amount);
    	return null;
    }
    
    public static Pair<Item,Integer> getDownwardConversion(Item coinItem)
    {
    	Item largeCoin = null;
    	int amount = Integer.MAX_VALUE;
    	for(CoinData coinData : coinList)
    	{
    		if(coinData.worthOtherCoin == coinItem && coinData.worthOtherCoinCount < amount)
    		{
    			largeCoin = coinData.coinItem;
    			amount = coinData.worthOtherCoinCount;
    		}
    	}
    	if(largeCoin != null)
    		return new Pair<Item,Integer>(largeCoin, amount);
    	return null;
    }
    
    public static class CoinData
    {
    	//Coin item
    	private Item coinItem;
    	//Value inputs
    	private Item worthOtherCoin = null;
    	private int worthOtherCoinCount = 0;
    	//Coin's display initial 'c','d', etc.
    	private ITextComponent initial;
		//Is this hidden or not
		private boolean isHidden = false;
		
    	
    	private CoinData(Builder builder)
    	{
    		this.coinItem = builder.coinItem;
    		this.worthOtherCoin = builder.worthOtherCoin;
    		this.worthOtherCoinCount = builder.worthOtherCoinCount;
    		this.initial = builder.initialText;
    		this.isHidden = builder.isHidden;
    	}
    	
    	
    	
    	public long getValue()
    	{
    		if(!this.convertsDownwards())
    		{
    			//LightmansCurrency.LOGGER.info("CoinData.getValue() returning 1 due to being a bottom-coin.");
    			return 1;
    		}
    		CoinData otherCoinData = MoneyUtil.getData(this.worthOtherCoin);
    		if(otherCoinData != null)
    		{
    			//LightmansCurrency.LOGGER.info("CoinData.getValue() calculated value of " + this.worthOtherCoinCount * otherCoinData.getValue() + ".");
    			return this.worthOtherCoinCount * otherCoinData.getValue();
    		}
    		else
    		{
    			LightmansCurrency.LogError("CoinData.getValue() returning 1 due it's dependent coin not being registered.");
    			return 1;
    		}
    	}
    	
    	public Item getCoinItem()
    	{
    		return this.coinItem;
    	}
    	
    	public boolean convertsDownwards()
    	{
    		return this.worthOtherCoin != null && this.worthOtherCoinCount > 0;
    	}
    	
    	public Pair<Item,Integer> getDownwardConversion()
    	{
    		return new Pair<>(this.worthOtherCoin, this.worthOtherCoinCount);
    	}
    	
    	private boolean overrideConversion(@Nonnull Item newOtherCoin, int newOtherCoinAmount)
    	{
    		if(newOtherCoin == this.worthOtherCoin && newOtherCoinAmount == this.worthOtherCoinCount)
    		{
    			LightmansCurrency.LogDebug("Conversion for " + this.coinItem.getRegistryName() + " does not need changing.");
    			return false;
    		}
    		LightmansCurrency.LogDebug("Conversion for " + this.coinItem.getRegistryName() + " changed from " + this.worthOtherCoinCount + "x'" + this.worthOtherCoin.getRegistryName() + "' to " + newOtherCoinAmount + "x'" + newOtherCoin.getRegistryName() + "'");
    		this.worthOtherCoin = newOtherCoin;
    		this.worthOtherCoinCount = newOtherCoinAmount;
    		return true;
    	}
    	
    	public ITextComponent getInitial()
    	{
    		if(this.initial != null)
    			return this.initial;
    		LightmansCurrency.LogWarning("No initial found for the coin '" + this.coinItem.getRegistryName().toString() + "'.");
    		return new StringTextComponent(this.coinItem.getDisplayName(new ItemStack(this.coinItem)).getString().substring(0,1).toLowerCase());
    	}
    	
    	/*public MintRecipe getMintRecipe()
    	{
    		if(!Config.canMint(this.coinItem)) //Block getting the mint recipe if minting is blocked for this coin
    			return null;
    		if(this.mintingMaterialItem != null)
    			return new MintRecipe(this.mintingMaterialItem, this.coinItem);
    		else if(this.mintingMaterialTag != null)
    			return new MintRecipe(this.mintingMaterialTag, this.coinItem);
    		return null;
    	}
    	
    	public MintRecipe getMeltRecipe()
    	{
    		if(!Config.canMelt(this.coinItem)) //Block getting the melt recipe if melting is blocked for this coin
    			return null;
    		if(this.mintingMaterialItem != null)
    			return new MintRecipe(this.coinItem, this.mintingMaterialItem);
    		else if(this.mintingMaterialTag != null)
    		{
    			//Get all items with the tag
    			ITag<Item> tag = ItemTags.getCollection().getTagByID(this.mintingMaterialTag);
    			if(tag != null)
    			{
    				List<Item> tagItems = tag.getAllElements();
    				if(tagItems.size() > 0)
    				{
    					Item resultItem = tagItems.get(0);
    					LightmansCurrency.LogInfo("Creating melt recipe for " + this.coinItem.getRegistryName().toString() + " using the first item with the given tag '" + this.mintingMaterialTag.toString() + "' (" + resultItem.getRegistryName().toString() + ")");
    					return new MintRecipe(this.coinItem, resultItem);
    				}
    			}
    		}
    		return null;
    	}*/
    	
    	public boolean isHidden()
    	{
    		return this.isHidden;
    	}
    	
    	public static Builder getBuilder(Item coinItem)
    	{
    		return new Builder(coinItem);
    	}
    	
    	public static class Builder
    	{
    		
    		//The coin's item
    		final Item coinItem;
    		//Defines its worth based on another coin's value
    		Item worthOtherCoin = null;
    		int worthOtherCoinCount = 0;
    		//The shortened name of the coin
    		ITextComponent initialText = null;
    		//Whether it's publicly visible
    		boolean isHidden = false;
    		
    		public Builder(@Nonnull Item coinItem)
    		{
    			this.coinItem = coinItem;
    		}
    		
    		/**
    		 * Defines what lesser coin can be converted into this one, and how many of those coins are worth 1 of this coin.
    		 */
    		public Builder defineConversion(Item otherCoin, int coinAmount)
    		{
    			this.worthOtherCoin = otherCoin;
    			this.worthOtherCoinCount = coinAmount;
    			return this;
    		}
    		
    		/**
    		 * Defines the coins initial used in displaying the short form of an price/value;
    		 */
    		public Builder defineInitial(ITextComponent textComponent)
    		{
    			this.initialText = textComponent;
    			return this;
    		}
    		
    		/**
    		 * Defines the coins initial used in displaying the short form of an price/value;
    		 */
    		public Builder defineInitial(String translationString)
    		{
    			this.initialText = new TranslationTextComponent(translationString);
    			return this;
    		}
    		
    		public Builder setHidden()
    		{
    			this.isHidden = true;
    			return this;
    		}
    		
    		public CoinData build()
    		{
    			return new CoinData(this);
    		}
    	}
    	
    }
    
    /*public static class MintRecipe
	{
		ResourceLocation itemInTag = null;
		Item itemIn = null;
		Item itemOut = null;
		
		public MintRecipe(ResourceLocation itemTag, Item out)
		{
			itemInTag = itemTag;
			itemOut = out;
		}
		
		public MintRecipe(Item in, Item out)
		{
			itemIn = in;
			itemOut = out;
		}
		
		public boolean validInput(Item itemIn)
		{
			if(itemInTag != null)
			{
				if(itemIn.getTags() != null)
				{
					return itemIn.getTags().contains(itemInTag);
				}
				return false;
			}
			else
			{
				return this.itemIn == itemIn;
			}
		}
		
		public ItemStack getOutput()
		{
			return new ItemStack(itemOut, 1);
		}
		
	}*/
	
    public static class CoinValue
    {
    	
    	public static final String DEFAULT_KEY = "CoinValue";
    	
    	private boolean isFree = false;
    	public boolean isFree() { return this.isFree; }
    	public void setFree(boolean free) { this.isFree = free; if(this.isFree) this.coinValues.clear(); }
    	public final List<CoinValuePair> coinValues;
    	
    	public CoinValue(CompoundNBT compound)
    	{
    		this.coinValues = Lists.newArrayList();
    		this.readFromNBT(compound, DEFAULT_KEY);
    		roundValue();
    	}
    	
    	public CoinValue(long rawValue)
    	{
    		this.coinValues = new ArrayList<>();
    		this.readFromOldValue(rawValue);
    		roundValue();
    	}
    	
    	public CoinValue(NonNullList<ItemStack> inventory)
    	{
    		this.coinValues = new ArrayList<>();
    		//this.readFromOldValue(MoneyUtil.getValue(inventory));
    		for(ItemStack stack : inventory)
    		{
    			Item coinItem = stack.getItem();
    			int count = stack.getCount();
    			CoinData coinData = getData(coinItem);
    			if(coinData != null)
    			{
    				//Get the most relevant non-hidden coin data
        			while(coinData != null && coinData.isHidden && coinData.convertsDownwards())
            		{
        				coinItem = coinData.getDownwardConversion().getFirst();
        				count *= coinData.getDownwardConversion().getSecond();
        				coinData = getData(coinItem);
            		}
        			for(int i = 0; i < this.coinValues.size(); i++)
        			{
        				if(this.coinValues.get(i).coin == coinItem)
        				{
        					this.coinValues.get(i).amount += count;
        					count = 0;
        					break;
        				}
        			}
        			if(count > 0)
        				this.coinValues.add(new CoinValuePair(coinItem, count));
    			}
    			else if(coinItem != Items.AIR)
    			{
    				LightmansCurrency.LogInfo("Coin Data for coin '" + coinItem.getItem().getRegistryName() + "' is null.");
    			}
    			
    		}
    		roundValue();
    	}
    	
    	private CoinValue(CoinValue otherValue)
    	{
    		this.isFree = otherValue.isFree;
    		this.coinValues = new ArrayList<>();
    		if(!this.isFree)
    		{
    			for(CoinValuePair pricePair : otherValue.coinValues)
        		{
        			this.coinValues.add(pricePair.copy());
        		}
        		roundValue();
    		}
    	}
    	
    	@SafeVarargs
		public CoinValue(CoinValuePair... priceValues)
    	{
    		this.coinValues = new ArrayList<>();
    		for(CoinValuePair value : priceValues)
    		{
    			for(int i = 0; i < this.coinValues.size(); i++)
    			{
    				if(this.coinValues.get(i).coin == value.coin)
    				{
    					this.coinValues.get(i).amount += value.amount;
    					value.amount = 0;
    				}
    			}
    			if(value.amount > 0)
    			{
    				this.coinValues.add(value);
    			}
    		}
    		roundValue();
    	}
    	
    	//Private constructors for easyBuilds
    	private CoinValue(List<CoinValuePair> priceValues)
    	{
    		this.coinValues = priceValues;
    		roundValue();
    	}
    	
    	public CompoundNBT writeToNBT(CompoundNBT compound, String key)
    	{
    		
    		if(this.isFree)
    		{
    			compound.putBoolean(key, true);
    		}
    		else
    		{
    			ListNBT list = new ListNBT();
        		for(CoinValuePair value : coinValues)
        		{
        			CompoundNBT thisCompound = new CompoundNBT();
        			//new ItemStack(null).write(nbt)
    				ResourceLocation resource = value.coin.getRegistryName();
        			if(resource != null && MoneyUtil.isCoin(value.coin))
        			{
        				thisCompound.putString("id", resource.toString());
        				thisCompound.putInt("amount", value.amount);
        				list.add(thisCompound);
        			}
        		}
        		compound.put(key, list);
    		}
    		return compound;
    	}
    	
    	public void readFromNBT(CompoundNBT compound, String key)
    	{
    		if(compound.contains(key, Constants.NBT.TAG_LIST))
    		{
    			ListNBT listNBT = compound.getList(key, Constants.NBT.TAG_COMPOUND);
        		if(listNBT != null)
        		{
        			this.coinValues.clear();
        			for(int i = 0; i < listNBT.size(); i++)
        			{
        				CompoundNBT thisCompound = listNBT.getCompound(i);
    					Item priceCoin = MoneyUtil.getItemFromID(thisCompound.getString("id"));
        				int amount = thisCompound.getInt("amount");
        				this.coinValues.add(new CoinValuePair(priceCoin,amount));
        				
        			}
        		}
    		}
    		else if(compound.contains(key))
    		{
    			this.setFree(compound.getBoolean(key));
    		}
    		
    	}
    	
    	public void readFromOldValue(long oldPrice)
    	{
    		this.coinValues.clear();
    		List<ItemStack> coinItems = MoneyUtil.getCoinsOfValue(oldPrice);
    		for(ItemStack stack : coinItems)
    		{
    			Item coinItem = stack.getItem();
    			int amount = stack.getCount();
    			for(int i = 0; i < this.coinValues.size(); i++)
    			{
    				if(this.coinValues.get(i).coin == coinItem)
    				{
    					this.coinValues.get(i).amount += amount;
    					amount = 0;
    				}
    			}
    			if(amount > 0)
    			{
    				this.coinValues.add(new CoinValuePair(coinItem,amount));
    			}
    		}
    		
    	}
    	
    	public void addValue(CoinValue other)
    	{
    		CoinValue otherPrice = other.copy();
    		for(int i = 0; i < this.coinValues.size(); i++)
    		{
    			for(int j = 0; j < otherPrice.coinValues.size(); j++)
    			{
    				if(this.coinValues.get(i).coin == otherPrice.coinValues.get(j).coin)
    				{
    					this.coinValues.get(i).amount += otherPrice.coinValues.get(j).amount;
    					otherPrice.coinValues.get(j).amount = 0;
    				}
    			}
    		}
    		for(CoinValuePair pair : otherPrice.coinValues)
    		{
    			if(pair.amount > 0)
    			{
    				this.coinValues.add(pair);
    			}
    		}
    		roundValue();
    	}
    	
    	public void addValue(Item coin, int amount)
    	{
    		for(int i = 0; i < this.coinValues.size(); i++)
    		{
    			CoinValuePair pair = this.coinValues.get(i);
    			if(pair.coin == coin)
    			{
    				pair.amount += amount;
    				amount = 0;
    			}
    		}
    		if(amount > 0)
    			this.coinValues.add(new CoinValuePair(coin, amount));
    		roundValue();
    	}
    	
    	public void removeValue(Item coin, int amount)
    	{
    		for(int i = 0; i < this.coinValues.size(); i++)
    		{
    			CoinValuePair pair = this.coinValues.get(i);
    			if(pair.coin == coin)
    			{
    				pair.amount -= amount;
    				if(pair.amount <= 0)
    				{
    					this.coinValues.remove(i);
    				}
    				return;
    			}
    		}
    	}
    	
    	private void roundValue()
    	{
    		while(needsRounding())
    		{
    			for(int i = 0; i < this.coinValues.size(); i++)
    			{
    				if(needsRounding(i))
    				{
    					CoinValuePair pair = this.coinValues.get(i);
    					Pair<Item,Integer> conversion = MoneyUtil.getUpwardConversion(pair.coin);
    					int largeAmount = 0;
    					while(pair.amount >= conversion.getSecond())
    					{
    						largeAmount++;
    						pair.amount -= conversion.getSecond();
    					}
    					//If there's none of this coin left, remove if from the list
    					if(pair.amount == 0)
    					{
    						this.coinValues.remove(i);
    						//Shrink the index to avoid oversight
    						i--;
    					}
    					//Add the larger amount to the price values list
    					for(CoinValuePair thisPair : this.coinValues)
    					{
    						if(thisPair.coin == conversion.getFirst())
    						{
    							thisPair.amount += largeAmount;
    							largeAmount = 0;
    						}
    					}
    					if(largeAmount > 0)
    					{
    						this.coinValues.add(new CoinValuePair(conversion.getFirst(), largeAmount));
    					}
    				}
    			}
    		}
    		sortValue();
    	}
    	
    	private void sortValue()
    	{
    		List<CoinValuePair> newList = new ArrayList<>();
    		while(this.coinValues.size() > 0)
    		{
    			//Get the largest index
    			long largestValue = MoneyUtil.getValue(this.coinValues.get(0).coin);
    			int largestIndex = 0;
    			for(int i = 1; i < this.coinValues.size(); i++)
    			{
    				long thisValue = MoneyUtil.getValue(this.coinValues.get(i).coin);
    				if(thisValue > largestValue)
    				{
    					largestIndex = i;
    					largestValue = thisValue;
    				}
    			}
    			newList.add(this.coinValues.get(largestIndex));
    			this.coinValues.remove(largestIndex);
    		}
    		for(int i = 0; i < newList.size(); i++)
    		{
    			this.coinValues.add(newList.get(i));
    		}
    	}
    	
    	private boolean needsRounding()
    	{
    		for(int i = 0; i < this.coinValues.size(); i++)
    		{
    			if(this.needsRounding(i))
    				return true;
    		}
    		return false;
    	}
    	
    	private boolean needsRounding(int index)
    	{
    		CoinValuePair pair = this.coinValues.get(index);
    		Pair<Item,Integer> conversion = MoneyUtil.getUpwardConversion(pair.coin);
			if(conversion != null)
			{
				if(pair.amount >= conversion.getSecond())
					return true;
			}
			return false;
    	}
    	
    	public int getEntry(Item coinItem)
    	{
    		for(CoinValuePair pair : this.coinValues)
    		{
    			if(pair.coin == coinItem)
    				return pair.amount;
    		}
    		return 0;
    	}
    	
    	public CoinValue copy()
    	{
    		return new CoinValue(this);
    	}
    	
    	public String getString() { return this.getString(""); }
    	
    	public String getString(String emptyFiller)
    	{
    		if(this.isFree)
    			return new TranslationTextComponent("gui.coinvalue.free").getString();
    		String string = "";
        	for(int i = 0; i < this.coinValues.size(); i++)
        	{
        		CoinValuePair pricePair = this.coinValues.get(i);
        		CoinData coinData = getData(pricePair.coin);
        		if(coinData != null)
        		{
        			string += String.valueOf(pricePair.amount);
        			string += coinData.getInitial().getString();
        		}
        	}
        	if(string.isEmpty())
        		return emptyFiller;
        	return string;
    	}
    	
    	public long getRawValue()
    	{
    		if(this.isFree)
    			return 0;
    		long value = 0;
    		for(CoinValuePair pricePair : this.coinValues)
    		{
    			CoinData coinData = MoneyUtil.getData(pricePair.coin);
    			if(coinData != null)
    			{
    				value += pricePair.amount * coinData.getValue();
    			}
    		}
    		//LightmansCurrency.LOGGER.info("Accumulated Value: " + value + " PricePair count: " + this.priceValues.size());
    		return value;
    	}
    	
    	public CoinValue ApplyMultiplier(double costMultiplier)
    	{
    		CoinValue multipliedValue = new CoinValue();
    		costMultiplier = MathUtil.clamp(costMultiplier, 0d, 10d);
    		
    		for(int i = 0; i < this.coinValues.size(); i++)
    		{
    			int amount = this.coinValues.get(i).amount;
    			Item coin = this.coinValues.get(i).coin;
    			double newAmount = amount * costMultiplier;
    			double leftoverAmount = newAmount % 1d;
    			multipliedValue.addValue(coin, (int)newAmount);
    			CoinData coinData = MoneyUtil.getData(coin);
    			while(coinData != null && coinData.convertsDownwards() && leftoverAmount > 0d)
    			{
    				Pair<Item,Integer> conversion = coinData.getDownwardConversion();
    				coin = conversion.getFirst();
    				coinData = MoneyUtil.getData(coin);
    				newAmount = leftoverAmount * conversion.getSecond();
    				leftoverAmount = newAmount % 1d;
    				multipliedValue.addValue(coin, (int)newAmount);
    			}
    		}
    		return multipliedValue;
    	}
    	
    	/**
    	 * Gets the two most significant coin stacks from the given value for use as a villager price
    	 */
    	public Pair<ItemStack,ItemStack> getTradeItems()
    	{
    		List<ItemStack> coins = MoneyUtil.getCoinsOfValue(this);
    		ItemStack stack1 = ItemStack.EMPTY;
    		ItemStack stack2 = ItemStack.EMPTY;
    		//Get the first stack
    		if(coins.size() > 0)
    			stack1 = coins.get(0);
    		else
    			LightmansCurrency.LogWarning("A CoinValue used in a trade gave no coins as an output.");
    		//Get the second stack
    		if(coins.size() > 1)
    			stack2 = coins.get(1);
    		//Warn about any excess stacks
    		if(coins.size() > 2)
    			LightmansCurrency.LogWarning("A CoinValue used in a trade gave more than two stacks of coins of output.");
    		return new Pair<>(stack1, stack2);
    	}
    	
    	/**
    	 * Gets the most significant coin stack from the given value for use as a villager buy item
    	 */
    	public ItemStack getTradeItem()
    	{
    		List<ItemStack> coins = MoneyUtil.getCoinsOfValue(this);
    		ItemStack stack = ItemStack.EMPTY;
    		//Get the first stack
    		if(coins.size() > 0)
    			stack = coins.get(0);
    		else
    			LightmansCurrency.LogWarning("A CoinValue used in a trade gave no coins as an output.");
    		//Warn about any excess stacks
    		if(coins.size() > 1)
    			LightmansCurrency.LogWarning("A CoinValue used in a trade output gave more than one stack of coins of output.");
    		return stack;
    	}
    	
    	public static class CoinValuePair
    	{
    		
    		public final Item coin;
    		public int amount = 0;
    		
    		public CoinValuePair(Item coin, int amount)
    		{
    			this.coin = coin;
    			this.amount = amount;
    		}
    		
    		public CoinValuePair copy()
    		{
    			return new CoinValuePair(this.coin,this.amount);
    		}
    		
    	}
    	
    	public static final CoinValue EMPTY = new CoinValue();
    	
    	public static CoinValue easyBuild1(ItemStack... stack)
    	{
    		List<CoinValuePair> pairs = new ArrayList<>();
    		for(int i = 0; i < stack.length; i++)
    		{
    			if(MoneyUtil.isCoin(stack[i]) || !MoneyUtil.initialized())
    				pairs.add(new CoinValuePair(stack[i].getItem(), stack[i].getCount()));
    			else
    				LightmansCurrency.LogWarning("CoinValue.easyBuild1: ItemStack at index " + i + " is not a valid coin.");
    		}
    		return new CoinValue(pairs);
    	}
    	
    	public static CoinValue easyBuild2(IInventory inventory)
    	{
    		return new CoinValue(MoneyUtil.getValue(inventory));
    		/*List<CoinValuePair> pairs = new ArrayList<>();
    		for(int i = 0; i < inventory.getSizeInventory(); i++)
    		{
    			Item item = inventory.getStackInSlot(i).getItem();
    			int amountToAdd = inventory.getStackInSlot(i).getCount();
    			if(MoneyUtil.isCoin(item) || !MoneyUtil.initialized())
    			{
    				for(int x = 0; x < pairs.size() && amountToAdd > 0; x++)
    				{
    					if(pairs.get(x).coin == item)
    					{
    						pairs.get(x).amount += amountToAdd;
    						amountToAdd = 0;
    					}
    				}
    				if(amountToAdd > 0)
    				{
    					pairs.add(new CoinValuePair(item, amountToAdd));
    				}
    			}
    		}
    		return new CoinValue(pairs);*/
    	}
    	
    	/*public static boolean canAfford(CoinValue price, CoinValue availableMoney)
    	{
    		return price.isEqualValue(availableMoney) || price.isGreaterValue(availableMoney);
    	}
    	
    	public boolean isLesserValue(CoinValue otherValue)
    	{
    		return false;
    	}
    	
    	public boolean isEqualValue(CoinValue otherValue)
    	{
    		this.sortValue();
    		return false;
    	}
    	
    	public boolean isGreaterValue(CoinValue otherValue)
    	{
    		return false;
    	}*/
    	
    }

    
}
