package io.github.lightman314.lightmanscurrency.common.data.types;

import io.github.lightman314.lightmanscurrency.api.misc.data.CustomData;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomDataType;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@FieldsAreNonnullByDefault
public class TicketDataCache extends CustomData {

    public static final CustomDataType<TicketDataCache> TYPE = new CustomDataType<>("lightmanscurrency_ticket_data",TicketDataCache::new);

    private long nextID = 0;

    private TicketDataCache() {}

    @Override
    public CustomDataType<?> getType() { return TYPE; }

    @Override
    public void save(CompoundTag tag, HolderLookup.Provider lookup) { tag.putLong("NextID", this.nextID); }

    @Override
    public void load(CompoundTag tag, HolderLookup.Provider lookup) { this.nextID = tag.getLong("NextID"); }

    public long peekNextID() { return this.nextID; }

    public long createNextID() {
        long id = this.nextID++;
        this.setChanged();
        this.sendSyncPacket(this.builder().setLong("NextID",this.nextID));
        return id;
    }

    @Override
    protected void parseSyncPacket(LazyPacketData message, HolderLookup.Provider lookup) {
        if(message.contains("NextID"))
            this.nextID = message.getLong("NextID");
    }

    @Override
    public void onPlayerJoin(ServerPlayer player) { this.sendSyncPacket(this.builder().setLong("NextID",this.nextID),player); }

}
