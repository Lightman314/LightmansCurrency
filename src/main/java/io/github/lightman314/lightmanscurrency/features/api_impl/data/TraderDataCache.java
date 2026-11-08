package io.github.lightman314.lightmanscurrency.features.api_impl.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.data.FancyData;
import io.github.lightman314.lightmanscurrency.api.data.FancyDataType;
import io.github.lightman314.lightmanscurrency.api.helpers.FileHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.JsonHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.data.io.JsonValueInput;
import io.github.lightman314.lightmanscurrency.api.helpers.data.io.JsonValueOutput;
import io.github.lightman314.lightmanscurrency.api.helpers.data.io.LoggedProblemReporter;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerClient;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerCommon;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerServer;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderType;
import io.github.lightman314.lightmanscurrency.api.trader.event.TraderEvent;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.PersistentDataNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPersistentNode;
import io.github.lightman314.lightmanscurrency.api.trader.persistent.PersistentDataException;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public final class TraderDataCache extends FancyData implements ITickerServer, ITickerClient, ITickerCommon {

    public static final String DATA_LOCATION = "config/lightmanscurrency_persistent_traders";

    private static final Codec<TraderDataCache> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.LONG.fieldOf("nextID").forGetter(c -> c.nextID),
            Codec.unboundedMap(CodecHelper.LONG_KEY,TraderData.CODEC).fieldOf("traders").forGetter(TraderDataCache::getWritableTraders),
            Codec.unboundedMap(Codec.STRING,PersistentTraderData.CODEC).optionalFieldOf("persistentTraderData",Map.of()).forGetter(TraderDataCache::getPersistentTraderSaveData)
    ).apply(builder,TraderDataCache::new));

    /**
     * Use {@link LCApi#getTraderAPI()} instead to access traders
     */
    @ApiStatus.Internal
    public static final FancyDataType<TraderDataCache> TYPE = new FancyDataType<>("lightmanscurrency_trader_data",TraderDataCache::new,CODEC);

    public static boolean validPersistentID(String id) {
        for(int i = 0; i < id.length(); ++i) {
            if(!validPersistentIDChar(id.charAt(i)))
                return false;
        }
        return true;
    }

    public static boolean validPersistentIDChar(char c) {
        return c == '_' || c == '-' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9';
    }

    private long nextID;

    private final Map<Long,TraderData> traders;
    //Persistent Data
    private final Map<String,PersistentTraderData> persistentTraderData;
    private final Map<String,TraderData> persistentTraders = new HashMap<>();


    private Set<Long> changedTraders = new HashSet<>();
    private final List<Long> deletedTraders = new ArrayList<>();

    private TraderDataCache() { this(0,new HashMap<>(),new HashMap<>()); }
    private TraderDataCache(long nextID,Map<Long,TraderData> traders,Map<String,PersistentTraderData> persistentTraderData) {
        this.nextID = nextID;
        this.traders = new HashMap<>(traders);
        this.traders.forEach((id,t) -> this.pairTrader(t));
        this.persistentTraderData = new HashMap<>(persistentTraderData);
    }

    @Override
    protected void setupServer() {
        //Don't send the packet here as players should not be present yet
        this.reloadPersistentTraders(false);
    }

    private Map<Long,TraderData> getWritableTraders()
    {
        Map<Long,TraderData> copy = new HashMap<>(this.traders);
        this.traders.forEach((key,t) -> {
            if(t.getNodeValue(PersistentDataNode.TYPE,PersistentDataNode::isPersistent,false))
                copy.remove(key);
        });
        return copy;
    }

    private Map<String,PersistentTraderData> getPersistentTraderSaveData() {
        this.updateExistingPersistentTraderSaves();
        return this.persistentTraderData;
    }

    @Override
    public FancyDataType<?> getType() { return TYPE; }

    @Nullable
    public TraderData getTrader(long traderID) { return this.traders.get(traderID); }
    @Nullable
    public TraderData getTrader(String persistentKey) { return this.persistentTraders.get(persistentKey); }
    @Nullable
    public List<TraderData> getAllTraders() { return new ArrayList<>(this.traders.values()); }

    public Set<String> getPersistentKeys() { return new HashSet<>(this.persistentTraders.keySet()); }

    public long registerTrader(TraderData newTrader)
    {
        long nextID = this.nextID++;
        LightmansCurrency.LogDebug("New Trader Registered with id of " + nextID,new Throwable());
        newTrader.setID(nextID);
        this.addTraderInternal(newTrader);
        //Post Created Event
        NeoForge.EVENT_BUS.post(new TraderEvent.TraderCreatedEvent(newTrader));
        //Send created trader packet
        this.sendPacketToAll(asCreationPacket(List.of(newTrader)));
        //Return the new trader ID
        return nextID;
    }

    private void pairTrader(TraderData trader) {
        //Make sure the trader is on the same logical side as the cache
        trader.setSidedContext(this);
        trader.setRegistryAccess(this);
    }

    private void addTraderInternal(TraderData trader)
    {
        this.pairTrader(trader);
        //Add the trader to the map
        this.traders.put(trader.getID(),trader);
        PersistentDataNode node = trader.getNode(PersistentDataNode.TYPE);
        if(node != null && node.isPersistent())
            this.persistentTraders.put(node.getPersistentKey(),trader);
        //Remove from the deleted traders set just in case this is a persistent trader that's going to be reloaded this same tick
        this.deletedTraders.remove(trader.getID());
        //Initialize the trader
        trader.initialize();
    }

    public void deleteTrader(long traderID)
    {
        TraderData trader = this.getTrader(traderID);
        if(trader != null)
        {
            this.deletedTraders.add(traderID);
            this.traders.remove(traderID);
            PersistentDataNode node = trader.getNode(PersistentDataNode.TYPE);
            if(node != null && node.isPersistent())
                this.persistentTraders.remove(node.getPersistentKey());
            else //Only post the deletion event when deleting a non-persistent trader
                NeoForge.EVENT_BUS.post(new TraderEvent.TraderDeletedEvent(trader));
        }
    }

    public void createPersistentCopy(TraderData trader,@Nullable String id,String ownerName,String nameOverride) {
        //Confirm that the trader has the persistent trader node type
        if(trader.hasNode(PersistentDataNode.TYPE)) {
            try {
                if(id == null || !validPersistentID(id))
                    id = this.nextAvailableID();
                try(LoggedProblemReporter reporter = LightmansCurrency.createProblemReporter(new PersistentTraderElement(id))) {
                    JsonValueOutput output = JsonValueOutput.createWithContext(reporter,this.registryAccess());
                    boolean success = true;
                    for(TraderNode node : trader.getAllNodes()) {
                        if(node instanceof IPersistentNode pn) {
                            try {
                                ValueOutput entry = output.child(node.getTypeString());
                                pn.writePersistentData(entry,nameOverride);
                                if(entry.isEmpty())
                                    output.discard(node.getTypeString());
                            } catch (PersistentDataException exception) {
                                success = false;
                                reporter.forChild(new ProblemReporter.FieldPathElement(node.getTypeString())).report(exception);
                            }
                        }
                    }
                    if(success) {
                        //Write the type
                        output.store("type",LCRegistries.Trader.TRADER_TYPES.byNameCodec(),trader.getType());
                        output.store("owner",ComponentSerialization.CODEC,Component.literal(ownerName));
                        //Write to file
                        this.writePersistentDataToFile(id,output.buildResult());
                        //Delete this specific persistent trader specifically
                        if(this.persistentTraders.containsKey(id)) {
                            this.updateExistingPersistentTraderSaves();
                            this.deleteTrader(this.persistentTraders.get(id).getID());
                        }
                        //Now reload this persistent trader specifically
                        TraderData newTrader = this.readPersistentData(id,output.buildResult());
                        if(newTrader != null) //Send created trader packet
                            this.sendPacketToAll(asCreationPacket(List.of(newTrader)));
                    }
                }
            } catch (PersistentDataException exception) {
                LightmansCurrency.LogWarning("Unable to make a persistent copy of this trader",exception);
            }
        }
    }

    private String nextAvailableID() throws PersistentDataException{
        String prefix = "trader_";
        for(int i = 0; i < Integer.MAX_VALUE; ++i) {
            if(!this.persistentTraderData.containsKey(prefix + i))
                return prefix + i;
        }
        throw new PersistentDataException("Unable to automatically generate a persistent trader ID!");
    }

    private void updateExistingPersistentTraderSaves() {
        //Updates the "persistent traders" map to have up-to-date persistent save data
        this.persistentTraders.forEach((pkey,trader) -> {
            PersistentTraderData data;
            if(this.persistentTraderData.containsKey(pkey))
                data = this.persistentTraderData.get(pkey);
            else {
                data = new PersistentTraderData(trader.getID(),new CompoundTag());
                this.persistentTraderData.put(pkey,data);
            }
            try(LoggedProblemReporter reporter = LightmansCurrency.createProblemReporter(new PersistentTraderElement(pkey))) {
                TagValueOutput output = TagValueOutput.createWithContext(reporter,this.registryAccess());
                for(IPersistentNode node : trader.getNodes(IPersistentNode.class)) {
                    String typeString = node instanceof TraderNode n ? n.getTypeString() : "unknown";
                    ValueOutput entry = output.child(typeString);
                    node.writePersistentSaveData(entry);
                    if(entry.isEmpty())
                        output.discard(typeString);
                }
                data.data = output.buildResult();
            }
        });
    }

    public void reloadPersistentTraders() { this.reloadPersistentTraders(true); }
    private void reloadPersistentTraders(boolean sendPacket) {
        //Update to the latest save data
        this.updateExistingPersistentTraderSaves();
        Map<String,TraderData> oldTraders = Map.copyOf(this.persistentTraders);
        for(TraderData trader : oldTraders.values())
            this.deleteTrader(trader.getID());
        File folder = new File(DATA_LOCATION);
        if(folder.exists() || folder.mkdirs()) {
            //Locate all files in the folder that end with ".json"
            File[] array = folder.listFiles((dir,name) -> dir.equals(folder) && name.endsWith(".json"));
            List<TraderData> addedTraders = new ArrayList<>();
            for(File file : array) {
                var loaded = this.readPersistentData(file);
                if(loaded != null) {
                    TraderData t = loaded.getSecond();
                    String id = loaded.getFirst();
                    if(oldTraders.containsKey(id))
                        t.copyTrackingData(oldTraders.get(id));
                    addedTraders.add(loaded.getSecond());
                }
            }
            if(sendPacket && !addedTraders.isEmpty()) {
                this.sendPacketToAll(asCreationPacket(addedTraders));
                //Resend full tracking packets for all traders if they have any players already tracking them
                for(TraderData trader : addedTraders)
                    this.sendFullTraderPacket(trader);
            }
            LightmansCurrency.LogDebug("Loaded " + addedTraders.size() + " of " + array.length + " persistent traders from file!");
        }
    }

    private void writePersistentDataToFile(String id,JsonObject json) {
        File folder = new File(DATA_LOCATION);
        if(folder.exists() || folder.mkdirs()) {
            File file = new File(DATA_LOCATION + "/" + id + ".json");
            try {
                FileHelper.writeStringToFile(file,JsonHelper.PRETTY_GSON.toJson(json));
            } catch (IOException e) {
                LightmansCurrency.LogError("Unable to write the persistent trader " + id + " to file!",e);
            }
        }
    }

    @Nullable
    private Pair<String,TraderData> readPersistentData(File file) {
        try {
            String id = this.extractTraderID(file);
            if(!TraderDataCache.validPersistentID(id)) {
                LightmansCurrency.LogWarning("Found " + id + ".json in the persistent traders folder, but this is not a valid file name for persistent traders!");
                return null;
            }
            JsonObject json = GsonHelper.parse(Files.readString(file.toPath()));
            TraderData result = this.readPersistentData(id,json);
            if(result != null)
                return Pair.of(id,result);
        } catch (IOException | JsonSyntaxException e) {
            LightmansCurrency.LogError("Unable to read " + file.getName() + " as a persistent trader!",e);
        }
        return null;
    }
    @Nullable
    private TraderData readPersistentData(String id,JsonObject json) {
        try(LoggedProblemReporter reporter = LightmansCurrency.createProblemReporter(new PersistentTraderElement(id))) {
            try {
                ValueInput input = JsonValueInput.create(reporter,this.registryAccess(),json);
                TraderType type = input.read("type",LCRegistries.Trader.TRADER_TYPES.byNameCodec()).orElseThrow(() -> new PersistentDataException("Missing type field!"));
                TraderData newPT = new TraderData(type);
                if(newPT.hasNode(PersistentDataNode.TYPE)) {
                    //Define the persistent data
                    PersistentDataNode pdn = newPT.getNode(PersistentDataNode.TYPE);
                    pdn.flagAsPersistent(id,input.read("owner",ComponentSerialization.CODEC).orElseThrow(() -> new PersistentDataException("Missing owner field!")));
                    boolean success = true;
                    for(TraderNode node : newPT.getAllNodes()) {
                        if(node instanceof IPersistentNode pn) {
                            String key = node.getTypeString();
                            ProblemReporter entryReporter = reporter.forChild(new ProblemReporter.FieldPathElement(key));
                            try {
                                pn.readPersistentData(input.childOrEmpty(key),entryReporter);
                            } catch (PersistentDataException exception) {
                                success = false;
                                entryReporter.report(exception);
                            }
                        }
                    }
                    if(success) {
                        //Now attempt to load save data
                        if(this.persistentTraderData.containsKey(id)) {
                            PersistentTraderData data = this.persistentTraderData.get(id);
                            newPT.setID(data.id);
                            ValueInput saveInput = TagValueInput.create(ProblemReporter.DISCARDING,this.registryAccess(),data.data);
                            for(TraderNode node : newPT.getAllNodes()) {
                                if(node instanceof IPersistentNode pn)
                                    saveInput.child(node.getTypeString()).ifPresent(pn::readPersisentSaveData);
                            }
                        }
                        else {
                            PersistentTraderData data = new PersistentTraderData(this.nextID++,new CompoundTag());
                            this.persistentTraderData.put(id,data);
                            //Set as changed since the next id changed
                            this.setChanged();
                            newPT.setID(data.id);
                        }
                        //Actually add the trader
                        this.addTraderInternal(newPT);
                        return newPT;
                    }
                }
                else
                    throw new PersistentDataException("Trader of type " + type.getKey() + " does not support persistent traders!");
            } catch (PersistentDataException e) {
                reporter.report(e);
            }
        }
        return null;
    }

    private String extractTraderID(File file) {
        String name = file.getName();
        return name.substring(0,name.length() - 5);
    }

    @Override
    public void onPlayerJoin(ServerPlayer player) {
        //Send "Trader Exists" packet for each existing trader

        this.sendPacketToTarget(player, asCreationPacket(this.getAllTraders()));
    }

    public void setTraderChanged(TraderData trader)
    {
        this.setChanged();
        this.changedTraders.add(trader.getID());
    }

    @Override
    public void syncTick() {
        Set<Long> changed = this.changedTraders;
        this.changedTraders = new HashSet<>();
        for(long id : changed)
        {
            TraderData trader = this.traders.get(id);
            if(trader != null)
                this.sendUpdatePacket(trader);
        }
        this.sendPacketToAll(FancyPacketMap.map().setList("DeletedTraders", LCFancyPacketTypes.LONG,this.deletedTraders));
        this.deletedTraders.clear();
    }

    private void sendUpdatePacket(TraderData trader)
    {
        for(ServerPlayer sp : this.getPossibleTargets())
            this.sendTraderPacket(sp,trader,trader.getChangedData(sp));
        trader.clean();
    }

    private void sendFullTraderPacket(TraderData trader) {
        for(Player player : this.getOnlinePlayers())
            this.sendFullTraderPacket(player,trader);
    }

    private void sendFullTraderPacket(Player player,TraderData trader) {
        this.sendTraderPacket(player,trader,trader.fullSyncPacket(player));
    }

    public void sendTraderPacket(Player player,TraderData trader,FancyPacketMap packet) {
        //Don't send a packet if the packet is empty
        if(packet.isEmpty())
            return;
        this.sendPacketToTarget(player,asTraderUpdatePacket(trader,packet));
    }

    private static FancyPacketMap asTraderUpdatePacket(TraderData trader,FancyPacketMap packet)
    {
        return FancyPacketMap.map()
                .setMap("UpdateTrader",FancyPacketMap.map()
                        .setLong("id",trader.getID())
                        .setMap("data",packet).immutable()).immutable();
    }

    private static FancyPacketMap asCreationPacket(List<TraderData> addedTraders) {
        List<FancyPacketMap> list = new ArrayList<>();
        for(TraderData trader : addedTraders) {
            list.add(FancyPacketMap.map()
                    .setLong("id",trader.getID())
                    .setRegistryEntry("type",LCRegistries.Trader.TRADER_TYPES,trader.getType())
                    .setMap("data",trader.createTraderPacket()));
        }
        return FancyPacketMap.map().setList("CreatedTraders",LCFancyPacketTypes.MAP,list);
    }

    @Override
    protected void handleSyncPacket(FancyPacketMap data) {
        if(data.contains("CreatedTraders"))
        {
            for(FancyPacketMap entry : data.getList("CreatedTraders", LCFancyPacketTypes.MAP))
            {
                long traderID = entry.getLong("id");
                TraderType type = entry.getRegistryEntry("type",LCRegistries.Trader.TRADER_TYPES);
                if(type == null)
                    continue;
                TraderData newTrader = new TraderData(type);
                newTrader.setID(traderID);
                //Pair the trader first, otherwise it'll think it's the server copy and ignore the packet
                this.pairTrader(newTrader);
                FancyPacketMap nodeData = entry.getMap("data");
                newTrader.handleSyncPacket(nodeData);
                //Now add the trader
                this.addTraderInternal(newTrader);
            }
        }
        if(data.contains("DeletedTraders"))
        {
            List<Long> list = data.getList("DeletedTraders", LCFancyPacketTypes.LONG);
            for(long id : list)
                this.traders.remove(id);
        }
        if(data.contains("UpdateTrader"))
        {
            FancyPacketMap entry = data.getMap("UpdateTrader");
            long traderID = entry.getLong("id",-1);
            FancyPacketMap update = entry.getMap("data");
            TraderData trader = this.getTrader(traderID);
            if(trader != null)
                trader.handleSyncPacket(update);
            else
                LightmansCurrency.LogError("Received a trader update packet for a trader not present on the client!");
        }
    }

    @Override
    protected void setupCommon() {
        for(TraderData trader : this.getAllTraders())
        {
            trader.initialize();
        }
    }

    @Override
    public void clientTick() {
        for(TraderData trader : new ArrayList<>(this.traders.values()))
        {
            for(ITickerClient t : trader.getNodes(ITickerClient.class))
                t.clientTick();
        }
    }

    @Override
    public void tick() {
        for(TraderData trader : new ArrayList<>(this.traders.values()))
        {
            for(ITickerCommon t : trader.getNodes(ITickerCommon.class))
                t.tick();
        }
    }

    @Override
    public void serverTick() {
        for(TraderData trader : new ArrayList<>(this.traders.values()))
        {
            for(ITickerServer t : trader.getNodes(ITickerServer.class))
                t.serverTick();
        }
    }

    private static class PersistentTraderData {
        private static final Codec<PersistentTraderData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.LONG.fieldOf("id").forGetter(d -> d.id),
                CompoundTag.CODEC.fieldOf("data").forGetter(d -> d.data)
        ).apply(builder,PersistentTraderData::new));
        public long id;
        public CompoundTag data;
        public PersistentTraderData(long id,CompoundTag data) { this.id = id; this.data = data; }
    }

    private record PersistentTraderElement(String name) implements ProblemReporter.PathElement {
        @Override
        public String get() { return "persistent_trader/" + name + ".json -> "; }
    }

}