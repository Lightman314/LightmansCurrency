package io.github.lightman314.lightmanscurrency.common.tickets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TicketSaveData extends SavedData {

    private long nextID = 0;
    private final Map<UUID,Long> convertedIDs = new HashMap<>();

    private TicketSaveData() {}
    private TicketSaveData(CompoundTag compound)
    {
        this.nextID = compound.getLong("NextID");
        if(compound.contains("ConvertedIDs"))
        {
            ListTag list = compound.getList("ConvertedIDs", Tag.TAG_COMPOUND);
            for(int i = 0; i < list.size(); ++i)
            {
                CompoundTag entry = list.getCompound(i);
                UUID uuid = entry.getUUID("UUID");
                long id = entry.getLong("ID");
                this.convertedIDs.put(uuid, id);
            }
        }
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compound) {

        compound.putLong("NextID", this.nextID);
        ListTag list = new ListTag();
        this.convertedIDs.forEach((uuid,id) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("UUID", uuid);
            entry.putLong("ID", id);
            list.add(entry);
        });
        if(list.size() > 0)
            compound.put("ConvertedIDs", list);
        return compound;
    }

    private static TicketSaveData get() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server != null)
            return server.overworld().getDataStorage().computeIfAbsent(TicketSaveData::new, TicketSaveData::new, "lightmanscurrency_ticket_data");
        return null;
    }

    private long createNextIDInternal() {
        long id = this.nextID++;
        this.setDirty();
        return id;
    }

    public static long createNextID() {
        TicketSaveData tsd = get();
        if(tsd != null)
            return tsd.createNextIDInternal();
        return 0;
    }

    public static long getConvertedID(UUID oldID) {
        TicketSaveData tsd = get();
        if(tsd != null)
        {
            if(tsd.convertedIDs.containsKey(oldID))
                return tsd.convertedIDs.get(oldID);
            tsd.convertedIDs.put(oldID, tsd.createNextIDInternal());
            tsd.setDirty();
            return tsd.convertedIDs.get(oldID);
        }
        return 0;
    }


}