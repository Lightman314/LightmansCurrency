package io.github.lightman314.lightmanscurrency.common.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.events.BuildDefaultMoneyDataEvent;
import io.github.lightman314.lightmanscurrency.api.events.ChainDataReloadedEvent;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.ATMAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.data.ATMData;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.data.ATMExchangeButtonData;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.CoinInputType;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.coin.CoinEntry;
import io.github.lightman314.lightmanscurrency.api.money.coins.display.ValueDisplayAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.display.builtin.*;
import io.github.lightman314.lightmanscurrency.api.money.coins.old_compat.OldCoinData;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.WalletCapability;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.data.SPacketSyncCoinData;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public final class CoinAPIImpl extends CoinAPI {

    public static final CoinAPIImpl INSTANCE = new CoinAPIImpl();
    public static final Comparator<ItemStack> SORTER = new CoinSorter();

    private CoinAPIImpl() {}
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    Map<String, ChainData> loadedChains = null;
    Map<ResourceLocation,ChainData> itemIdToChainMap = null;

    private boolean setup = false;

    @Override
    public boolean NoDataAvailable() { return loadedChains == null; }

    @Override
    public void Setup()
    {
        if(setup)
            return;
        setup = true;
        MinecraftForge.EVENT_BUS.addListener(this::onServerStart);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onJoinServer);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::generateDefaultCoins);
        ValueDisplayAPI.Setup();
        ATMAPI.Setup();
    }

    @Override
    public void ReloadCoinDataFromFile() {
        LightmansCurrency.LogInfo("Reloading Money Data");
        File mcl = new File(MONEY_FILE_LOCATION);
        if(!mcl.exists())
        {
            LightmansCurrency.LogInfo("MasterCoinList.json does not exist. Creating a fresh copy.");
            createMoneyDataFile(mcl, generateDefaultMoneyData(), false);
        }
        try {
            JsonObject fileData = GsonHelper.parse(Files.readString(mcl.toPath()));
            if(fileData.has("CoinEntries") && !fileData.has("Chains"))
                loadDeprecatedData(GsonHelper.getAsJsonArray(fileData, "CoinEntries"));
            else
                loadMoneyDataFromJson(fileData);
        } catch (JsonParseException | ResourceLocationException | IOException e) {
            LightmansCurrency.LogError("Error loading the Master Coin List. Using default values for now.", e);
            loadData(generateDefaultMoneyData());
        }
        this.SyncCoinDataWith(PacketDistributor.ALL.noArg());
    }

    public static void LoadEditedData(@Nonnull String customJson) {
        try {
            JsonObject json = GsonHelper.parse(customJson);
            INSTANCE.loadMoneyDataFromJson(json);
            INSTANCE.createMoneyDataFile(new File(MONEY_FILE_LOCATION),INSTANCE.loadedChains,false);
        } catch (JsonParseException | ResourceLocationException e) { LightmansCurrency.LogError("Error parsing custom json data!",e); }
    }

    private void loadData(@Nonnull Map<String,ChainData> dataMap)
    {
        ChainDataReloadedEvent.Pre event = new ChainDataReloadedEvent.Pre(dataMap);
        MinecraftForge.EVENT_BUS.post(event);
        loadedChains = event.getChainMap();
        Map<ResourceLocation,ChainData> temp = new HashMap<>();
        //Store chain data in an item to chain map so that we don't have to be manually searching through lists for matching entries.
        for(ChainData chain : loadedChains.values())
        {
            for(CoinEntry entry : chain.getAllEntries(true))
            {
                ResourceLocation coinID = ForgeRegistries.ITEMS.getKey(entry.getCoin());
                temp.put(coinID, chain);
            }
        }
        this.itemIdToChainMap = ImmutableMap.copyOf(temp);
        MinecraftForge.EVENT_BUS.post(new ChainDataReloadedEvent.Post(loadedChains));
    }

    @SuppressWarnings("deprecation")
    private void loadDeprecatedData(@Nonnull JsonArray oldArray) throws JsonSyntaxException, ResourceLocationException
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
            ChainData.Builder builder = ChainData.builder(chain, Component.translatable("lightmanscurrency.money.chain." + chain));
            //Copy old initial and plural values into the new Value Display system
            //Assuming coin display as the default display type
            CoinDisplay.Builder displayBuilder = CoinDisplay.builder();
            for(OldCoinData data : dataList)
            {
                Component initial = data.initialTranslation != null ? Component.translatable(data.initialTranslation) : null;
                Component plural = data.pluralTranslation != null ? Component.translatable(data.pluralTranslation) : null;
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
        createMoneyDataFile(new File(MONEY_FILE_LOCATION), loadedChains, true);
    }

    @SuppressWarnings("deprecation")
    private OldCoinData findNextInChain(@Nonnull List<OldCoinData> dataList, @Nonnull Item coin, boolean coreChainOnly)
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

    private void loadMoneyDataFromJson(@Nonnull JsonObject root) throws JsonSyntaxException, ResourceLocationException
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

    private void createMoneyDataFile(@Nonnull File mcl, @Nonnull Map<String,ChainData> data, boolean hideEventChains)
    {
        File dir = new File(mcl.getParent());
        if(!dir.exists())
            dir.mkdirs();
        if(dir.exists())
        {
            try {

                mcl.createNewFile();

                @Nonnull JsonObject json = this.getDataJson(data, hideEventChains);

                FileUtil.writeStringToFile(mcl, GSON.toJson(json));

            } catch(IOException e) { LightmansCurrency.LogError("Error attempting to create 'MasterCoinList.json' file.", e); }
        }
    }

    @Nonnull
    private Map<String,ChainData> generateDefaultMoneyData()
    {
        BuildDefaultMoneyDataEvent event = new BuildDefaultMoneyDataEvent();
        try { MinecraftForge.EVENT_BUS.post(event);
        } catch(RuntimeException e) { LightmansCurrency.LogError("Error generating default money data!", e); return new HashMap<>(); }

        Map<String,ChainData> results = new HashMap<>();
        event.getFinalResult().forEach((chain,builder) -> results.put(chain, builder.build()));
        return results;
    }

    //Flag as highest priority so that other mods can more easily modify my default values
    private void generateDefaultCoins(BuildDefaultMoneyDataEvent event)
    {
        ChainData.builder(CoinAPI.MAIN_CHAIN, LCText.COIN_CHAIN_MAIN)
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

        //Emerald Coin Chain
        ChainData.builder("emeralds", LCText.COIN_CHAIN_EMERALDS)
                .withCoreChain(Items.EMERALD).withCoin(Items.EMERALD_BLOCK, 9).back()
                .withInputType(CoinInputType.DEFAULT)
                .withDisplay(new NumberDisplay(LCText.COIN_CHAIN_EMERALDS_DISPLAY,LCText.COIN_CHAIN_EMERALDS_DISPLAY_WORDY, Items.EMERALD))
                .apply(event, true);

    }

    @Nonnull
    private JsonObject getDataJson(@Nonnull Map<String,ChainData> data, boolean hideEventChains)
    {
        JsonObject fileJson = new JsonObject();
        JsonArray chainArray = new JsonArray();

        for(ChainData chain : data.values())
        {
            if(hideEventChains && chain.isEvent)
                continue;
            chainArray.add(chain.getAsJson());
        }

        fileJson.add("Chains", chainArray);
        return fileJson;
    }

    @Nonnull
    @Override
    public ItemStack getEquippedWallet(@Nonnull Player player) {
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

    @Override
    @Nullable
    public ChainData ChainData(@Nonnull String chain) {
        if(this.NoDataAvailable())
            return null;
        return this.loadedChains.get(chain);
    }

    @Nonnull
    @Override
    public List<ChainData> AllChainData() {
        if(this.NoDataAvailable())
            return ImmutableList.of();
        return ImmutableList.copyOf(this.loadedChains.values());
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public ChainData ChainDataOfCoin(@Nonnull ItemStack coin) { return this.ChainDataOfCoin(coin.getItem()); }

    @Nullable
    @Override
    public ChainData ChainDataOfCoin(@Nonnull Item coin) {
        if(this.NoDataAvailable())
            return null;
        return this.itemIdToChainMap.get(ForgeRegistries.ITEMS.getKey(coin));
    }

    @Override
    public boolean IsCoin(@Nonnull ItemStack coin, boolean allowSideChains) { return !coin.isEmpty() && this.IsCoin(coin.getItem(), allowSideChains); }

    @Override
    public boolean IsCoin(@Nonnull Item coin, boolean allowSideChains) {
        if(coin == Items.AIR)
            return false;
        ChainData chainData = this.ChainDataOfCoin(coin);
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

    @Override
    public void CoinExchangeAllUp(@Nonnull Container container) {
        if(this.NoDataAvailable())
            return;
        for(ChainData chain : this.AllChainData())
        {
            List<CoinEntry> entryList = chain.getAllEntries(false, ChainData.SORT_LOWEST_VALUE_FIRST);
            for(CoinEntry entry : entryList)
                this.CoinExchangeUp(container, entry.getCoin());
            for(CoinEntry entry : entryList)
                this.CoinExchangeUp(container, entry.getCoin());
            for(CoinEntry entry : entryList)
                this.CoinExchangeUp(container, entry.getCoin());
        }
    }

    @Override
    public void CoinExchangeUp(@Nonnull Container container, @Nonnull Item smallCoin) {
        if(this.NoDataAvailable())
            return;
        ChainData chain = this.ChainDataOfCoin(smallCoin);
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

    @Override
    public void CoinExchangeAllDown(@Nonnull Container container) {
        if(this.NoDataAvailable())
            return;
        for(ChainData chain : this.AllChainData())
        {
            List<CoinEntry> entryList = chain.getAllEntries(false, ChainData.SORT_HIGHEST_VALUE_FIRST);
            for(CoinEntry entry : entryList)
                this.CoinExchangeDown(container, entry.getCoin());
            for(CoinEntry entry : entryList)
                this.CoinExchangeDown(container, entry.getCoin());
        }
    }

    @Override
    public void CoinExchangeDown(@Nonnull Container container, @Nonnull Item largeCoin) {
        if(this.NoDataAvailable())
            return;
        ChainData chain = this.ChainDataOfCoin(largeCoin);
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

    @Override
    public void SortCoinsByValue(@Nonnull Container container) {
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
        while(!oldInventory.isEmpty())
        {
            container.setItem(index++, oldInventory.get(0));
            oldInventory.remove(0);
        }
    }

    //Reload
    private void onServerStart(@Nonnull ServerAboutToStartEvent event) { this.ReloadCoinDataFromFile(); }

    private void onJoinServer(@Nonnull PlayerEvent.PlayerLoggedInEvent event)
    {
        LightmansCurrency.LogDebug("PlayerLoggedInEvent was called!");
        if(this.NoDataAvailable())
            this.ReloadCoinDataFromFile();
        this.SyncCoinDataWith(LightmansCurrencyPacketHandler.getTarget(event.getEntity()));
    }

    @Override
    public void SyncCoinDataWith(@Nonnull PacketDistributor.PacketTarget target) { new SPacketSyncCoinData(getDataJson(this.loadedChains, false)).sendToTarget(target); }

    @Override
    public void HandleSyncPacket(@Nonnull SPacketSyncCoinData packet) { this.loadMoneyDataFromJson(packet.getJson()); }

    private static class CoinSorter implements Comparator<ItemStack>
    {
        @Override
        public int compare(ItemStack stack1, ItemStack stack2) {
            //Sort by count if they're the same item
            if(stack1.getItem() == stack2.getItem())
                return Integer.compare(stack2.getCount(), stack1.getCount());

            //Start comparing chains
            ChainData chain1 = API.ChainDataOfCoin(stack1);
            ChainData chain2 = API.ChainDataOfCoin(stack2);
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
