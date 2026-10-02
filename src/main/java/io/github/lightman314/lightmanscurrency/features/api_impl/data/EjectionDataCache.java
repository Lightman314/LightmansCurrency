package io.github.lightman314.lightmanscurrency.features.api_impl.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.data.FancyData;
import io.github.lightman314.lightmanscurrency.api.data.FancyDataType;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionEntry;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.*;

public class EjectionDataCache extends FancyData {

    private long nextID = 0;
    private final Map<Long,EjectionEntry> data = new HashMap<>();
    private Set<Long> changedEntries = new HashSet<>();

    private static final Codec<EjectionDataCache> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.LONG.fieldOf("nextID").forGetter(c -> c.nextID),
            EjectionEntry.CODEC.listOf().fieldOf("data").forGetter(EjectionDataCache::getAllEntries)
    ).apply(builder,EjectionDataCache::new));

    public static final FancyDataType<EjectionDataCache> TYPE = new FancyDataType<>("ejection_data",EjectionDataCache::new,CODEC);

    private EjectionDataCache() { }
    private EjectionDataCache(long nextID,List<EjectionEntry> data) {
        this.nextID = nextID;
        for(EjectionEntry e : data)
            this.data.put(e.getID(),e.setSidedContext(this));
    }

    @Override
    public FancyDataType<?> getType() { return TYPE; }

    public List<EjectionEntry> getAllEntries() { return new ArrayList<>(this.data.values()); }

    @Nullable
    public EjectionEntry getEntry(long id) { return this.data.get(id); }

    public void addEntry(EjectionEntry entry) {
        if(this.isClient())
            return;
        entry.setID(this.nextID++);
        this.data.put(entry.getID(),entry.setSidedContext(this));
    }

    @Override
    public void onPlayerJoin(ServerPlayer player) {
        this.sendPacket(player,FancyPacketMap.map()
                .setList("update",LCFancyPacketTypes.EJECTION_DATA,this.getAllEntries()));
    }

    public void setChanged(long id) {
        this.setChanged();
        if(this.isServer() && this.data.containsKey(id))
            this.changedEntries.add(id);
    }

    @Override
    public void syncTick() {
        Set<Long> changed = this.changedEntries;
        this.changedEntries = new HashSet<>();
        List<Long> removed = new ArrayList<>();
        List<EjectionEntry> sending = new ArrayList<>();
        for(long id : changed) {
            EjectionEntry entry = this.data.get(id);
            if(entry == null || entry.isEmpty()) {
                this.data.remove(id);
                removed.add(id);
            }
            else
                sending.add(entry);
        }
        this.sendPacketToAll(FancyPacketMap.map()
                .setList("update",LCFancyPacketTypes.EJECTION_DATA,sending)
                .setList("removed",LCFancyPacketTypes.LONG,removed));
    }

    @Override
    protected void handleSyncPacket(FancyPacketMap data) {
        for(EjectionEntry entry : data.getList("update",LCFancyPacketTypes.EJECTION_DATA))
            this.data.put(entry.getID(),entry.setSidedContext(this));
        for(long removed : data.getList("removed",LCFancyPacketTypes.LONG))
            this.data.remove(removed);
    }

}
