package io.github.lightman314.lightmanscurrency.common.tickets;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TicketSaveData extends WorldSavedData {

    private long nextID = 0;
    private final Map<UUID,Long> convertedIDs = new HashMap<>();

    private TicketSaveData() { super("lightmanscurrency_ticket_data"); }
    public void load(CompoundNBT compound)
    {
        this.nextID = compound.getLong("NextID");
        if(compound.contains("ConvertedIDs"))
        {
            ListNBT list = compound.getList("ConvertedIDs", Constants.NBT.TAG_COMPOUND);
            for(int i = 0; i < list.size(); ++i)
            {
                CompoundNBT entry = list.getCompound(i);
                UUID uuid = entry.getUUID("UUID");
                long id = entry.getLong("ID");
                this.convertedIDs.put(uuid, id);
            }
        }
    }

    @Override
    public @Nonnull CompoundNBT save(@Nonnull CompoundNBT compound) {

        compound.putLong("NextID", this.nextID);
        ListNBT list = new ListNBT();
        this.convertedIDs.forEach((uuid,id) -> {
            CompoundNBT entry = new CompoundNBT();
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
            return server.overworld().getDataStorage().computeIfAbsent(TicketSaveData::new, "lightmanscurrency_ticket_data");
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