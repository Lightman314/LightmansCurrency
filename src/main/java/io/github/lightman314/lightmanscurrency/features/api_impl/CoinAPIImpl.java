package io.github.lightman314.lightmanscurrency.features.api_impl;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.coins.ICoinLike;
import io.github.lightman314.lightmanscurrency.api.coins.atm.ATMExchangeButtonData;
import io.github.lightman314.lightmanscurrency.api.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.coins.data.CoinInputType;
import io.github.lightman314.lightmanscurrency.api.coins.data.coin.CoinEntry;
import io.github.lightman314.lightmanscurrency.api.coins.display.builtin.CoinDisplay;
import io.github.lightman314.lightmanscurrency.api.coins.display.builtin.NumberDisplay;
import io.github.lightman314.lightmanscurrency.api.coins.events.BuildDefaultCoinDataEvent;
import io.github.lightman314.lightmanscurrency.api.coins.events.ChainDataReloadedEvent;
import io.github.lightman314.lightmanscurrency.api.helpers.FileHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.JsonHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.ResourceHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import io.github.lightman314.lightmanscurrency.core.LCBlocks;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import io.github.lightman314.lightmanscurrency.network.message.data.SPacketSyncCoinData;
import net.minecraft.IdentifierException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

@EventBusSubscriber
public final class CoinAPIImpl implements CoinAPI {

    public static final CoinAPIImpl INSTANCE = new CoinAPIImpl();

    private final Comparator<ItemStack> coinSorter = new CoinSorter();

    Map<String, ChainData> loadedChains = null;
    Map<Identifier,ChainData> itemIdToChainMap = null;
    final List<Comparator<ItemStack>> customSorters = new ArrayList<>();
    final List<BiPredicate<ItemStack,Boolean>> customCoinContainerFilters = new ArrayList<>();

    private CoinAPIImpl() {
        this.customCoinContainerFilters.add(this::isCoin);
        this.customCoinContainerFilters.add((stack,side) -> {
            if(stack.getItem() instanceof ICoinLike coin)
                return coin.isCoin(stack) && (side || !coin.isFromSideChain(stack));
            return false;
        });
    }

    @Override
    public boolean isDataLoaded() { return this.loadedChains != null && this.itemIdToChainMap != null; }

    @Override
    public void reloadCoinData(HolderLookup.Provider registryAccess, boolean sync) {
        LightmansCurrency.LogInfo("Reloading Coin Data");
        //Clear the old data
        this.loadedChains = null;
        this.itemIdToChainMap = null;

        File folder = new File(DATA_LOCATION);
        DataContext<JsonElement> context = DataContext.createJson(registryAccess);
        //Locate all files in the folder that end with ".json"
        //Ignore sub-folders
        File[] array = folder.listFiles((dir, name) -> dir.equals(folder) && name.endsWith(".json"));
        List<File> files = array == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(array));
        if(files.isEmpty())
        {
            LightmansCurrency.LogInfo("No entries found in '" + DATA_LOCATION + "'. Generating new files!");
            this.loadData(this.generateDefaultCoinData(),false);
            this.createMoneyDataFiles(this.loadedChains,context);
        }
        else
        {
            Map<String,ChainData> temp = new HashMap<>();
            List<CoinEntry> allEntries = new ArrayList<>();
            //Load the "main" chain first
            for(File file : new ArrayList<>(files))
            {
                String chain = extractChainID(file);
                if(chain.equals(DEFAULT_CHAIN))
                {
                    if(!safeLoadData(file,temp,allEntries,context)) {
                        //If the data failed to load, then we should forcibly add the "main" chain from the default values
                        LightmansCurrency.LogError("Error loading the '" + DEFAULT_CHAIN + "' coin data entry. Forcibly adding the default chain to the data entries.");
                        Map<String,ChainData> defaultData = this.generateDefaultCoinData();
                        if(!defaultData.containsKey(DEFAULT_CHAIN))
                            throw new IllegalStateException("Somehow the default coin data does not contain the default chain!");
                        ChainData c = defaultData.get(DEFAULT_CHAIN);
                        temp.put(DEFAULT_CHAIN,c);
                        allEntries.addAll(c.getAllEntries(true));
                    }
                    //And now we shall ignore the main chain
                    files.remove(file);
                    break;
                }
            }
            //Load the remaining chains
            for(File file : files)
                safeLoadData(file,temp,allEntries,context);
            this.loadData(temp,true);
        }
        //Send sync packet
        if(sync)
            this.syncCoinDataWith(null);
    }

    private void loadData(Map<String,ChainData> dataMap,boolean postEvent)
    {
        if(postEvent)
        {
            //Post the event when loading on the logical server
            ChainDataReloadedEvent.Pre event = new ChainDataReloadedEvent.Pre(dataMap);
            NeoForge.EVENT_BUS.post(event);
            this.loadedChains = event.getChainMap();
        }
        else //Special override when receiving data from a packet
            this.loadedChains = ImmutableMap.copyOf(dataMap);
        Map<Identifier,ChainData> temp = new HashMap<>();
        //Store chain data in an item to chain map so that we don't have to manually search through lists for matching entries
        for(ChainData chain : this.loadedChains.values())
        {
            for(CoinEntry entry : chain.getAllEntries(true))
            {
                Identifier coinID = BuiltInRegistries.ITEM.getKey(entry.getCoin());
                temp.put(coinID,chain);
            }
        }
        this.itemIdToChainMap = ImmutableMap.copyOf(temp);
        if(postEvent)
            NeoForge.EVENT_BUS.post(new ChainDataReloadedEvent.Post(this.loadedChains));
    }

    private static String extractChainID(File file)
    {
        String path = file.getAbsolutePath();
        LightmansCurrency.LogInfo("Attempting to get chain id of " + path);
        int lastSeperator = path.lastIndexOf(File.separatorChar);
        String result = path.substring(lastSeperator + 1,path.length() - 5);
        LightmansCurrency.LogDebug("Extracted chain id of '" + result + "' from '" + path + "'");
        return result;
    }

    private static boolean safeLoadData(File file,Map<String,ChainData> map,List<CoinEntry> allEntries, DataContext<JsonElement> context)
    {
        try {
            String chainName = extractChainID(file);
            if(map.containsKey(chainName))
                throw new IOException("Somehow loaded two chains with the chain id of '" + chainName + "'");
            JsonObject json = GsonHelper.parse(Files.readString(file.toPath()));
            ChainData chain = ChainData.fromJson(chainName,allEntries,json,context);
            map.put(chainName,chain);
            //Add all the successfully loaded entries to the data
            allEntries.addAll(chain.getAllEntries(true));
            return true;
        } catch (JsonParseException | IdentifierException | IOException e) {
            LightmansCurrency.LogError("Error loading coin data from " + file.getPath() + "!",e);
            return false;
        }
    }

    private void createMoneyDataFiles(Map<String,ChainData> allChains,DataContext<JsonElement> context)
    {
        for(ChainData chain : allChains.values())
            this.createMoneyDataFile(chain,context);
    }

    private void createMoneyDataFile(ChainData chain,DataContext<JsonElement> context)
    {
        File file = new File(DATA_LOCATION + File.separatorChar + chain.chain + ".json");
        File dir = new File(file.getParent());
        if(!dir.exists())
            dir.mkdirs();
        if(dir.exists())
        {
            try {
                file.createNewFile();
                JsonObject json = chain.getAsJson(context);
                FileHelper.writeStringToFile(file,JsonHelper.PRETTY_GSON.toJson(json));
            } catch (IOException e) { LightmansCurrency.LogError("Error attmpting to create '" + file.getPath() + " file.",e); }
        }
    }

    private Map<String,ChainData> generateDefaultCoinData()
    {
        BuildDefaultCoinDataEvent event = new BuildDefaultCoinDataEvent();
        try { NeoForge.EVENT_BUS.post(event);
        } catch (RuntimeException e) { LightmansCurrency.LogError("Error generating default money data!",e); return new HashMap<>(); }
        Map<String,ChainData> results = new HashMap<>();
        event.getFinalResult().forEach((chain,builder) -> results.put(chain,builder.build()));
        return results;
    }

    //Flag as highest priority so that other mods can more easily modify my default values
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    private static void generateDefaultCoins(BuildDefaultCoinDataEvent event)
    {
        ChainData.builder(CoinAPI.DEFAULT_CHAIN,LCText.Coins.COIN_CHAIN_MAIN)
                .withCoreChain(LCItems.COIN_COPPER)
                .withCoin(LCItems.COIN_IRON, 10)
                .withCoin(LCItems.COIN_GOLD, 10)
                .withCoin(LCItems.COIN_EMERALD, 10)
                .withCoin(LCItems.COIN_DIAMOND, 10)
                .withCoin(LCItems.COIN_NETHERITE,10)
                .back()
                .withSideChain(LCBlocks.COIN_PILE_COPPER, 9, LCItems.COIN_COPPER)
                .withCoin(LCBlocks.COIN_BLOCK_COPPER, 4).back()
                .withSideChain(LCBlocks.COIN_PILE_IRON, 9, LCItems.COIN_IRON)
                .withCoin(LCBlocks.COIN_BLOCK_IRON, 4).back()
                .withSideChain(LCBlocks.COIN_PILE_GOLD, 9, LCItems.COIN_GOLD)
                .withCoin(LCBlocks.COIN_BLOCK_GOLD, 4).back()
                .withSideChain(LCBlocks.COIN_PILE_EMERALD, 9, LCItems.COIN_EMERALD)
                .withCoin(LCBlocks.COIN_BLOCK_EMERALD, 4).back()
                .withSideChain(LCBlocks.COIN_PILE_DIAMOND, 9, LCItems.COIN_DIAMOND)
                .withCoin(LCBlocks.COIN_BLOCK_DIAMOND, 4).back()
                .withSideChain(LCBlocks.COIN_PILE_NETHERITE, 9, LCItems.COIN_NETHERITE)
                .withCoin(LCBlocks.COIN_BLOCK_NETHERITE, 4).back()
                .withDisplay(CoinDisplay.easyDefine())
                .atmBuilder().accept(ATMExchangeButtonData::generateMain).back()
                .apply(event,true); //Override any existing chains with this id, as they shouldn't be replacing the main chain on this priority level

        //Emerald Coin Chain
        ChainData.builder("emeralds",LCText.Coins.COIN_CHAIN_EMERALDS)
                .withCoreChain(Items.EMERALD).withCoin(Items.EMERALD_BLOCK,9).back()
                .withInputType(CoinInputType.DEFAULT)
                .withDisplay(new NumberDisplay(LCText.Coins.COIN_CHAIN_EMERALDS_DISPLAY,LCText.Coins.COIN_CHAIN_EMERALDS_DISPLAY_WORDY, Items.EMERALD))
                .apply(event, true);
    }

    @Override
    public ItemStack getEquippedWallet(Entity player) {
        //TODO
        return ItemStack.EMPTY;
    }

    @Override
    public Collection<ChainData> lookupAllChains() {
        if(this.loadedChains == null)
            return Collections.EMPTY_LIST;
        return this.loadedChains.values();
    }

    @Nullable
    @Override
    public ChainData lookupChain(String chain) {
        if(this.loadedChains == null)
            return null;
        return this.loadedChains.get(chain);
    }

    @Nullable
    @Override
    public ChainData lookupChain(Item coin) {
        if(this.itemIdToChainMap == null)
            return null;
        return this.itemIdToChainMap.get(BuiltInRegistries.ITEM.getKey(coin));
    }

    @Override
    public boolean isCoin(Item coin, boolean allowSideChains) {
        if(coin == Items.AIR)
            return false;
        ChainData chain = this.lookupChain(coin);
        if(chain != null)
        {
            if(allowSideChains)
                return true;
            CoinEntry entry = chain.findEntry(coin);
            if(entry != null)
                return !entry.isSideChain();
            return false;
        }
        return false;
    }

    @Override
    public void registerCoinContainerFilter(BiPredicate<ItemStack, Boolean> filter) {
        if(!this.customCoinContainerFilters.contains(filter))
            this.customCoinContainerFilters.add(filter);
    }

    @Override
    public boolean isAllowedInCoinContainer(ItemStack coin, boolean allowSideChains) {
        for(BiPredicate<ItemStack,Boolean> filter : new ArrayList<>(this.customCoinContainerFilters))
        {
            if(filter.test(coin,allowSideChains))
                return true;
        }
        return false;
    }

    @Override
    public void exchangeCoinsAllUp(ResourceHandler<ItemResource> container,@Nullable Transaction transaction) {
        if(this.dataNotLoaded())
            return;
        try (Transaction tx = Transaction.open(transaction)) {
            for (ChainData chain : this.lookupAllChains())
            {
                List<CoinEntry> entryList = chain.getAllEntries(false,ChainData.SORT_LOWEST_VALUE_FIRST);
                for(CoinEntry entry : entryList)
                    this.exchangeCoinsUpInternal(container,entry,tx);
            }
            tx.commit();
        }
    }

    @Override
    public boolean exchangeCoinsUp(ResourceHandler<ItemResource> container,Item smallCoin,@Nullable Transaction transaction) {
        if(this.dataNotLoaded())
            return false;
        ChainData chain = this.lookupChain(smallCoin);
        if(chain != null)
        {
            CoinEntry entry = chain.findEntry(smallCoin);
            if(entry != null)
            {
                this.exchangeCoinsUpInternal(container,entry,transaction);
                return true;
            }
        }
        return false;
    }

    private void exchangeCoinsUpInternal(ResourceHandler<ItemResource> container, CoinEntry entry, @Nullable Transaction transaction)
    {
        //Get the next-higher coin data
        Pair<CoinEntry,Integer> upperExchange = entry.getUpperExchange();
        if(upperExchange == null)
            return;
        Item smallCoin = entry.getCoin();
        Item largeCoin = upperExchange.getFirst().getCoin();
        int smallCoinCount = upperExchange.getSecond();
        Predicate<ItemResource> filter = i -> i.is(smallCoin);
        while(ResourceHelper.getResourceCount(container,filter,transaction) >= smallCoinCount)
        {
            ItemStack stack = new ItemStack(entry.getCoin(),smallCoinCount);
            boolean success = false;
            try(Transaction tx = Transaction.open(transaction)) {
                //Remove the smaller coins
                if(ResourceHelper.extractFirstToTarget(container,filter,smallCoinCount,tx).totalCount() == smallCoinCount)
                {
                    //Insert the large coin
                    if(ResourceHandlerUtil.insertStacking(container,ItemResource.of(largeCoin),1,tx) == 1)
                    {
                        //Commit the exchange if both transactions went through
                        tx.commit();
                        success = true;
                    }
                }
            }
            //Forcibly break the loop if we failed to exchange the coins
            if(!success)
                return;
        }
    }

    @Override
    public void exchangeCoinsAllDown(ResourceHandler<ItemResource> container,@Nullable Transaction transaction) {
        if(this.dataNotLoaded())
            return;
        try(Transaction tx = Transaction.open(transaction))
        {
            for(ChainData chain : this.lookupAllChains())
            {
                List<CoinEntry> entryList = chain.getAllEntries(false,ChainData.SORT_HIGHEST_VALUE_FIRST);
                for(int loop = 0; loop < 2; loop++)
                {
                    for(CoinEntry entry : entryList)
                        this.exchangeCoinsDownInternal(container,entry,tx);
                }
            }
            tx.commit();
        }
    }

    @Override
    public boolean exchangeCoinsDown(ResourceHandler<ItemResource> container,Item largeCoin,@Nullable Transaction transaction) {
        if(this.dataNotLoaded())
            return false;
        ChainData chain = this.lookupChain(largeCoin);
        if(chain != null)
        {
            CoinEntry entry = chain.findEntry(largeCoin);
            if(entry != null)
            {
                this.exchangeCoinsDownInternal(container,entry,transaction);
                return true;
            }
        }
        return false;
    }

    private void exchangeCoinsDownInternal(ResourceHandler<ItemResource> container, CoinEntry entry, @Nullable Transaction transaction) {
        Pair<CoinEntry,Integer> lowerExchange = entry.getLowerExchange();
        if(lowerExchange == null)
            return;
        Item largeCoin = entry.getCoin();
        Item smallCoin = lowerExchange.getFirst().getCoin();
        int smallCoinCount = lowerExchange.getSecond();
        Predicate<ItemResource> filter = i -> i.is(largeCoin);
        while(ResourceHandlerUtil.findExtractableResource(container,filter,transaction) != null)
        {
            boolean success = false;
            try(Transaction tx = Transaction.open(transaction)) {
                //Remove the large coin
                ResourceStack<ItemResource> result = ResourceHandlerUtil.extractFirst(container,filter,1,tx);
                if(result != null && result.amount() == 1)
                {
                    //Insert the small coins
                    if(ResourceHandlerUtil.insertStacking(container,ItemResource.of(smallCoin),smallCoinCount,tx) == smallCoinCount)
                    {
                        tx.commit();
                        success = true;
                    }
                }
            }
            //Abort if we failed the exchange
            if(!success)
                return;
        }
    }

    @Override
    public Comparator<ItemStack> getCoinSorter() { return this.coinSorter; }

    @Override
    public void registerCustomSorter(Comparator<ItemStack> sorter) {
        if(!this.customSorters.contains(sorter))
            this.customSorters.add(sorter);
    }

    @Override
    public void sortCoinsByValue(ResourceHandler<ItemResource> container,@Nullable Transaction transaction) {
        try(Transaction tx = Transaction.open(transaction)) {
            //Collect a list of all items in the item resource handler
            List<ItemStack> oldItems = new ArrayList<>();
            for(int i = 0; i < container.size(); ++i)
            {
                ItemResource resource = container.getResource(i);
                if(resource.isEmpty())
                    continue;
                int extracted = container.extract(i,container.getResource(i),Integer.MAX_VALUE,tx);
                if(extracted > 0)
                    oldItems.add(resource.toStack(extracted));
            }
            //Combine like-stacks
            oldItems = ItemHelper.combineStacks(oldItems);
            //Sort the item list using the sorter
            oldItems.sort(this.getCoinSorter());

            //Re-add the items to the container
            while(!oldItems.isEmpty())
            {
                ItemStack stack = oldItems.getFirst();
                if(container.insert(ItemResource.of(stack),stack.getCount(),tx) != stack.getCount())
                {
                    LightmansCurrency.LogWarning("Failed to re-insert the sorted coins back into the item resource handler. Aborting sorting task.");
                    return;
                }
                oldItems.removeFirst();
            }
            //Commit the transaction if the sorted items were re-inserted
            tx.commit();
        }
    }

    private SPacketSyncCoinData getSyncPacket(HolderLookup.Provider registryAccess) {
        if(this.dataNotLoaded())
            this.reloadCoinData(registryAccess,false);
        DataContext<JsonElement> context = DataContext.createJson(registryAccess);
        Map<String,JsonObject> data = new HashMap<>();
        this.loadedChains.forEach((key,chain) -> data.put(key,chain.getAsJson(context)));
        return new SPacketSyncCoinData(data);
    }

    private void syncCoinDataWith(@Nullable Player player)
    {
        //Get the registry access
        HolderLookup.Provider registryAccess;
        if(player != null)
            registryAccess = player.registryAccess();
        else
        {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if(server != null)
                registryAccess = server.registryAccess();
            else
                return;
        }
        //Create packet
        SPacketSyncCoinData packet = this.getSyncPacket(registryAccess);
        if(player == null)
            packet.sendToAll();
        else
            packet.sendTo(player);
    }

    public static void handleSyncPacket(SPacketSyncCoinData message,HolderLookup.Provider registryAccess) {
        Map<String,ChainData> temp = new HashMap<>();
        List<CoinEntry> existingEntries = new ArrayList<>();
        DataContext<JsonElement> context = DataContext.createJson(registryAccess);
        try {
            message.getJson().forEach((key,data) -> {
                ChainData chain = ChainData.fromJson(key,existingEntries,data,context);
                temp.put(key,chain);
            });
        } catch (JsonParseException | IdentifierException e) {
            LightmansCurrency.LogError("Error parsing Coin Data that was sent from the server!",e);
        }
        if(!temp.containsKey(DEFAULT_CHAIN))
        {
            LightmansCurrency.LogError("Coin Data sent from the server is missing the '" + DEFAULT_CHAIN + "' chain, and cannot be used!");
            return;
        }
        INSTANCE.loadData(temp,false);
    }

    @SubscribeEvent
    private static void onServerStart(ServerAboutToStartEvent event) { INSTANCE.reloadCoinData(event.getServer().registryAccess(),false); }

    @SubscribeEvent
    private static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if(INSTANCE.dataNotLoaded())
            INSTANCE.reloadCoinData(event.getEntity().registryAccess(),true);
        else
            INSTANCE.syncCoinDataWith(event.getEntity());
    }

    private static class CoinSorter implements Comparator<ItemStack>
    {

        @Override
        public int compare(ItemStack stack1, ItemStack stack2) {
            //Start comparing chains
            ChainData chain1 = INSTANCE.lookupChain(stack1);
            ChainData chain2 = INSTANCE.lookupChain(stack2);
            if(chain1 == null && chain2 == null)
            {
                //If neither item has a coin chain, use custom sorters
                for(Comparator<ItemStack> custom : new ArrayList<>(INSTANCE.customSorters))
                {
                    int result = custom.compare(stack1,stack2);
                    if(result != 0)
                        return result;
                }
            }
            if(chain2 == null)
                return -1;
            if(chain1 == null)
                return 1;
            if(chain1 != chain2) //Sort by chain name
                return chain2.getDisplayName().getString().compareToIgnoreCase(chain1.getDisplayName().getString());

            //Sort by individual value
            CoinEntry entry1 = chain1.findEntry(stack1);
            CoinEntry entry2 = chain2.findEntry(stack2);
            if(entry1 == entry2)
                return Integer.compare(stack2.getCount(),stack1.getCount());
            return Long.compare(entry2.getInternalValue(),entry1.getInternalValue());
        }
    }

}
