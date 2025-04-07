package io.github.lightman314.lightmanscurrency.common.data.types;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.api.ejection.SafeEjectionAPI;
import io.github.lightman314.lightmanscurrency.api.events.TraderEvent;
import io.github.lightman314.lightmanscurrency.api.misc.IEasyTickable;
import io.github.lightman314.lightmanscurrency.api.misc.IServerTicker;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomData;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomDataType;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.auction.PersistentAuctionData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.common.util.LookupHelper;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TraderDataCache extends CustomData implements IServerTicker {

    public static final CustomDataType<TraderDataCache> TYPE = new CustomDataType<>("lightmanscurrency_trader_data",TraderDataCache::new);

    public static final String PERSISTENT_TRADER_FILENAME = "config/lightmanscurrency/PersistentTraders.json";

    public static final String PERSISTENT_TRADER_SECTION = "Traders";
    public static final String PERSISTENT_AUCTION_SECTION = "Auctions";

    private int cleanTick = 0;
    private long nextID = 0;
    private long getNextID() {
        long id = nextID;
        this.nextID++;
        this.setChanged();
        return id;
    }
    private final Map<Long, TraderData> traderData = new HashMap<>();

    //Persistent Data
    private final Map<String,PersistentData> persistentTraderData = new HashMap<>();
    private final List<PersistentAuctionData> persistentAuctionData = new ArrayList<>();

    private final List<IEasyTickable> tickers = new ArrayList<>();

    private JsonObject persistentTraderJson = new JsonObject();

    private TraderDataCache() { }

    private void validateAuctionHouse() {
        if(!LCConfig.SERVER.auctionHouseEnabled.get())
        {
            LightmansCurrency.LogInfo("Will not create or validate the auction house as the auction house is disabled.");
            return;
        }
        if(this.traderData.values().stream().noneMatch(t -> t instanceof AuctionHouseTrader))
        {
            //Create the auction house manually
            AuctionHouseTrader ah = AuctionHouseTrader.TYPE.create();
            ah.creative.set(true);

            //Generate a trader ID
            long traderID = this.getNextID();

            //Apply it to the trader
            ah.setID(traderID);

            LightmansCurrency.LogInfo("Successfully created an auction house trader with id '" + traderID + "'!");
            this.addTraderInternal(traderID, ah, LookupHelper.getRegistryAccess());
        }
    }

    @Override
    public CustomDataType<?> getType() { return TYPE; }

    @Override
    public void save(CompoundTag tag, HolderLookup.Provider lookup) {
        tag.putLong("NextID", this.nextID);

        ListTag traderData = new ListTag();
        this.traderData.forEach((id,trader) -> {
            if(trader.isPersistent())
            {
                try {
                    this.putPersistentTag(trader.getPersistentID(), trader.savePersistentData(lookup));
                } catch(Throwable t) { LightmansCurrency.LogError("Error saving persistent trader data:", t); }
            }
            else
            {
                try {
                    traderData.add(trader.save(lookup));
                } catch(Throwable t) { LightmansCurrency.LogError("Error saving trader data:", t); }
            }
        });
        tag.put("TraderData", traderData);

        ListTag persistentData = new ListTag();
        this.persistentTraderData.forEach((id,data) -> {
            try {
                CompoundTag c = new CompoundTag();
                c.putString("Name", id);
                c.putLong("ID", data.id);
                c.put("Tag", data.tag);
                persistentData.add(c);
            } catch(Throwable t) { LightmansCurrency.LogError("Error saving Persistent Data:", t); }
        });
        tag.put("PersistentData", persistentData);
    }

    @Override
    protected void load(CompoundTag tag, HolderLookup.Provider lookup) {
        this.nextID = tag.getLong("NextID");
        LightmansCurrency.LogInfo("Loaded NextID (" + this.nextID + ") from tag.");

        ListTag traderData = tag.getList("TraderData", Tag.TAG_COMPOUND);
        for(int i = 0; i < traderData.size(); ++i)
        {
            try {
                CompoundTag traderTag = traderData.getCompound(i);
                TraderData trader = TraderData.Deserialize(false, traderTag, lookup);
                if(trader != null)
                    this.addTraderInternal(trader.getID(), trader, lookup);
                else
                    LightmansCurrency.LogError("Error loading TraderData entry at index " + i);
            } catch(Throwable t) { LightmansCurrency.LogError("Error loading TraderData", t); }
        }

        ListTag persistentData = tag.getList("PersistentData", Tag.TAG_COMPOUND);
        for(int i = 0; i < persistentData.size(); ++i)
        {
            try {
                CompoundTag c = persistentData.getCompound(i);
                String name = c.getString("Name");
                long id = c.getLong("ID");
                CompoundTag data = c.getCompound("Tag");
                this.persistentTraderData.put(name, new PersistentData(id,data));
            } catch(Throwable t) { LightmansCurrency.LogError("Error loading Persistent Data", t); }
        }
    }

    private long getPersistentID(String traderID) {
        if(this.persistentTraderData.containsKey(traderID))
            return this.persistentTraderData.get(traderID).id;
        return -1;
    }

    private void putPersistentID(String traderID, long id) {
        if(this.persistentTraderData.containsKey(traderID))
            this.persistentTraderData.get(traderID).id = id;
        else
            this.persistentTraderData.put(traderID, new PersistentData(id, new CompoundTag()));
        this.setChanged();
    }

    private CompoundTag getPersistentTag(String traderID) {
        if(this.persistentTraderData.containsKey(traderID))
            return this.persistentTraderData.get(traderID).tag;
        return new CompoundTag();
    }

    private void putPersistentTag(String traderID, CompoundTag tag) {
        if(this.persistentTraderData.containsKey(traderID))
            this.persistentTraderData.get(traderID).tag = tag;
        else
            this.persistentTraderData.put(traderID, new PersistentData(-1, tag));
        this.setChanged();
    }

    @Nullable
    public JsonObject getPersistentTraderJson() {
        if(this.isClient())
            return null;
        return this.persistentTraderJson;
    }

    @Nullable
    public JsonArray getPersistentTraderJson(String section)
    {
        if(this.isClient())
            return null;
        if(!this.persistentTraderJson.has(section))
        {
            JsonArray newSection = new JsonArray();
            this.persistentTraderJson.add(section,newSection);
        }
        if(this.persistentTraderJson.get(section).isJsonArray())
            return this.persistentTraderJson.get(section).getAsJsonArray();
        LightmansCurrency.LogError("Cannot get Persistent Data section '" + section + "' as it is not a JsonArray.");
        return null;
    }

    public void setPersistentTraderJson(JsonObject newData, HolderLookup.Provider lookup)
    {
        if(this.isClient())
            return;
        File ptf = new File(PERSISTENT_TRADER_FILENAME);
        try {
            this.loadPersistentTrader(newData, lookup);
        } catch(Exception e) {
            LightmansCurrency.LogError("Error loading modified Persistent Trader Data. Ignoring request.", e);
            return;
        }
        //Now that it's safely loaded, set the data and saveItem to file
        this.persistentTraderJson = newData;
        this.savePersistentTraderJson(ptf);
        this.resendTraderData();
    }

    public void setPersistentTraderSection(String section, JsonArray newData, HolderLookup.Provider lookup)
    {
        if(this.isClient())
            return;
        this.persistentTraderJson.add(section,newData);
        this.setPersistentTraderJson(this.persistentTraderJson,lookup);
    }

    public void reloadPersistentTraders()
    {
        this.loadPersistentTraders();
        this.resendTraderData();
    }

    private void loadPersistentTraders() {
        if(this.isClient())
            return;
        //Get JSON file
        File ptf = new File(PERSISTENT_TRADER_FILENAME);
        if(!ptf.exists())
        {
            this.persistentTraderJson = generateDefaultPersistentTraderJson();
            this.savePersistentTraderJson(ptf);
        }
        try {
            this.persistentTraderJson = GsonHelper.parse(Files.readString(ptf.toPath()));
            LightmansCurrency.LogDebug("Loading PersistentTraders.json\n" +  FileUtil.GSON.toJson(this.persistentTraderJson));
            this.loadPersistentTrader(this.persistentTraderJson, LookupHelper.getRegistryAccess());
        } catch(Throwable e) {
            LightmansCurrency.LogError("Error loading Persistent Traders.", e);
            //If an error occurs while loading, set the data to default.
            this.persistentTraderJson = generateDefaultPersistentTraderJson();
        }
    }

    private void savePersistentTraderJson(File ptf) {
        File dir = new File(ptf.getParent());
        if(!dir.exists())
            dir.mkdirs();
        if(dir.exists())
        {
            try {

                ptf.createNewFile();

                String jsonString = FileUtil.GSON.toJson(this.persistentTraderJson);

                FileUtil.writeStringToFile(ptf, jsonString);

                LightmansCurrency.LogInfo("PersistentTraders.json does not exist. Creating a fresh copy.");

            } catch(Throwable e) { LightmansCurrency.LogError("Error attempting to create 'persistentTraders.json' file.", e); }
        }
    }

    private static JsonObject generateDefaultPersistentTraderJson() {
        JsonObject fileData = new JsonObject();
        JsonArray traderList = new JsonArray();
        fileData.add(PERSISTENT_TRADER_SECTION, traderList);
        JsonArray auctions = new JsonArray();
        fileData.add(PERSISTENT_AUCTION_SECTION, auctions);
        return fileData;
    }

    private void loadPersistentTrader(@Nonnull JsonObject fileData, @Nonnull HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException {
        boolean hadNone = true;
        if(fileData.has(PERSISTENT_TRADER_SECTION))
        {
            hadNone = false;

            //Remove persistent traders
            List<Long> removeTraderList = new ArrayList<>();
            this.traderData.forEach((id,trader) -> {
                if(trader.isPersistent())
                {
                    if(trader instanceof IEasyTickable t)
                        this.tickers.remove(t);
                    //Save persistent tag
                    this.putPersistentTag(trader.getPersistentID(), trader.savePersistentData(lookup));
                    removeTraderList.add(id);
                }
            });

            //Don't need to remove from tickers as this is done in the forEach call
            for(long id : removeTraderList)
                this.traderData.remove(id);

            List<String> loadedIDs = new ArrayList<>();
            JsonArray traderList = fileData.getAsJsonArray(PERSISTENT_TRADER_SECTION);
            for(int i = 0; i < traderList.size(); ++i)
            {
                try {

                    //Load the trader
                    JsonObject traderTag = GsonHelper.convertToJsonObject(traderList.get(i), PERSISTENT_TRADER_SECTION + "[" + i + "]");
                    String traderID = GsonHelper.getAsString(traderTag, "id", GsonHelper.getAsString(traderTag, "ID"));
                    if(loadedIDs.contains(traderID))
                        throw new JsonSyntaxException("Trader with id '" + traderID + "' already exists. Cannot have duplicate ids.");
                    if(traderID.isBlank())
                        throw new JsonSyntaxException("Trader cannot have a blank id!");
                    TraderData data = TraderData.Deserialize(traderTag, lookup);

                    //Load the persistent data
                    data.loadPersistentData(this.getPersistentTag(traderID), lookup);

                    //Match the persistent data with traders id
                    long id = this.getPersistentID(traderID);
                    if(id < 0) //If no ID has ever been generated for this persistent trader ID, generate one and add it to the list
                    {
                        id = this.getNextID();
                        this.putPersistentID(traderID, id);
                        this.setChanged();
                        LightmansCurrency.LogInfo("Generated new ID for persistent trader '" + traderID + "' (" + id + ")");
                    }
                    //Initialize the persistence (forces creative & terminal access)
                    data.makePersistent(id, traderID);

                    this.addTraderInternal(id, data, lookup);
                    loadedIDs.add(traderID);
                    LightmansCurrency.LogInfo("Successfully loaded persistent trader '" + traderID + "' with ID " + id + ".");

                } catch(JsonSyntaxException | ResourceLocationException e) { LightmansCurrency.LogError("Error loading Persistent Trader at index " + i, e); }
            }
        }
        if(fileData.has(PERSISTENT_AUCTION_SECTION))
        {
            hadNone = false;
            this.persistentAuctionData.clear();
            List<String> loadedIDs = new ArrayList<>();
            JsonArray auctionList = fileData.getAsJsonArray(PERSISTENT_AUCTION_SECTION);
            for(int i = 0; i < auctionList.size(); ++i)
            {
                try {

                    //Load the auction
                    JsonObject auctionTag = auctionList.get(i).getAsJsonObject();
                    PersistentAuctionData data = PersistentAuctionData.load(auctionTag,lookup);
                    if(loadedIDs.contains(data.id))
                        throw new JsonSyntaxException("Auction with id '" + data.id + "' already exists. Cannot have duplicate ids.");
                    else
                        loadedIDs.add(data.id);

                    this.persistentAuctionData.add(data);

                    LightmansCurrency.LogInfo("Successfully loaded persistent auction '" + data.id + "'");

                } catch(JsonSyntaxException | ResourceLocationException e) { LightmansCurrency.LogError("Error loading Persistent Auction at index " + i, e); }

            }

        }
        if(hadNone)
            throw new JsonSyntaxException("Json Data has no 'Traders' or 'Auctions' entry.");
    }

    public long registerTrader(TraderData newTrader, @Nullable Player player)
    {
        if(this.isClient())
            return -1;
        long newID = this.getNextID();
        this.addTraderInternal(newID, newTrader, LookupHelper.getRegistryAccess());
        if(newTrader.shouldAlwaysShowOnTerminal() && player != null)
            NeoForge.EVENT_BUS.post(new TraderEvent.CreateNetworkTraderEvent(newID, player));
        return newID;
    }

    private void addTraderInternal(long traderID, TraderData trader, HolderLookup.Provider lookup)
    {
        //Set Trader ID
        trader.setID(traderID);
        //Add to storage
        this.traderData.put(traderID, trader.allowMarkingDirty());
        this.setChanged();
        //Trigger OnRegisteration listener
        try{ trader.OnRegisteredToOffice();
        } catch(Throwable t) { LightmansCurrency.LogError("Error handling Trader-OnRegistration function!", t); }
        //Send update packet to all relevant clients
        this.sendSyncPacket(this.updatePacket(trader.save(lookup)));
        //Register tick listeners (if applicable)
        if(trader instanceof IEasyTickable t)
            this.tickers.add(t);
    }

    public void markTraderDirty(CompoundTag updateData)
    {
        this.setChanged();
        this.sendSyncPacket(this.updatePacket(updateData));
    }

    private LazyPacketData.Builder updatePacket(CompoundTag updateData) { return this.builder().setCompound("UpdateTrader",updateData); }

    public void deleteTrader(long traderID)
    {
        if(this.isClient())
            return;
        if(this.traderData.containsKey(traderID))
        {
            TraderData trader = this.traderData.get(traderID);
            //Delete from appropriate tax entries
            TaxDataCache.TYPE.get(false).getAllEntries().forEach(e -> e.TaxableWasRemoved(trader));
            //Remove from the Trader List
            this.traderData.remove(traderID);
            if(trader instanceof IEasyTickable t)
                this.tickers.remove(t);
            this.setChanged();
            this.sendSyncPacket(this.builder().setLong("DeleteTrader",traderID));
            if(trader.shouldAlwaysShowOnTerminal())
                NeoForge.EVENT_BUS.post(new TraderEvent.RemoveNetworkTraderEvent(traderID, trader));
        }
    }

    @Override
    protected void parseSyncPacket(LazyPacketData data, HolderLookup.Provider lookup) {
        if(data.contains("ClearTraders"))
            this.traderData.clear();
        if(data.contains("DeleteTrader"))
            this.traderData.remove(data.getLong("DeleteTrader"));
        if(data.contains("UpdateTrader"))
        {
            CompoundTag updateTag = data.getNBT("UpdateTrader");
            long id = updateTag.getLong("ID");
            if(this.traderData.containsKey(id))
                this.traderData.get(id).load(updateTag,lookup);
            else
            {
                TraderData trader = TraderData.Deserialize(true, updateTag, LookupHelper.getRegistryAccess());
                if(trader != null)
                {
                    this.traderData.put(id,trader.flagAsClient(this));
                    trader.OnRegisteredToOffice();
                }
            }
        }
    }

    public List<TraderData> getAllTraders() { return new ArrayList<>(this.traderData.values()); }

    public List<TraderData> getAllTerminalTraders() { return new ArrayList<>(this.getAllTraders().stream().filter(TraderData::showOnTerminal).toList()); }

    @Nullable
    public TraderData getTrader(long traderID) { return this.traderData.get(traderID); }

    @Nullable
    public TraderData getTrader(String persistentTraderID)
    {
        if(this.isClient())
        {
            //Lookup manually as persistent trader ids aren't synced with the clients
            List<TraderData> validTraders = this.getAllTraders().stream().filter(t -> t.getPersistentID().equals(persistentTraderID)).toList();
            if(!validTraders.isEmpty())
                return validTraders.getFirst();
            return null;
        }
        else
            return this.traderData.get(this.getPersistentID(persistentTraderID));
    }

    @Nullable
    public TraderData getAuctionHouse()
    {
        List<TraderData> validTraders = this.getAllTraders().stream().filter(t -> t instanceof AuctionHouseTrader).toList();
        if(!validTraders.isEmpty())
            return validTraders.getFirst();
        return null;
    }

    @Override
    public void serverTick() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server == null)
            return;
        ProfilerFiller filler = server.getProfiler();
        if(this.cleanTick++ >= 1200)
        {
            filler.push("Trader Data Position Validation");
            this.cleanTick = 0;
            List<TraderData> remove = new ArrayList<>();
            for(TraderData traderData : new ArrayList<>(this.traderData.values()))
            {
                if(traderData.shouldRemove(server))
                    remove.add(traderData);
            }
            for(TraderData traderData : remove)
            {
                if(traderData instanceof IEasyTickable t)
                    this.tickers.remove(t);
                this.traderData.remove(traderData.getID());
                try {
                    Level level = server.getLevel(traderData.getLevel());
                    BlockPos pos = traderData.getPos();
                    EjectionData e = traderData.buildEjectionData(level,pos,null);
                    SafeEjectionAPI.getApi().handleEjection(level,pos,e);
                } catch(NullPointerException e) { LightmansCurrency.LogError("Error deleting missing trader.",e); }
                this.sendSyncPacket(this.builder().setLong("DeleteTrader",traderData.getID()));
            }
            filler.pop();
        }
        if(server.getTickCount() % 20 == 0 && !this.persistentAuctionData.isEmpty())
        {
            filler.push("Persistent Auction Tick");
            List<TraderData> traders = new ArrayList<>(this.traderData.values());
            AuctionHouseTrader ah = null;
            for(int i = 0; i < traders.size() && ah == null; ++i)
            {
                if(traders.get(i) instanceof AuctionHouseTrader temp)
                    ah = temp;
            }
            if(ah != null)
            {
                for(PersistentAuctionData pad : this.persistentAuctionData)
                {
                    if(!ah.hasPersistentAuction(pad.id))
                    {
                        AuctionTradeData trade = pad.createAuction();
                        if(trade != null)
                        {
                            ah.addTrade(trade, true);
                            LightmansCurrency.LogInfo("Successfully added Persistent Auction '" + pad.id + "' into the auction house.");
                        }
                    }
                }
            }
            filler.pop();
        }
        filler.push("Trader Ticks");
        this.tickers.forEach(IEasyTickable::tick);
        filler.pop();
    }

    @Override
    protected void serverInit() {
        if(this.isClient())
            return;
        this.validateAuctionHouse();
        this.loadPersistentTraders();
    }

    @Override
    public void onPlayerJoin(ServerPlayer player) {
        this.sendSyncPacket(this.builder().setFlag("ClearTraders"),player);
        this.traderData.forEach((id,trader) -> this.sendSyncPacket(this.updatePacket(trader.save(player.registryAccess())),player));
    }

    private void resendTraderData() {
        this.sendSyncPacket(this.builder().setFlag("ClearTraders"));
        this.traderData.forEach((id,trader) -> this.sendSyncPacket(this.updatePacket(trader.save(LookupHelper.getRegistryAccess()))));
    }

    private static class PersistentData
    {

        public long id;
        public CompoundTag tag;

        public PersistentData(long id, CompoundTag tag) { this.id = id; this.tag = tag; }

    }

}
