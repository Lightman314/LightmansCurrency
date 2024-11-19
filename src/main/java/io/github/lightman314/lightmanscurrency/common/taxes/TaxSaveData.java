package io.github.lightman314.lightmanscurrency.common.taxes;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.data.ClientTaxData;
import io.github.lightman314.lightmanscurrency.common.util.LookupHelper;
import io.github.lightman314.lightmanscurrency.network.message.tax.SPacketRemoveTax;
import io.github.lightman314.lightmanscurrency.network.message.tax.SPacketUpdateClientTax;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = LightmansCurrency.MODID)
public class TaxSaveData extends SavedData {

    public static final long SERVER_TAX_ID = -9;

    private long nextID = 0;
    private final Map<Long,TaxEntry> entries = new HashMap<>();
    private TaxSaveData() {}
    private TaxSaveData(CompoundTag compound, @Nonnull HolderLookup.Provider lookup)
    {
        this.nextID = compound.getLong("NextID");
        ListTag list = compound.getList("TaxEntries", Tag.TAG_COMPOUND);
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

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
        compound.putLong("NextID", this.nextID);
        ListTag entryList = new ListTag();
        this.entries.forEach((id,entry) -> {
            CompoundTag entryTag = entry.save(lookup);
            if(entryTag != null)
                entryList.add(entryTag);
        });
        compound.put("TaxEntries", entryList);
        return compound;
    }

    private static TaxSaveData get() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server != null)
            return server.overworld().getDataStorage().computeIfAbsent(new Factory<>(TaxSaveData::new, TaxSaveData::new), "lightmanscurrency_tax_data");
        return null;
    }

    public static List<TaxEntry> GetAllTaxEntries(boolean isClient)
    {
        if(isClient)
            return ClientTaxData.GetAllTaxEntries();
        TaxSaveData data = get();
        if(data != null)
            return ImmutableList.copyOf(data.entries.values());
        return ImmutableList.of();
    }

    @Nullable
    public static TaxEntry GetTaxEntry(long id, boolean isClient)
    {
        if(isClient)
            return ClientTaxData.GetEntry(id);
        TaxSaveData data = get();
        if(data != null)
            return data.entries.get(id);
        return null;
    }

    @Nullable
    public static TaxEntry GetServerTaxEntry(boolean isClient)
    {
        if(isClient)
            return ClientTaxData.GetEntry(SERVER_TAX_ID);
        TaxSaveData data = get();
        if(data != null)
        {
            TaxEntry entry = data.entries.get(SERVER_TAX_ID);
            if(entry == null)
            {
                entry = new TaxEntry(SERVER_TAX_ID, null, null);
                data.entries.put(SERVER_TAX_ID, entry);
                MarkTaxEntryDirty(SERVER_TAX_ID, entry.save(LookupHelper.getRegistryAccess()));
            }
            return entry;
        }
        return null;
    }

    public static void MarkTaxEntryDirty(long id, CompoundTag syncData)
    {
        if(id < 0 && id != SERVER_TAX_ID)
        {
            LightmansCurrency.LogWarning("Attempted to mark a Tax Entry as changed, but is has no defined ID!");
            return;
        }
        TaxSaveData data = get();
        if(data != null)
        {
            data.setDirty();
            syncData.putLong("ID", id);
            new SPacketUpdateClientTax(syncData).sendToAll();
        }
    }

    public static long CreateAndRegister(@Nullable BlockEntity spawnBE, @Nullable Player player)
    {
        TaxSaveData data = get();
        if(data != null)
        {
            long id = data.nextID++;
            TaxEntry entry = new TaxEntry(id, spawnBE, player);
            data.entries.put(id, entry.unlock());
            MarkTaxEntryDirty(id, entry.save(LookupHelper.getRegistryAccess()));
            return id;
        }
        return -1;
    }

    public static void RemoveEntry(long id)
    {
        TaxSaveData data = get();
        if(data != null && data.entries.containsKey(id))
        {
            data.entries.remove(id);
            data.setDirty();
            new SPacketRemoveTax(id).sendToAll();
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event)
    {
        //Sync all tax data on player join
        TaxSaveData data = get();
        if(data != null)
        {
            Player player = event.getEntity();
            data.entries.forEach((id,entry) -> new SPacketUpdateClientTax(entry.save(player.registryAccess())).sendTo(player));
        }
    }

}
