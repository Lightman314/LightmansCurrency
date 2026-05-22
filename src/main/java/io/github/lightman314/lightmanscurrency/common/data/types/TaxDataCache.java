package io.github.lightman314.lightmanscurrency.common.data.types;

import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomData;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomDataType;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.blockentity.TaxBlockEntity;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.*;

public class TaxDataCache extends CustomData {

    public static final CustomDataType<TaxDataCache> TYPE = new CustomDataType<>("lightmanscurrency_tax_data",TaxDataCache::new);

    private long nextID = 0;
    private final Map<Long, TaxEntry> entries = new HashMap<>();

    private Set<Long> changedEntries = new HashSet<>();

    private TaxDataCache() { }

    @Override
    public CustomDataType<?> getType() { return TYPE; }

    @Override
    public void save(CompoundTag tag,DataContext<Tag> context) {
        tag.putLong("NextID", this.nextID);
        ListTag entryList = new ListTag();
        this.entries.forEach((id,entry) -> {
            CompoundTag entryTag = entry.save(context);
            if(entryTag != null)
                entryList.add(entryTag);
        });
        tag.put("TaxEntries", entryList);
    }

    @Override
    protected void load(CompoundTag tag,DataContext<Tag> context) {
        this.nextID = tag.getLong("NextID");
        ListTag list = tag.getList("TaxEntries", Tag.TAG_COMPOUND);
        for(int i = 0; i < list.size(); ++i)
        {
            TaxEntry entry = new TaxEntry();
            entry.load(list.getCompound(i),context);
            if(entry.getID() >= 0 || entry.isServerEntry())
            {
                this.entries.put(entry.getID(), entry.unlock());
            }
        }
    }

    public List<TaxEntry> getAllEntries() { return new ArrayList<>(this.entries.values()); }

    @Nullable
    public TaxEntry getEntry(long id) { return this.entries.get(id); }

    public TaxEntry getServerEntry() {
        TaxEntry result = this.getEntry(TaxEntry.SERVER_TAX_ID);
        if(result != null)
            return result;
        TaxEntry temp = new TaxEntry(TaxEntry.SERVER_TAX_ID,null,null);
        temp.setRegistryAccess(this);
        this.entries.put(TaxEntry.SERVER_TAX_ID,temp.unlock());
        this.sendSyncPacket(this.builder()
                .setTag("CreateEntry",temp.save(this.dataContext())));
        return temp;
    }

    public void setEntryChanged(long id)
    {
        if(id < 0 && id != TaxEntry.SERVER_TAX_ID)
            return;
        this.setChanged();
        this.changedEntries.add(id);
    }

    public long createEntry(@Nullable TaxBlockEntity spawnBE, @Nullable Player player)
    {
        if(this.isClient())
            return -1;
        long id = this.nextID++;
        TaxEntry entry = new TaxEntry(id, spawnBE, player);
        this.entries.put(id,entry.unlock());
        this.sendSyncPacket(this.builder()
                .setTag("CreateEntry",entry.save(this.dataContext())));
        return id;
    }

    public void removeEntry(long id)
    {
        if(this.isClient())
            return;
        if(this.entries.containsKey(id))
        {
            this.entries.remove(id);
            this.setChanged();
            this.sendSyncPacket(this.builder()
                    .setLong("RemoveEntry",id));
        }
    }

    @Override
    protected void parseSyncPacket(LazyPacketData message, HolderLookup.Provider lookup) {
        if(message.contains("RemoveEntry"))
            this.entries.remove(message.getLong("RemoveEntry"));
        if(message.contains("CreateEntry"))
        {
            CompoundTag data = message.getTag("CreateEntry");
            TaxEntry entry = new TaxEntry();
            entry.load(data,this.dataContext());
            this.entries.put(entry.getID(),entry.flagAsClient());
        }
        if(message.contains("UpdateEntry"))
        {
            long id = message.getLong("ID");
            LazyPacketData data = message.getMap("UpdateEntry");
            if(this.entries.containsKey(id))
                this.entries.get(id).handleSyncPacket(data);
        }
    }

    @Override
    public void onPlayerJoin(ServerPlayer player)
    {
        for(TaxEntry entry : this.entries.values())
        {
            this.sendSyncPacket(this.builder()
                    .setTag("CreateEntry",entry.save(this.dataContext())));
        }
    }

    @Override
    public void syncTick() {
        Set<Long> changed = this.changedEntries;
        this.changedEntries = new HashSet<>();
        for(long id : changed)
        {
            TaxEntry entry = this.entries.get(id);
            if(entry != null)
            {
                this.sendSyncPacket(this.builder()
                        .setLong("ID",id)
                        .setMap("UpdateEntry",entry.clean()));
            }
        }
    }

}
