package io.github.lightman314.lightmanscurrency.api.money.coins;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.events.BuildDefaultMoneyDataEvent;
import io.github.lightman314.lightmanscurrency.api.events.ChainDataReloadedEvent;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.ATMAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.coin.CoinEntry;
import io.github.lightman314.lightmanscurrency.api.money.coins.display.ValueDisplayAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.display.builtin.CoinDisplay;
import io.github.lightman314.lightmanscurrency.api.money.coins.old_compat.OldCoinData;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.data.ATMData;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.data.ATMExchangeButtonData;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.WalletCapability;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.data.SPacketSyncCoinData;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * Utility class with Coin-Related data and functions.
 * Use {@link MoneyAPI} for more generic Money-Related functions that aren't coin-specific.
 */
public final class CoinAPI {

    private CoinAPI() {}

    public static final Comparator<ItemStack> COIN_SORTER = new CoinSorter();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static final String MONEY_FILE_LOCATION = "config/lightmanscurrency/MasterCoinList.json";
    public static final String MAIN_CHAIN = "main";

    private static Map<String, ChainData> LOADED_CHAINS = null;

    public static boolean DataNotReady() { return LOADED_CHAINS == null; }

    private static boolean setup = false;

    public static void Setup()
    {
        if(setup)
            return;
        setup = true;
        MinecraftForge.EVENT_BUS.addListener(CoinAPI::onServerStart);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, CoinAPI::onJoinServer);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, CoinAPI::generateDefaultCoins);
        ValueDisplayAPI.Setup();
        ATMAPI.Setup();
    }

    public static void reloadMoneyDataFromFile()
    {
        LightmansCurrency.LogInfo("Reloading Money Data");
        File mcl = new File(MONEY_FILE_LOCATION);
        if(!mcl.exists())
        {
            LightmansCurrency.LogInfo("MasterCoinList.json does not exist. Creating a fresh copy.");
            createMoneyDataFile(mcl,generateDefaultMoneyData());
        }
        try {
            JsonObject fileData = GsonHelper.parse(Files.readString(mcl.toPath()));
            if(fileData.has("CoinEntries") && !fileData.has("Chains"))
                loadDeprecatedData(GsonHelper.getAsJsonArray(fileData, "CoinEntries"));
            else
                loadMoneyDataFromJson(fileData);
        } catch (JsonSyntaxException | ResourceLocationException | IOException e) {
            LightmansCurrency.LogError("Error loading the Master Coin List. Using default values for now.", e);
            loadData(generateDefaultMoneyData());
        }
        syncDataWith(PacketDistributor.ALL.noArg());
    }

    private static void loadData(@Nonnull Map<String,ChainData> dataMap)
    {
        ChainDataReloadedEvent.Pre event = new ChainDataReloadedEvent.Pre(dataMap);
        MinecraftForge.EVENT_BUS.post(event);
        LOADED_CHAINS = event.getChainMap();
        MinecraftForge.EVENT_BUS.post(new ChainDataReloadedEvent.Post(LOADED_CHAINS));
    }

    @SuppressWarnings("deprecation")
    private static void loadDeprecatedData(@Nonnull JsonArray oldArray) throws JsonSyntaxException, ResourceLocationException
    {
        Map<String, List<OldCoinData>> oldData = new HashMap<>();
        for(int i = 0; i < oldArray.size(); ++i)
        {
            try {
                OldCoinData data  = OldCoinData.parse(GsonHelper.convertToJsonObject(oldArray.get(i), "CoinEntries[" + i + "]"));
                if(oldData.containsKey(data.chain))
                    oldData.get(data.chain).add(data);
                else
                {
                    List<OldCoinData> list = new ArrayList<>();
                    list.add(data);
                    oldData.put(data.chain, list);
                }
            } catch (JsonSyntaxException | ResourceLocationException e) { LightmansCurrency.LogError("Error parsing CoinEntries[" + i + "] entry!", e); }
        }
        Map<String,ChainData> tempMap = new HashMap<>();
        oldData.forEach((chain,dataList) -> {
            //Create a builder
            ChainData.Builder builder = ChainData.builder(chain, EasyText.translatable("lightmanscurrency.money.chain." + chain));
            //Copy old initial and plural values into the new Value Display system
            //Assuming coin display as the default display type
            CoinDisplay.Builder displayBuilder = CoinDisplay.builder();
            for(OldCoinData data : dataList)
            {
                Component initial = data.initialTranslation != null ? EasyText.translatable(data.initialTranslation) : null;
                Component plural = data.pluralTranslation != null ? EasyText.translatable(data.pluralTranslation) : null;
                if(initial != null || plural != null)
                    displayBuilder.defineFor(data.coinItem, initial, plural);
            }
            builder.withDisplay(displayBuilder.build());
            //First step, find the "root" coin
            OldCoinData rootCoin = null;
            for(OldCoinData data : dataList)
            {
                if(data.worthOtherCoin == null && !data.isHidden)
                {
                    rootCoin = data;
                    break;
                }
            }
            //Remove this coin entry
            dataList.remove(rootCoin);
            //Collect the entire core chain
            ChainData.Builder.ChainBuilder coreChain = builder.withCoreChain(rootCoin.coinItem);
            OldCoinData nextCoin = findNextInChain(dataList, rootCoin.coinItem, true);
            while(nextCoin != null)
            {
                coreChain.withCoin(nextCoin.coinItem, nextCoin.worthOtherCoinCount);
                dataList.remove(nextCoin);
                nextCoin = findNextInChain(dataList, nextCoin.coinItem, true);
            }
            //Collect all the side chains
            for(CoinEntry entry : coreChain.getEntries())
            {
                OldCoinData sideChainRoot = findNextInChain(dataList, entry.getCoin(), false);
                while(sideChainRoot != null)
                {
                    try {
                        dataList.remove(sideChainRoot);
                        ChainData.Builder.ChainBuilder sideChain = builder.withSideChain(sideChainRoot.coinItem, sideChainRoot.worthOtherCoinCount, entry.getCoin());
                        nextCoin = findNextInChain(dataList, sideChainRoot.coinItem, false);
                        while(nextCoin != null)
                        {
                            sideChain.withCoin(nextCoin.coinItem, nextCoin.worthOtherCoinCount);
                            dataList.remove(nextCoin);
                            nextCoin = findNextInChain(dataList, nextCoin.coinItem, false);
                        }
                    } catch (IllegalArgumentException ignored) {}
                    sideChainRoot = findNextInChain(dataList, entry.getCoin(), false);
                }
            }
            if(!dataList.isEmpty())
                LightmansCurrency.LogWarning("Old MasterCoinList data could not be fully converted, likely due to multiple chain splits in a 'hidden' side-chain.");

            //Load old ATMData for the Main Chain
            if(chain.equals(CoinAPI.MAIN_CHAIN))
                ATMData.parseDeprecated(builder);

            tempMap.put(chain, builder.build());
        });
        if(tempMap.isEmpty())
            throw new JsonSyntaxException("No valid chains could be converted to the new system!");

        loadData(tempMap);
        LightmansCurrency.LogInfo("Old MasterCoinList data successfully converted to the new system! Replacing the old MasterCoinList.json file with the updated data.");
        createMoneyDataFile(new File(MONEY_FILE_LOCATION), LOADED_CHAINS);
    }

    @SuppressWarnings("deprecation")
    private static OldCoinData findNextInChain(@Nonnull List<OldCoinData> dataList, @Nonnull Item coin, boolean coreChainOnly)
    {
        for(OldCoinData data : dataList)
        {
            if(data.worthOtherCoin == coin)
            {
                if(coreChainOnly && data.isHidden)
                    continue;
                return data;
            }
        }
        return null;
    }

    private static void loadMoneyDataFromJson(@Nonnull JsonObject root) throws JsonSyntaxException, ResourceLocationException
    {
        List<CoinEntry> allEntries = new ArrayList<>();
        Map<String, ChainData> tempMap = new HashMap<>();
        JsonArray chainList = GsonHelper.getAsJsonArray(root, "Chains");
        for(int i = 0; i < chainList.size(); ++i)
        {
            String chainName = "UNDEFINED";
            try {
                JsonObject entry = GsonHelper.convertToJsonObject(chainList.get(i), "Chains[" + i + "]");
                chainName = GsonHelper.getAsString(entry, "chain", null);
                ChainData chain = ChainData.fromJson(allEntries, entry);
                if(tempMap.containsKey(chain.chain))
                    throw new JsonSyntaxException("Multple '" + chain.chain  + "' chains detected. Duplicate will be ignored!");
                tempMap.put(chain.chain, chain);
            } catch(JsonSyntaxException | ResourceLocationException e) { LightmansCurrency.LogError("Error loading Chain[" + i + "] (" + chainName + ") from the Master Coin List!", e); }
        }
        if(tempMap.isEmpty())
            throw new JsonSyntaxException("No valid coin chains were registered!");
        if(!tempMap.containsKey(MAIN_CHAIN))
            throw new JsonSyntaxException("At least 1 chain named 'main' must be present!");
        loadData(tempMap);
    }

    private static void createMoneyDataFile(@Nonnull File mcl, @Nonnull Map<String,ChainData> data)
    {
        File dir = new File(mcl.getParent());
        if(!dir.exists())
            dir.mkdirs();
        if(dir.exists())
        {
            try {

                mcl.createNewFile();

                @Nonnull JsonObject json = getDataJson(data);

                FileUtil.writeStringToFile(mcl, GSON.toJson(json));

            } catch(IOException e) { LightmansCurrency.LogError("Error attempting to createTrue 'MasterCoinList.json' file.", e); }
        }
    }

    @Nonnull
    private static Map<String,ChainData> generateDefaultMoneyData()
    {
        BuildDefaultMoneyDataEvent event = new BuildDefaultMoneyDataEvent();
        try { MinecraftForge.EVENT_BUS.post(event);
        } catch(RuntimeException e) { LightmansCurrency.LogError("Error generating default money data!", e); return new HashMap<>(); }

        Map<String,ChainData> results = new HashMap<>();
        event.getFinalResult().forEach((chain,builder) -> results.put(chain, builder.build()));
        return results;
    }

    //Flag as highest priority so that other mods can more easily modify my default values
    private static void generateDefaultCoins(BuildDefaultMoneyDataEvent event)
    {
        ChainData.builder(CoinAPI.MAIN_CHAIN, EasyText.translatable("lightmanscurrency.money.chain.main"))
                .withCoreChain(ModItems.COIN_COPPER)
                    .withCoin(ModItems.COIN_IRON, 10)
                    .withCoin(ModItems.COIN_GOLD, 10)
                    .withCoin(ModItems.COIN_EMERALD, 10)
                    .withCoin(ModItems.COIN_DIAMOND, 10)
                    .withCoin(ModItems.COIN_NETHERITE,10)
                    .back()
                .withSideChain(ModBlocks.COINPILE_COPPER, 9, ModItems.COIN_COPPER)
                    .withCoin(ModBlocks.COINBLOCK_COPPER, 4).back()
                .withSideChain(ModBlocks.COINPILE_IRON, 9, ModItems.COIN_IRON)
                    .withCoin(ModBlocks.COINBLOCK_IRON, 4).back()
                .withSideChain(ModBlocks.COINPILE_GOLD, 9, ModItems.COIN_GOLD)
                    .withCoin(ModBlocks.COINBLOCK_GOLD, 4).back()
                .withSideChain(ModBlocks.COINPILE_EMERALD, 9, ModItems.COIN_EMERALD)
                    .withCoin(ModBlocks.COINBLOCK_EMERALD, 4).back()
                .withSideChain(ModBlocks.COINPILE_DIAMOND, 9, ModItems.COIN_DIAMOND)
                    .withCoin(ModBlocks.COINBLOCK_DIAMOND, 4).back()
                .withSideChain(ModBlocks.COINPILE_NETHERITE, 9, ModItems.COIN_NETHERITE)
                    .withCoin(ModBlocks.COINBLOCK_NETHERITE, 4).back()
                .withDisplay(CoinDisplay.easyDefine())
                .atmBuilder().accept(ATMExchangeButtonData::generateMain).back()
                .apply(event,true); //Override any existing chains with this id, as they shouldn't be replacing the main chain on this priority level
    }

    @Nonnull
    private static JsonObject getDataJson(@Nonnull Map<String,ChainData> data)
    {
        JsonObject fileJson = new JsonObject();
        JsonArray chainArray = new JsonArray();

        for(ChainData chain : data.values())
            chainArray.add(chain.getAsJson());

        fileJson.add("Chains", chainArray);
        return fileJson;
    }

    /**
     * Easy public access to the equipped wallet.
     * Also confirms that the equipped wallet is either empty or a valid WalletItem.
     * Returns an empty stack if no wallet is equipped, or if the equipped item is not a valid wallet.
     */
    @Nonnull
    public static ItemStack getWalletStack(@Nonnull Player player)
    {
        ItemStack wallet = ItemStack.EMPTY;
        IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(player);
        if(walletHandler != null)
            wallet = walletHandler.getWallet();
        //Safety check to confirm that the Item Stack found is a valid wallet
        if(!WalletItem.validWalletStack(wallet))
        {
            LightmansCurrency.LogDebug(player.getName().getString() + "'s equipped wallet is not a valid WalletItem.");
            LightmansCurrency.LogDebug("Equipped wallet is of type " + wallet.getItem().getClass().getName());
            return ItemStack.EMPTY;
        }
        return wallet;
    }

    @Nullable
    public static ChainData getChainData(@Nonnull String chain)
    {
        if(DataNotReady())
            return null;
        return  LOADED_CHAINS.get(chain);
    }

    @Nonnull
    public static List<ChainData> getAllChainData() {
        if(DataNotReady()) //Return empty list if chain data is not yet loaded.
            return ImmutableList.of();
        return ImmutableList.copyOf(LOADED_CHAINS.values()); }

    @Nullable
    public static ChainData chainForCoin(@Nonnull ItemStack coin) { return chainForCoin(coin.getItem()); }
    public static ChainData chainForCoin(@Nonnull Item coin)
    {
        if(DataNotReady())
            return null;
        for(ChainData chain : getAllChainData())
        {
            if(chain.containsEntry(coin))
                return chain;
        }
        return null;
    }

    /**
     * Whether the given item is a valid coin.
     * @param allowSideChains Whether coins from side chains (e.g. decorative Coin Piles or Coin Blocks) are acceptable.
     */
    public static boolean isCoin(@Nonnull ItemStack coin, boolean allowSideChains) { return isCoin(coin.getItem(), allowSideChains); }
    public static boolean isCoin(@Nonnull Item coin, boolean allowSideChains)
    {
        if(coin == Items.AIR)
            return false;
        ChainData chainData = chainForCoin(coin);
        if(chainData != null)
        {
            if(allowSideChains)
                return true;
            CoinEntry entry = chainData.findEntry(coin);
            if(entry != null)
                return !entry.isSideChain();
            return false;
        }
        return false;
    }

    /**
     * Exchanges all coins in the container to as large of a coin as humanly possible
     */
    public static void ExchangeAllCoinsUp(@Nonnull Container container)
    {
        if(DataNotReady())
            return;
        for(ChainData chain : getAllChainData())
        {
            List<CoinEntry> entryList = chain.getAllEntries(false, ChainData.SORT_LOWEST_VALUE_FIRST);
            for(CoinEntry entry : entryList)
                ExchangeCoinsUp(container, entry.getCoin());
            for(CoinEntry entry : entryList)
                ExchangeCoinsUp(container, entry.getCoin());
        }
    }

    /**
     * Exchanges as many of the small coin that it can for its next largest coin
     */
    public static void ExchangeCoinsUp(@Nonnull Container container, @Nonnull Item smallCoin)
    {
        if(DataNotReady())
            return;
        ChainData chain = chainForCoin(smallCoin);
        if(chain == null)
            return;
        //Get next-higher coin data
        Pair<CoinEntry,Integer> upperExchange = chain.getUpperExchange(smallCoin);
        if(upperExchange == null)
            return;
        Item largeCoin = upperExchange.getFirst().getCoin();
        int smallCoinCount = upperExchange.getSecond();
        while(InventoryUtil.GetItemCount(container,smallCoin) >= smallCoinCount)
        {
            //Remove the smaller coins
            InventoryUtil.RemoveItemCount(container, smallCoin, smallCoinCount);
            //Put the new coin into the inventory
            ItemStack newCoinStack = new ItemStack(largeCoin,1);
            if(!InventoryUtil.PutItemStack(container, newCoinStack))
            {
                //Could not merge the inventory. Re-add the smaller coins & break the loop
                InventoryUtil.TryPutItemStack(container, new ItemStack(smallCoin, smallCoinCount));
                return;
            }
        }
    }

    public static void ExchangeAllCoinsDown(@Nonnull Container container)
    {
        if(DataNotReady())
            return;
        for(ChainData chain : getAllChainData())
        {
            List<CoinEntry> entryList = chain.getAllEntries(false, ChainData.SORT_LOWEST_VALUE_FIRST);
            for(CoinEntry entry : entryList)
                ExchangeCoinsDown(container, entry.getCoin());
            for(CoinEntry entry : entryList)
                ExchangeCoinsDown(container, entry.getCoin());
        }
    }

    public static void ExchangeCoinsDown(@Nonnull Container container, @Nonnull Item largeCoin)
    {
        if(DataNotReady())
            return;
        ChainData chain = chainForCoin(largeCoin);
        if(chain == null)
            return;
        //Get next-lower coin data
        Pair<CoinEntry,Integer> lowerExchange = chain.getLowerExchange(largeCoin);
        if(lowerExchange == null)
            return;
        Item smallCoin = lowerExchange.getFirst().getCoin();
        int smallCoinCount = lowerExchange.getSecond();
        while(InventoryUtil.GetItemCount(container,largeCoin) > 0)
        {
            //Remove the large coin
            InventoryUtil.RemoveItemCount(container, largeCoin, 1);
            //Merge the new coins into the container
            ItemStack newCoinStack = new ItemStack(smallCoin, smallCoinCount);
            if(!InventoryUtil.PutItemStack(container, newCoinStack))
            {
                //Could not merge the inventory. Re-add the large coin & break the loop;
                InventoryUtil.TryPutItemStack(container, new ItemStack(largeCoin, 1));
                return;
            }
        }
    }

    public static void SortCoins(@Nonnull Container container)
    {
        //Merge like stacks
        InventoryUtil.MergeStacks(container);

        //Collect a list of all items in the container
        List<ItemStack> oldInventory = new ArrayList<>();
        for(int i = 0; i < container.getContainerSize(); ++i)
        {
            if(!container.getItem(i).isEmpty())
                oldInventory.add(container.getItem(i));
        }
        container.clearContent();

        //Sort the item list using a comparator
        oldInventory.sort(COIN_SORTER);

        //Re-add the items to the container
        int index = 0;
        while(oldInventory.size() > 0)
        {
            container.setItem(index++, oldInventory.get(0));
            oldInventory.remove(0);
        }
    }

    //Reload
    private static void onServerStart(@Nonnull ServerAboutToStartEvent event) { reloadMoneyDataFromFile(); }
    private static void onJoinServer(@Nonnull PlayerEvent.PlayerLoggedInEvent event)
    {
        //Force data reload if somehow not yet loaded at this point.
        if(LOADED_CHAINS == null)
            reloadMoneyDataFromFile();
        syncDataWith(LightmansCurrencyPacketHandler.getTarget(event.getEntity()));
    }
    public static void syncDataWith(@Nonnull PacketDistributor.PacketTarget target) { new SPacketSyncCoinData(getDataJson(LOADED_CHAINS)).sendToTarget(target); }
    public static void handleSyncPacket(@Nonnull SPacketSyncCoinData packet) { loadMoneyDataFromJson(packet.getJson()); }

    private static class CoinSorter implements Comparator<ItemStack>
    {
        @Override
        public int compare(ItemStack stack1, ItemStack stack2) {
            //Sort by count if they're the same item
            if(stack1.getItem() == stack2.getItem())
                return Integer.compare(stack2.getCount(), stack1.getCount());

            //Start comparing chains
            ChainData chain1 = chainForCoin(stack1);
            ChainData chain2 = chainForCoin(stack2);
            if(chain1 == null && chain2 == null)
                return 0;
            if(chain2 == null)
                return 1;
            if(chain1 == null)
                return -1;
            if(chain1 != chain2) //Sort by chain name
                return chain2.getDisplayName().getString().compareToIgnoreCase(chain1.getDisplayName().getString());

            //Sort by individual value
            CoinEntry entry1 = chain1.findEntry(stack1);
            CoinEntry entry2 = chain2.findEntry(stack2);
            return Long.compare(entry2.getCoreValue(), entry1.getCoreValue());
        }
    }

}
