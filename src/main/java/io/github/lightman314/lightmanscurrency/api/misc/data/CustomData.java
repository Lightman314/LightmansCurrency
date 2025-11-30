package io.github.lightman314.lightmanscurrency.api.misc.data;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.common.util.LookupHelper;
import io.github.lightman314.lightmanscurrency.network.message.data.SPacketSyncCustomData;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class CustomData implements IClientTracker, LazyPacketData.IBuilderProvider {

    public abstract CustomDataType<?> getType();

    private boolean initialized = false;
    private boolean isClient = false;
    private boolean loaded = false;
    private Runnable setChanged = () -> {};

    @Override
    public final boolean isClient() { return this.isClient; }
    //Override to make it final
    @Override
    public final boolean isServer() { return IClientTracker.super.isServer(); }
    public final CustomData initClient() {
        if(this.initialized)
            return this;
        this.initialized = this.isClient = true;
        return this;
    }
    public final void initServer(Runnable setChanged) {
        if(this.initialized)
            return;
        this.initialized = true;
        this.setChanged = setChanged;
        try { this.serverInit();
        } catch (Throwable t) { LightmansCurrency.LogError("Error caught in the custom data's serverInit function!",t); }
    }

    /**
     * Called in {@link net.minecraft.world.level.saveddata.SavedData#save(CompoundTag, HolderLookup.Provider)} to save the data to disk
     * @param tag The NBT tag to write the data too
     * @param lookup The holder lookup provider for context
     */
    public abstract void save(CompoundTag tag, HolderLookup.Provider lookup);

    public final void loadData(CompoundTag tag, HolderLookup.Provider lookup)
    {
        if(this.isClient || this.loaded)
            return;
        this.loaded = true;
        this.load(tag,lookup);
    }

    /**
     * Called in save data deserilizer to load the data from the existing data.<br>
     * @param tag The NBT tag the data was written to in {@link #save(CompoundTag, HolderLookup.Provider)}
     * @param lookup The holder lookup provider for context
     */
    protected abstract void load(CompoundTag tag, HolderLookup.Provider lookup);

    /**
     * Public method used to call {@link #parseSyncPacket(LazyPacketData, HolderLookup.Provider)}<br>
     * Called on the logical client. Has a check to confirm that this is in-fact the client-sided data before parsing the packet.
     * @param message The message sent from the server to sync the data with your client
     */
    public final void receivePacket(LazyPacketData message)
    {
        if(this.isServer())
            return;
        this.parseSyncPacket(message,message.lookup);
    }

    /**
     * Called on the logical client to handle sync packets sent from the server
     * @param message The message sent from the server to sync the data with your client
     * @param lookup The holder lookup for context
     */
    protected abstract void parseSyncPacket(LazyPacketData message, HolderLookup.Provider lookup);

    /**
     * Called on the logical server when the data is first created/loaded from file<br>
     * Called <b>AFTER</b> {@link #load(CompoundTag, HolderLookup.Provider)} is called, so all data should be up-to-date
     */
    protected void serverInit() {}

    /**
     * Called on the logical server whenever a player joins the server<br>
     * Use to send sync packets to the newly joined player via {@link #sendSyncPacket(LazyPacketData.Builder, ServerPlayer)}
     */
    public abstract void onPlayerJoin(ServerPlayer player);

    /**
     * Use to send sync packets from this data to it's client-side counter-parts.<br>
     * Will not send a packet if run from the logical client.<br>
     * Sends the packet to all players. Use {@link #sendSyncPacket(LazyPacketData.Builder, ServerPlayer)} to send to a specific player<br>
     * Packets will be handled client-side by {@link #parseSyncPacket(LazyPacketData, HolderLookup.Provider)}
     */
    public final void sendSyncPacket(LazyPacketData.Builder builder) {
        if(this.isClient)
            return;
        new SPacketSyncCustomData(this.getType(),builder.build()).sendToAll();
    }
    /**
     * Use to send sync packets from this data to it's client-side counter-parts.<br>
     * Will not send a packet if run from the logical client.<br>
     * Only sends the packet to given player. Use {@link #sendSyncPacket(LazyPacketData.Builder)} to send to all players<br>
     * Packets will be handled client-side by {@link #parseSyncPacket(LazyPacketData, HolderLookup.Provider)}
     */
    public final void sendSyncPacket(LazyPacketData.Builder builder,ServerPlayer player) {
        if(this.isClient)
            return;
        new SPacketSyncCustomData(this.getType(),builder.build()).sendTo(player);
    }

    /**
     * Used to mark the data as changed
     */
    public final void setChanged() { this.setChanged.run(); }

    @Override
    public final LazyPacketData.Builder builder() { return LazyPacketData.builder(LookupHelper.getRegistryAccess()); }

}
