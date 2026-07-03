package io.github.lightman314.lightmanscurrency.features.api_impl.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.data.FancyData;
import io.github.lightman314.lightmanscurrency.api.data.FancyDataType;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerClient;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerCommon;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerServer;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderType;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.*;

public final class TraderDataCache extends FancyData implements ITickerServer, ITickerClient, ITickerCommon {

    private static final Codec<TraderDataCache> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.LONG.fieldOf("nextID").forGetter(c -> c.nextID),
            Codec.unboundedMap(CodecHelper.LONG_KEY,TraderData.CODEC).fieldOf("traders").forGetter(TraderDataCache::getWritableTraders)
    ).apply(builder,TraderDataCache::new));

    public static final FancyDataType<TraderDataCache> TYPE = new FancyDataType<>("lightmanscurrency_trader_data",TraderDataCache::new,CODEC);

    private long nextID;

    private final Map<Long,TraderData> traders;
    private Set<Long> changedTraders = new HashSet<>();
    private final List<Long> deletedTraders = new ArrayList<>();

    private TraderDataCache() { this(0,new HashMap<>()); }
    private TraderDataCache(long nextID,Map<Long,TraderData> traders) {
        this.nextID = nextID;
        this.traders = new HashMap<>(traders);
        this.traders.forEach((id,t) -> this.pairTrader(t));
    }

    private Map<Long,TraderData> getWritableTraders()
    {
        //TODO filter out persistent traders
        return this.traders;
    }

    @Override
    public FancyDataType<?> getType() { return TYPE; }

    @Nullable
    public TraderData getTrader(long traderID) { return this.traders.get(traderID); }
    @Nullable
    public List<TraderData> getAllTraders() { return new ArrayList<>(this.traders.values()); }

    public long registerTrader(TraderData newTrader)
    {
        long nextID = this.nextID++;
        LightmansCurrency.LogDebug("New Trader Registered with id of " + nextID,new Throwable());
        newTrader.setID(nextID);
        this.addTraderInternal(newTrader);
        //Send created trader packet
        this.sendPacketToAll(FancyPacketMap.newMutable()
                .setList("CreatedTraders",LCFancyPacketTypes.MAP,ImmutableList.of(asCreatedTraderPacket(newTrader))));
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
        }
    }

    @Override
    public void onPlayerJoin(ServerPlayer player) {
        //Send "Trader Exists" packet for each existing trader
        FancyPacketMap.Mutable packet = FancyPacketMap.newMutable();
        List<FancyPacketMap> list = new ArrayList<>();
        for(TraderData trader : this.getAllTraders())
            list.add(asCreatedTraderPacket(trader));
        packet.setList("CreatedTraders",LCFancyPacketTypes.MAP,list);
        this.sendPacketToTarget(player,packet);
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
        this.sendPacketToAll(FancyPacketMap.newMutable().setList("DeletedTraders", LCFancyPacketTypes.LONG,this.deletedTraders));
        this.deletedTraders.clear();
    }

    private void sendUpdatePacket(TraderData trader)
    {
        for(ServerPlayer sp : this.getPossibleTargets())
            this.sendTraderPacket(sp,trader,trader.getChangedData(sp));
        trader.clean();
    }

    public void sendTraderPacket(Player player,TraderData trader,FancyPacketMap packet) { this.sendPacketToTarget(player,asTraderUpdatePacket(trader,packet)); }

    private static FancyPacketMap asTraderUpdatePacket(TraderData trader,FancyPacketMap packet)
    {
        return FancyPacketMap.newMutable()
                .setMap("UpdateTrader",FancyPacketMap.newMutable()
                        .setLong("id",trader.getID())
                        .setMap("data",packet).immutable()).immutable();
    }

    private static FancyPacketMap asCreatedTraderPacket(TraderData trader)
    {
        return FancyPacketMap.newMutable()
                .setLong("id",trader.getID())
                .setRegistryEntry("type",LCRegistries.Trader.TRADER_TYPES,trader.getType())
                .setMap("data",trader.createTraderPacket())
                .immutable();
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

}