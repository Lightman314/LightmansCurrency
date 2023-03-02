package io.github.lightman314.lightmanscurrency.common.money;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.events.GetDefaultMoneyDataEvent;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber
public class MoneyUtil {
	
	public static final String MONEY_FILE_LOCATION = "config/lightmanscurrency/MasterCoinList.json";
	
	public static final String MAIN_CHAIN = "main";
	
	private static MoneyData moneyData = null;
	public static MoneyData getMoneyData() { return moneyData; }
	public static void receiveMoneyData(MoneyData data) { moneyData = data; }
	
	public static Component getPluralName(Item coin) {
		if(moneyData != null)
			return moneyData.getPluralName(coin);
		return getDefaultPlural(coin);
	}
	
	public static Component getDefaultPlural(Item coin) {
		//If no plural form defined, attempt to find one.
		String defaultPlural = coin.getDescriptionId() + ".plural";
		if(new TranslatableComponent(defaultPlural).getString().equals(defaultPlural))
			return new TranslatableComponent("item.lightmanscurrency.generic.plural", coin.getName(new ItemStack(coin)));
		return new TranslatableComponent(defaultPlural);
	}

	//Make high priority so that it runs before other "server start" events that may end up loading traders
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onServerStart(ServerStartedEvent event) {
		LightmansCurrency.LogInfo("Setting up Money System for server.");
		reloadMoneyData();
	}
	
	public static void reloadMoneyData() {
		LightmansCurrency.LogInfo("Reloading Money Data");
		File mcl = new File(MONEY_FILE_LOCATION);
		if(!mcl.exists())
		{
			createMoneyDataFile(mcl);
		}
		try { 
			JsonObject fileData = GsonHelper.parse(Files.readString(mcl.toPath()));
			moneyData = MoneyData.fromJson(fileData);
		} catch(Throwable e) {
			LightmansCurrency.LogError("Error loading Master Coin List. Using default values for now.", e);
			moneyData = MoneyData.generateDefault();
		}
		LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), moneyData);
	}
	
	private static void createMoneyDataFile(File mcl) {
		File dir = new File(mcl.getParent());
		if(!dir.exists())
			dir.mkdirs();
		if(dir.exists())
		{
			try {
				
				MoneyData defaultData = MoneyData.generateDefault();
				
				mcl.createNewFile();
				
				FileUtil.writeStringToFile(mcl, new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(defaultData.toJson()));
				
				LightmansCurrency.LogInfo("MasterCoinList.json does not exist. Creating a fresh copy.");
				
			} catch(Throwable e) { LightmansCurrency.LogError("Error attempting to create 'MasterCoinList.json' file.", e); }
		}
	}
	
	@SubscribeEvent
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		//Send the player the Money Data
		LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(event.getPlayer()), moneyData);
	}
	
	/**
     * Initializes the default master coin list from order of highest to lowest values.
     */
	@SubscribeEvent
    public static void initializeDefaultCoins(GetDefaultMoneyDataEvent event)
    {
    	
    	LightmansCurrency.LogInfo("Generating default coin values.");
    	
    	//Copper Coin
    	event.dataCollector.addCoinBuilder(CoinData.getBuilder(ModItems.COIN_COPPER.get(), MAIN_CHAIN)
    			.defineInitial("item.lightmanscurrency.coin_copper.initial")
    			.definePluralForm("item.lightmanscurrency.coin_copper.plural"));
    	//Iron Coin
    	event.dataCollector.addCoinBuilder(CoinData.getBuilder(ModItems.COIN_IRON.get(), MAIN_CHAIN)
    			.defineInitial("item.lightmanscurrency.coin_iron.initial")
    			.definePluralForm("item.lightmanscurrency.coin_iron.plural")
    			.defineConversion(ModItems.COIN_COPPER.get(), 10));
    	//Gold Coin
    	event.dataCollector.addCoinBuilder(CoinData.getBuilder(ModItems.COIN_GOLD.get(), MAIN_CHAIN)
    			.defineInitial("item.lightmanscurrency.coin_gold.initial")
    			.definePluralForm("item.lightmanscurrency.coin_gold.plural")
    			.defineConversion(ModItems.COIN_IRON.get(), 10));
    	//Emerald Coin
    	event.dataCollector.addCoinBuilder(CoinData.getBuilder(ModItems.COIN_EMERALD.get(), MAIN_CHAIN)
    			.defineInitial("item.lightmanscurrency.coin_emerald.initial")
    			.definePluralForm("item.lightmanscurrency.coin_emerald.plural")
    			.defineConversion(ModItems.COIN_GOLD.get(), 10));
    	//Diamond Coin
    	event.dataCollector.addCoinBuilder(CoinData.getBuilder(ModItems.COIN_DIAMOND.get(), MAIN_CHAIN)
    			.defineInitial("item.lightmanscurrency.coin_diamond.initial")
    			.definePluralForm("item.lightmanscurrency.coin_diamond.plural")
    			.defineConversion(ModItems.COIN_EMERALD.get(), 10));
    	//Netherite Coin
    	event.dataCollector.addCoinBuilder(CoinData.getBuilder(ModItems.COIN_NETHERITE.get(), MAIN_CHAIN)
    			.defineInitial("item.lightmanscurrency.coin_netherite.initial")
    			.definePluralForm("item.lightmanscurrency.coin_netherite.plural")
    			.defineConversion(ModItems.COIN_DIAMOND.get(), 10));
    	
    	//Hidden coins
    	//Copper Coinpile
    	event.dataCollector.addCoinBuilder(CoinData.getBuilder(ModBlocks.COINPILE_COPPER.get(), MAIN_CHAIN)
    			.defineConversion(ModItems.COIN_COPPER.get(), 9)
    			.definePluralForm("block.lightmanscurrency.coinpile_copper.plural")
    			.setHidden());
    	//Copper Coin Block
    	event.dataCollector.addCoinBuilder(CoinData.getBuilder(ModBlocks.COINBLOCK_COPPER.get(), MAIN_CHAIN)
    			.defineConversion(ModBlocks.COINPILE_COPPER.get(), 4)
    			.definePluralForm("block.lightmanscurrency.coinblock_copper.plural")
    			.setHidden());
    	
    	//Iron Coinpile
    	event.dataCollector.addCoinBuilder(CoinData.getBuilder(ModBlocks.COINPILE_IRON.get(), MAIN_CHAIN)
    			.defineConversion(ModItems.COIN_IRON.get(), 9)
    			.definePluralForm("block.lightmanscurrency.coinpile_iron.plural")
    			.setHidden());
    	//Iron Coin Block
    	event.dataCollector.addCoinBuilder(CoinData.getBuilder(ModBlocks.COINBLOCK_IRON.get(), MAIN_CHAIN)
    			.defineConversion(ModBlocks.COINPILE_IRON.get(), 4)
    			.definePluralForm("block.lightmanscurrency.coinblock_iron.plural")
    			.setHidden());
    	
    	//Gold Coinpile
    	event.dataCollector.addCoinBuilder(CoinData.getBuilder(ModBlocks.COINPILE_GOLD.get(), MAIN_CHAIN)
    			.defineConversion(ModItems.COIN_GOLD.get(), 9)
    			.definePluralForm("block.lightmanscurrency.coinpile_gold.plural")
    			.setHidden());
    	//Gold Coin Block
    	event.dataCollector.addCoinBuilder(CoinData.getBuilder(ModBlocks.COINBLOCK_GOLD.get(), MAIN_CHAIN)
    			.defineConversion(ModBlocks.COINPILE_GOLD.get(), 4)
    			.definePluralForm("block.lightmanscurrency.coinblock_gold.plural")
    			.setHidden());
    	
    	//Emerald Coinpile
    	event.dataCollector.addCoinBuilder(CoinData.getBuilder(ModBlocks.COINPILE_EMERALD.get(), MAIN_CHAIN)
    			.defineConversion(ModItems.COIN_EMERALD.get(), 9)
    			.definePluralForm("block.lightmanscurrency.coinpile_emerald.plural")
    			.setHidden());
    	//Emerald Coin Block
    	event.dataCollector.addCoinBuilder(CoinData.getBuilder(ModBlocks.COINBLOCK_EMERALD.get(), MAIN_CHAIN)
    			.defineConversion(ModBlocks.COINPILE_EMERALD.get(), 4)
    			.definePluralForm("block.lightmanscurrency.coinblock_emerald.plural")
    			.setHidden());
    	
    	//Diamond Coinpile
    	event.dataCollector.addCoinBuilder(CoinData.getBuilder(ModBlocks.COINPILE_DIAMOND.get(), MAIN_CHAIN)
    			.defineConversion(ModItems.COIN_DIAMOND.get(), 9)
    			.definePluralForm("block.lightmanscurrency.coinpile_diamond.plural")
    			.setHidden());
    	//Diamond Coin Block
    	event.dataCollector.addCoinBuilder(CoinData.getBuilder(ModBlocks.COINBLOCK_DIAMOND.get(), MAIN_CHAIN)
    			.defineConversion(ModBlocks.COINPILE_DIAMOND.get(), 4)
    			.definePluralForm("block.lightmanscurrency.coinblock_diamond.plural")
    			.setHidden());
    	
    	//Netherite Coinpile
    	event.dataCollector.addCoinBuilder(CoinData.getBuilder(ModBlocks.COINPILE_NETHERITE.get(), MAIN_CHAIN)
    			.defineConversion(ModItems.COIN_NETHERITE.get(), 9)
    			.definePluralForm("block.lightmanscurrency.coinpile_netherite.plural")
    			.setHidden());
    	//Netherite Coin Block
    	event.dataCollector.addCoinBuilder(CoinData.getBuilder(ModBlocks.COINBLOCK_NETHERITE.get(), MAIN_CHAIN)
    			.defineConversion(ModBlocks.COINPILE_NETHERITE.get(), 4)
    			.definePluralForm("block.lightmanscurrency.coinblock_netherite.plural")
    			.setHidden());
		
    }
	
    /**
     * Checks if the given item is in the master coin list.
     * By default, allows hidden coins
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
    	CoinData data = getData(item);
    	if(data == null)
    		return false;
    	return allowHidden || !data.isHidden;
    }

	public static boolean isVisibleCoin(Item item) { return isCoin(item, false); }
    
    /**
     * Checks if the given item is both a coin & that the coin is considered hidden.
     * Used to blacklist "hidden" coins from certain slots (such as the ATM slots) while still letting them be considered coins in the default isCoin check
     */
    public static boolean isCoinHidden(Item item)
    {
    	if(item == null)
    		return false;
    	CoinData data = getData(item);
    	if(data == null)
    		return false;
    	return data.isHidden;
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
    public static CoinValue getCoinValue(ItemStack coinStack) { return new CoinValue(getValue(coinStack)); }
    
    /**
     * Gets the total value of the item stack.
     * @param coinStack The item stack to get the value of.
     */
    public static long getValue(ItemStack coinStack)
    {
    	return getValue(coinStack.getItem()) * coinStack.getCount();
    }
    
    /**
	 * Gets the total value of the items in the given ItemStack list.
	 * @param inventory The list full of coins from which to get the value of.
	 */
    public static CoinValue getCoinValue(NonNullList<ItemStack> inventory) { return new CoinValue(getValue(inventory)); }
    
    /**
	 * Gets the total value of the items in the given ItemStack list.
	 * @param inventory The list full of coins from which to get the value of.
	 */
    public static long getValue(NonNullList<ItemStack> inventory)
	{
    	long value = 0;
		for (ItemStack itemStack : inventory)
			value += getValue(itemStack);
		return value;
	}
    
    /**
	 * Gets the total value of the items in the given inventory.
	 * @param inventory The inventory full of coins with which to get the value of.
	 */
    public static CoinValue getCoinValue(Container inventory) { return new CoinValue(getValue(inventory)); }
    
    /**
	 * Gets the total value of the items in the given inventory.
	 * @param inventory The inventory full of coins with which to get the value of.
	 */
    public static long getValue(Container inventory)
	{
    	long value = 0;
		for(int i = 0; i < inventory.getContainerSize(); i++)
		{
			value += getValue(inventory.getItem(i));
		}
		return value;
	}
    
    /**
     * Converts all coins in the inventory to as large a coin as humanly possible
     */
    public static void ConvertAllCoinsUp(Container inventory)
    {
    	if(moneyData == null)
    		return;
    	List<Item> coinList = getAllCoins(false);
    	for(int i = 1; i < coinList.size(); i++)
    	{
    		ConvertCoinsUp(inventory, coinList.get(i));
    	}
    	for(int i = coinList.size() - 1; i > 0; i--)
    	{
    		ConvertCoinsUp(inventory, coinList.get(i));
    	}
    }
    
    /**
     * Converts all coins in the inventory to as large a coin as humanly possible
     */
    public static NonNullList<ItemStack> ConvertAllCoinsUp(NonNullList<ItemStack> inventoryList)
    {
    	Container inventory = InventoryUtil.buildInventory(inventoryList);
    	ConvertAllCoinsUp(inventory);
    	return InventoryUtil.buildList(inventory);
    }
    
    /**
     * Converts as many of the small coin that it can into its next largest coin
     */
    public static void ConvertCoinsUp(Container inventory, Item smallCoin)
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
    public static void ConvertAllCoinsDown(Container inventory)
    {
    	ConvertAllCoinsDown(inventory, 2);
    }
    
    /**
     * Converts all coins in the inventory to as small a coin as humanly possible
     * @param iterations The number of times to repeatedly convert to ensure that the available space is used. Default is 2.
     */
    private static void ConvertAllCoinsDown(Container inventory, int iterations)
    {
    	if(moneyData == null)
    		return;
    	List<CoinData> coinList = moneyData.getSortedCoinList();
    	for(int x = 0; x < iterations; x++)
    	{
    		for(int i = 0; i < (coinList.size() - 1); i++)
    		{
    			if(!coinList.get(i).isHidden)
    				ConvertCoinsDown(inventory, coinList.get(i).coinItem);
    		}
    	}
    }
    
    /**
     * Converts as many of the large coin that it can into its defined smaller coin
     */
    public static void ConvertCoinsDown(Container inventory, Item largeCoin)
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
    
    public static void SortCoins(Container inventory)
	{
		
		InventoryUtil.MergeStacks(inventory);
		
		List<ItemStack> oldInventory = new ArrayList<>();
		for(int i = 0; i < inventory.getContainerSize(); i++)
		{
			if(!inventory.getItem(i).isEmpty())
				oldInventory.add(inventory.getItem(i));
		}
		
		inventory.clearContent();
		
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
			
			inventory.setItem(index, oldInventory.get(highestIndex));
			index++;
			oldInventory.remove(highestIndex);
		}
		
	}
    
    public static NonNullList<ItemStack> SortCoins(NonNullList<ItemStack> inventory)
    {
    	Container tempInventory = InventoryUtil.buildInventory(inventory);
    	SortCoins(tempInventory);
    	return InventoryUtil.buildList(tempInventory);
    }
    
    /**
     * Process a payment from the given coin slot inventory & player.
     * @param inventory An inventory that may or may not contain coins that payment can be taken from. Can be null, but then payment will only be taken from the players' wallet.
     * @param player The player making the payment. Required for item overflow, and wallet aquisition.
     * @param price The price of the payment that we are attempting to process.
     * @return Whether the payment went through. If false is returned, no money was taken from the wallet nor the inventory.
     */
    public static boolean ProcessPayment(@Nullable Container inventory, @Nonnull Player player, @Nonnull CoinValue price)
    {
    	return ProcessPayment(inventory, player, price, false);
    }
    
    /**
     * Process a payment from the given coin slot inventory & player.
     * @param inventory An inventory that may or may not contain coins that payment can be taken from. Can be null, but then payment will only be taken from the players' wallet.
     * @param player The player making the payment. Required for item overflow, and wallet aquisition.
     * @param price The price of the payment that we are attempting to process.
     * @param ignoreWallet Whether we should ignore the players wallet in terms of taking payment or giving change.
     * @return Whether the payment went through. If false is returned, no money was taken from the wallet nor the inventory.
     */
    public static boolean ProcessPayment(@Nullable Container inventory, @Nonnull Player player, @Nonnull CoinValue price, boolean ignoreWallet)
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
					WalletMenuBase.OnWalletUpdated(player);
    			}
    			if(!coinStack.isEmpty() && inventory != null)
    			{
    				//TryPutItemStack allows partial placement unlike PutItemStack which is used for cancelable placements
    				coinStack = InventoryUtil.TryPutItemStack(inventory, coinStack);
    			}
    			//Out of room to place it, throw it at the player
    			if(!coinStack.isEmpty())
    			{
    				ItemHandlerHelper.giveItemToPlayer(player, coinStack);
    			}
    		}
    	}
    	
    	return true;
    }
    
    /**
     * Put money into the coin slot inventory & player.
     * @param inventory An inventory in which to give the coins to by default should the player not have an equipped wallet. Can be null, but then payment will only be given to the players' wallet/inventory.
     * @param player The player receiving the money. Required for item overflow, and wallet aquisition.
     * @param change The amount of money we're attempting to give.
     */
    public static void ProcessChange(@Nullable Container inventory, @Nonnull Player player, @Nonnull CoinValue change)
    {
    	ProcessChange(inventory, player, change, false);
    }
    
    public static void ProcessChange(@Nullable Container inventory, @Nonnull Player player, @Nonnull CoinValue change, boolean ignoreWallet)
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
				WalletMenuBase.OnWalletUpdated(player);
			}
			if(!coinStack.isEmpty() && inventory != null)
			{
				//TryPutItemStack allows partial placement unlike PutItemStack which is used for cancelable placements
				coinStack = InventoryUtil.TryPutItemStack(inventory, coinStack);
			}
			//Out of room to place it, throw it at the player
			if(!coinStack.isEmpty())
			{
				player.getInventory().placeItemBackInInventory(coinStack);
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
    public static long takeObjectsOfValue(long value, Container inventory, boolean forceTake)
	{
    	if(moneyData == null)
    		return value;
		//Check to ensure that the inventory has enough 'value' to remove
		if(MoneyUtil.getValue(inventory) >= value || forceTake)
		{
			List<CoinData> coinList = moneyData.getSortedCoinList();
			//Remove objects from the inventory.
			for(CoinData coinData : coinList)
			{
				long coinValue = coinData.getValue();
				if(coinValue <= value)
				{
					//Search the inventory for this coin
					for(int i = 0; i < inventory.getContainerSize() && coinValue <= value; i++)
					{
						ItemStack itemStack = inventory.getItem(i);
						if(inventory.getItem(i).getItem().equals(coinData.coinItem))
						{
							//Remove the coins until they would be too much money or until the stack is empty.
							while(coinValue <= value && !itemStack.isEmpty())
							{
								value -= coinValue;
								itemStack.setCount(itemStack.getCount() - 1);
								if(itemStack.isEmpty())
									inventory.setItem(i, ItemStack.EMPTY);
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
					Item coin = coinList.get(c).coinItem;
					long coinValue = coinList.get(c).getValue();
					//Search the inventory for this coin
					for(int i = 0; i < inventory.getContainerSize() && value > 0; i++)
					{
						ItemStack itemStack = inventory.getItem(i);
						if(itemStack.getItem() == coin)
						{
							//Remove the coins until they would be too much money or until the stack is empty.
							while(value > 0 && !itemStack.isEmpty())
							{
								value -= coinValue;
								itemStack.setCount(itemStack.getCount() - 1);
								if(itemStack.isEmpty())
									inventory.setItem(i, ItemStack.EMPTY);
							}
						}
					}
				}
			}
			
			//Inform the user if we were exact, or if too many items were taken and a refund is required via the getObjectsOfValue function

		}
		return value;
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
    public static long takeObjectsOfValue(long value, NonNullList<ItemStack> inventory, boolean forceTake)
	{
    	if(moneyData == null)
    		return value;
		//Check to ensure that the inventory has enough 'value' to remove
		if(MoneyUtil.getValue(inventory) >= value || forceTake)
		{
			//Remove objects from the inventory.
			List<CoinData> coinList = moneyData.getSortedCoinList();
			for(CoinData coinData : coinList)
			{
				long coinValue = coinData.getValue();
				if(coinValue <= value)
				{
					//Search the inventory for this coin
					for(int i = 0; i < inventory.size() && coinValue <= value; i++)
					{
						ItemStack itemStack = inventory.get(i);
						if(inventory.get(i).getItem().equals(coinData.coinItem))
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
					Item coin = coinList.get(c).coinItem;
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
			
		}
		//Inform the user if we were exact, or if too many items were taken and a refund is required via the getObjectsOfValue function
		return value;
	}
    
    /**
     * Returns a list of coins in Item Stack format from the given value.
     * Used to convert from a stored value back into coins, or give change after processing a payment.
     */
    //@Deprecated //No longer used in favor of the new CoinValue data type
    //Do not replace, as the CoinValue uses this to read from long values
    public static List<ItemStack> getCoinsOfValue(long value)
	{
		List<ItemStack> items = new ArrayList<>();
		if(value <= 0 || moneyData == null)
			return items;
		
		//Search through each coin in the coinList
		List<CoinData> coinList = moneyData.getSortedCoinList(MAIN_CHAIN);
		for (CoinData coinData : coinList) {
			if (!coinData.isHidden) {
				Item coin = coinData.coinItem;
				int coinsToGive = 0;
				long coinValue = coinData.getValue();
				while (coinValue <= value) {
					value -= coinValue;
					coinsToGive++;
				}
				while (coinsToGive > 0) {
					int giveCount = coinsToGive;
					ItemStack newStack = new ItemStack(coin);
					if (giveCount > newStack.getMaxStackSize())
						giveCount = newStack.getMaxStackSize();
					coinsToGive -= giveCount;
					newStack.setCount(giveCount);
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
    	for(CoinValue.CoinValuePair pricePair : value.coinValues)
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
     * Gets a short display string for the given monetary value in the format of '1n2d...'
     * @param value The amount of monetary value to display.
     */
    public static String getStringOfValue(long value)
    {
    	return new CoinValue(value).getString();
    }
    
    /**
     * Gets the coin data for the given coin item
     */
    public static CoinData getData(Item coinItem)
    {
    	if(moneyData == null)
    		return null;
    	return moneyData.getData(coinItem);
    }
    
    /**
     * Gets a list of all the registered coin items
     * By default, hidden coins will not be included in this list.
     */
    public static List<Item> getAllCoins()
    {
    	return getAllCoins(false);
    }
    
    /**
     * Gets a list of all the registered coin items
     * @param includeHidden Whether hidden coins will be included in the list.
     */
    public static List<Item> getAllCoins(boolean includeHidden)
    {
    	if(moneyData == null)
    		return new ArrayList<>();
    	List<Item> coinItems = new ArrayList<>();
    	List<CoinData> coinList = moneyData.getSortedCoinList();
		for (CoinData coinData : coinList) {
			if (!coinData.isHidden || includeHidden)
				coinItems.add(coinData.coinItem);
		}
    	return coinItems;
    }
    
    /**
     * Gets a list of all the registered coin items
     * By default, hidden coins will not be included in this list.
     * @param chain The coin chain to receive coins from
     */
    public static List<Item> getAllCoins(String chain)
    {
    	return getAllCoins(chain, false);
    }
    
    /**
     * Gets a list of all the registered coin items
     * @param chain The coin chain to receive coins from
     * @param includeHidden Whether hidden coins will be included in the list.
     */
    public static List<Item> getAllCoins(String chain, boolean includeHidden)
    {
    	if(moneyData == null)
    		return new ArrayList<>();
    	List<Item> coinItems = new ArrayList<>();
    	List<CoinData> coinList = moneyData.getSortedCoinList();
		for (CoinData coinData : coinList) {
			if (coinData.chain.contentEquals(chain) && (!coinData.isHidden || includeHidden))
				coinItems.add(coinData.coinItem);
		}
    	return coinItems;
    }
    
    /**
     * Gets a sorted list of all the coin data
     * By default CoinData for hidden coins will not be included in this list.
     */
    public static List<CoinData> getAllData()
    {
    	return getAllData(false);
    }
    
    /**
     * Gets a sorted list of all the coin data
     * @param includeHidden Whether hidden CoinData will be included in the list.
     */
    public static List<CoinData> getAllData(boolean includeHidden)
    {
    	if(moneyData == null)
    		return new ArrayList<>();
    	List<CoinData> coinList = moneyData.getSortedCoinList();
    	if(includeHidden)
    		return coinList;
    	List<CoinData> publicCoinList = new ArrayList<>();
		for (CoinData coinData : coinList) {
			if (!coinData.isHidden)
				publicCoinList.add(coinData);
		}
    	return publicCoinList;
    }
    
    /**
     * Gets a sorted list of all the coin data in the given chain
     * By default CoinData for hidden coins will not be included in this list.
     * @param chain The coin chain to receive coins from
     */
    public static List<CoinData> getAllData(String chain)
    {
    	return getAllData(chain, false);
    }
    
    /**
     * Gets a sorted list of all the coin data
     * @param chain The coin chain to receive coins from
     * @param includeHidden Whether hidden CoinData will be included in the list.
     */
    public static List<CoinData> getAllData(String chain, boolean includeHidden)
    {
    	if(moneyData == null)
    		return new ArrayList<>();
    	List<CoinData> coinList = moneyData.getSortedCoinList(chain);
    	if(includeHidden)
    		return coinList;
    	List<CoinData> publicCoinList = new ArrayList<>();
		for (CoinData coinData : coinList) {
			if (!coinData.isHidden)
				publicCoinList.add(coinData);
		}
    	return publicCoinList;
    }
    
    public static Pair<Item,Integer> getUpwardConversion(Item coinItem)
    {
    	if(moneyData == null)
    		return null;
    	Item largeCoin = null;
    	int amount = Integer.MAX_VALUE;
    	List<CoinData> coinList = moneyData.getSortedCoinList();
    	CoinData smallCoinData = moneyData.getData(coinItem);
    	if(smallCoinData == null)
    		return null;
    	for(CoinData coinData : coinList)
    	{
    		//Don't get upward conversion for hidden coins or coins from a different chain
    		if(coinData.worthOtherCoin == coinItem && coinData.worthOtherCoinCount < amount && !coinData.isHidden && smallCoinData.chain.contentEquals(coinData.chain))
    		{
    			largeCoin = coinData.coinItem;
    			amount = coinData.worthOtherCoinCount;
    		}
    	}
    	if(largeCoin != null)
    		return new Pair<>(largeCoin, amount);
    	return null;
    }
    
    public static Pair<Item,Integer> getDownwardConversion(Item coinItem)
    {
    	if(moneyData == null)
    		return null;
    	CoinData data = moneyData.getData(coinItem);
    	if(data == null)
    		return null;
    	if(data.convertsDownwards())
    		return data.getDownwardConversion();
    	return null;
    }
    
    public static long displayValueToLong(double displayValue)
    {
    	long baseCoinValue = getValue(Config.SERVER.valueBaseCoin.get());
    	double totalValue = displayValue * baseCoinValue;
    	long value = (long)totalValue;
    	return totalValue % 1d >= 0.5d ? value + 1 : value;
    }
    
    public static CoinValue displayValueToCoinValue(double displayValue)
    {
    	return new CoinValue(displayValueToLong(displayValue));
    }
    
}
