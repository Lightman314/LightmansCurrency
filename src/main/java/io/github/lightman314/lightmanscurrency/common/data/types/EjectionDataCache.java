package io.github.lightman314.lightmanscurrency.common.data.types;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.api.ejection.SafeEjectionAPI;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomData;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomDataType;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@FieldsAreNonnullByDefault
public class EjectionDataCache extends CustomData {

    public static final CustomDataType<EjectionDataCache> TYPE = new CustomDataType<>("lightmanscurrency_ejection_data",EjectionDataCache::new);

    private long nextID = 0;
    private long getNextID() {
        long val = this.nextID++;
        this.setChanged();
        return val;
    }
    private final Map<Long,EjectionData> data = new HashMap<>();

    private EjectionDataCache() {}

    @Override
    public CustomDataType<?> getType() { return TYPE; }

    @Override
    public void save(CompoundTag tag) {
        tag.putLong("NextID",this.nextID);
        ListTag list = new ListTag();
        this.data.forEach((id,e) -> {
            CompoundTag entry = e.save();
            entry.putLong("ID",id);
            list.add(entry);
        });
        tag.put("EmergencyEjectionData",list);
    }

    @Override
    protected void load(CompoundTag tag) {
        if(tag.contains("NextID"))
            this.nextID = tag.getLong("NextID");
        ListTag ejectionData = tag.getList("EmergencyEjectionData", Tag.TAG_COMPOUND);
        for(int i = 0; i < ejectionData.size(); ++i)
        {
            try {
                CompoundTag data = ejectionData.getCompound(i);
                EjectionData e = SafeEjectionAPI.getApi().parseData(data);
                if(e != null && !e.isEmpty())
                {
                    if(e.id() < 0)
                        e.setID(this.nextID++);
                    this.data.put(e.id(),e);
                }
                else
                    LightmansCurrency.LogWarning("Loaded " + (e == null ? "null" : "empty") + " Ejection Data from file!\n" + data.getAsString());
            } catch(Throwable t) { LightmansCurrency.LogError("Error loading ejection data entry " + i, t); }
        }
    }

    public List<EjectionData> getData() { return new ArrayList<>(this.data.values()); }

    public void handleEjection(Level level, BlockPos pos, EjectionData data)
    {
        Objects.requireNonNull(data);
        if(data.isEmpty())
            return;

        if(LCConfig.SERVER.safelyEjectMachineContents.get() && !LCConfig.SERVER.anarchyMode.get())
        {
            long id = this.getNextID();
            data.setID(id);
            this.data.put(id,data);
            this.markEjectionDataDirty(id);
        }
        else
        {
            //Split/dismantle the ejection data in anarchy mode, but leave it as the recoverable item if ejection is simply turned off/disabled
            if(data.canSplit() && LCConfig.SERVER.anarchyMode.get())
                data.splitContents();
            InventoryUtil.dumpContents(level, pos, data.getContents());
        }

        //Push notification to the data's owner(s)
        data.pushNotificationToOwner();
    }

    public void markEjectionDataDirty(long id)
    {
        this.setChanged();
        if(this.data.containsKey(id))
        {
            EjectionData data = this.data.get(id);
            if(data.isEmpty())
            {
                //If the data is now empty, remove it and send a removal sync packet
                this.data.remove(id);
                this.sendSyncPacket(this.builder().setLong("RemoveData",id));
            }
            else
            {
                //Otherwise send an update packet
                this.sendSyncPacket(this.builder().setCompound("UpdateData",data.save()));
            }
        }
        else
        {
            //If the data isn't registered, send a removal packet just to be safe
            this.sendSyncPacket(this.builder().setLong("RemoveData",id));
        }
    }

    @Override
    protected void parseSyncPacket(LazyPacketData message) {
        if(message.contains("RemoveData"))
            this.data.remove(message.getLong("RemoveData"));
        if(message.contains("UpdateData"))
        {
            EjectionData d = SafeEjectionAPI.getApi().parseData(message.getNBT("UpdateData"));
            if(d != null)
                this.data.put(d.id(),d.flagAsClient(this));
        }
    }

    @Override
    public void onPlayerJoin(ServerPlayer player) {

        for(EjectionData d : this.data.values())
            this.sendSyncPacket(this.builder().setCompound("UpdateData",d.save()),player);

    }

}