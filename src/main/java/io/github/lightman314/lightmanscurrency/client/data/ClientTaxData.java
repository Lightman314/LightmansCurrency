package io.github.lightman314.lightmanscurrency.client.data;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientTaxData {

    private static final Map<Long, TaxEntry> loadedEntries = new HashMap<>();

    public static List<TaxEntry> GetAllTaxEntries() { return ImmutableList.copyOf(loadedEntries.values()); }

    public static TaxEntry GetEntry(long id) { return loadedEntries.get(id); }

    public static void UpdateEntry(CompoundTag tag)
    {
        long entryID = tag.getLong("ID");
        if(loadedEntries.containsKey(entryID))
            loadedEntries.get(entryID).load(tag);
        else
        {
            TaxEntry newEntry = new TaxEntry();
            newEntry.load(tag);
            loadedEntries.put(entryID, newEntry.flagAsClient());
        }
    }

    public static void RemoveEntry(long id) { loadedEntries.remove(id); }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggedOutEvent event) { loadedEntries.clear(); }

}