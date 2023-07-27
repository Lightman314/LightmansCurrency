package io.github.lightman314.lightmanscurrency.common.taxes;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.data.ClientTaxData;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.tax.MessageRemoveClientTax;
import io.github.lightman314.lightmanscurrency.network.message.tax.MessageUpdateClientTax;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaxSaveData extends SavedData {

    private long nextID = 0;
    private final Map<Long,TaxEntry> entries = new HashMap<>();
    private TaxSaveData() {}
    private TaxSaveData(CompoundTag compound)
    {
        if(compound.contains("TaxEntries"))
        {
            ListTag list = compound.getList("TaxEntries", Tag.TAG_COMPOUND);
            for(int i = 0; i < list.size(); ++i)
            {
                TaxEntry entry = new TaxEntry();
                entry.load(list.getCompound(i));
                if(entry.getID() >= 0)
                    this.entries.put(entry.getID(), entry.unlock());
            }
        }
        if(compound.contains("NextID"))
            this.nextID = compound.getLong("NextID");
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag compound) {
        ListTag list = new ListTag();
        this.entries.forEach((id,entry) -> {
            CompoundTag entryTag = entry.save();
            if(entryTag != null)
                list.add(entryTag);
        });
        compound.put("TaxEntries", list);
        compound.putLong("NextID", this.nextID);
        return compound;
    }

    private static TaxSaveData get() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server != null)
            return server.overworld().getDataStorage().computeIfAbsent(TaxSaveData::new, TaxSaveData::new, "lightmanscurrency_tax_data");
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
            return data.entries.getOrDefault(id, null);
        return null;
    }

    public static void MarkTaxEntryDirty(long id, CompoundTag syncData)
    {
        if(id < 0)
        {
            LightmansCurrency.LogWarning("Attempted to mark a Tax Entry as changed, but is has no defined ID!");
            return;
        }
        TaxSaveData data = get();
        if(data != null)
        {
            data.setDirty();
            syncData.putLong("ID", id);
            LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageUpdateClientTax(syncData));
        }
    }

    public static long CreateAndRegister(@Nullable BlockEntity spawnBE, @Nullable Player player)
    {
        TaxSaveData data = get();
        if(data != null)
        {
            long id = data.nextID++;
            data.setDirty();
            TaxEntry entry = new TaxEntry(id, spawnBE, player);
            data.entries.put(id, entry.unlock());
            MarkTaxEntryDirty(id, entry.save());
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
            LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageRemoveClientTax(id));
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event)
    {
        //Sync all tax data on player join
        TaxSaveData data = get();
        if(data != null)
        {
            PacketDistributor.PacketTarget target = LightmansCurrencyPacketHandler.getTarget(event.getEntity());
            data.entries.forEach((id,entry) -> LightmansCurrencyPacketHandler.instance.send(target, new MessageUpdateClientTax(entry.save())));
        }
    }

}
