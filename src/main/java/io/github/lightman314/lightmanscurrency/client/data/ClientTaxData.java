package io.github.lightman314.lightmanscurrency.client.data;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.common.util.LookupHelper;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientTaxData {

    private static final Map<Long, TaxEntry> loadedEntries = new HashMap<>();

    public static List<TaxEntry> GetAllTaxEntries() { return ImmutableList.copyOf(loadedEntries.values()); }

    public static TaxEntry GetEntry(long id) { return loadedEntries.get(id); }

    public static void UpdateEntry(CompoundTag tag)
    {
        long entryID = tag.getLong("ID");
        if(loadedEntries.containsKey(entryID))
            loadedEntries.get(entryID).load(tag, LookupHelper.getRegistryAccess(true));
        else
        {
            TaxEntry newEntry = new TaxEntry();
            newEntry.load(tag,LookupHelper.getRegistryAccess(true));
            loadedEntries.put(entryID, newEntry.flagAsClient());
        }
    }

    public static void RemoveEntry(long id) { loadedEntries.remove(id); }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) { loadedEntries.clear(); }

}
