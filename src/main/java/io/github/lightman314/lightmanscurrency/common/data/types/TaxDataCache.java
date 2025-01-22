package io.github.lightman314.lightmanscurrency.common.data.types;

import io.github.lightman314.lightmanscurrency.api.misc.data.CustomData;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomDataType;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.blockentity.TaxBlockEntity;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.common.util.LookupHelper;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@FieldsAreNonnullByDefault
public class TaxDataCache extends CustomData {

    public static final CustomDataType<TaxDataCache> TYPE = new CustomDataType<>("lightmanscurrency_tax_data",TaxDataCache::new);

    private long nextID = 0;
    private final Map<Long, TaxEntry> entries = new HashMap<>();

    private TaxDataCache() {}

    @Override
    public CustomDataType<?> getType() { return TYPE; }

    @Override
    public void save(CompoundTag tag, HolderLookup.Provider lookup) {
        tag.putLong("NextID", this.nextID);
        ListTag entryList = new ListTag();
        this.entries.forEach((id,entry) -> {
            CompoundTag entryTag = entry.save(lookup);
            if(entryTag != null)
                entryList.add(entryTag);
        });
        tag.put("TaxEntries", entryList);
    }

    @Override
    protected void load(CompoundTag tag, HolderLookup.Provider lookup) {
        this.nextID = tag.getLong("NextID");
        ListTag list = tag.getList("TaxEntries", Tag.TAG_COMPOUND);
        for(int i = 0; i < list.size(); ++i)
        {
            TaxEntry entry = new TaxEntry();
            entry.load(list.getCompound(i), lookup);
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
        this.entries.put(TaxEntry.SERVER_TAX_ID,temp);
        this.markEntryDirty(TaxEntry.SERVER_TAX_ID,temp.save(LookupHelper.getRegistryAccess()));
        return temp;
    }

    public void markEntryDirty(long id, CompoundTag syncData)
    {
        if(id < 0 && id != TaxEntry.SERVER_TAX_ID)
            return;
        this.setChanged();
        this.sendSyncPacket(this.builder()
                .setCompound("UpdateEntry",syncData)
                .setLong("ID",id));
    }

    public long createEntry(@Nullable TaxBlockEntity spawnBE, @Nullable Player player)
    {
        if(this.isClient())
            return -1;
        long id = this.nextID++;
        TaxEntry entry = new TaxEntry(id, spawnBE, player);
        this.entries.put(id, entry.unlock());
        this.markEntryDirty(id,entry.save(LookupHelper.getRegistryAccess()));
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
            this.sendSyncPacket(this.builder().setLong("RemoveEntry",id));
        }
    }

    @Override
    protected void parseSyncPacket(LazyPacketData message, HolderLookup.Provider lookup) {
        if(message.contains("RemoveEntry"))
            this.entries.remove(message.getLong("RemoveEntry"));
        if(message.contains("UpdateEntry"))
        {
            long id = message.getLong("ID");
            CompoundTag data = message.getNBT("UpdateEntry");
            if(this.entries.containsKey(id))
                this.entries.get(id).load(data,LookupHelper.getRegistryAccess());
            else
            {
                TaxEntry newEntry = new TaxEntry();
                newEntry.load(data,LookupHelper.getRegistryAccess());
                this.entries.put(id,newEntry.flagAsClient());
            }
        }
    }

    @Override
    public void onPlayerJoin(ServerPlayer player)
    {
        for(TaxEntry entry : this.entries.values())
        {
            this.sendSyncPacket(this.builder()
                    .setCompound("UpdateEntry",entry.save(LookupHelper.getRegistryAccess()))
                    .setLong("ID",entry.getID()),player);
        }
    }

}
