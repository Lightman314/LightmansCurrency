package io.github.lightman314.lightmanscurrency.api.data;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.IRegistryAccess;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.network.message.data.SPacketSyncFancyData;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.Objects;

public abstract class FancyData implements ISidedContext, IRegistryAccess {

    private boolean initialized = false;
    private boolean isClient = false;
    private Runnable listener = null;
    private MinecraftServer server = null;
    private HolderLookup.Provider registryAccess = null;

    @Override
    public final boolean isClient() { return this.isClient; }
    @Override
    public final HolderLookup.Provider registryAccess() { return Objects.requireNonNull(this.registryAccess, "Attempted to get the registry access before the data was initialized!"); }

    public final void initClient(HolderLookup.Provider registryAccess) {
        this.initialize(true,() -> {},registryAccess);
        this.setupCommon();
        this.setupClient();
    }
    public final void initServer(Runnable listener,MinecraftServer server) {
        this.initialize(false,listener,server.registryAccess());
        this.server = server;
        this.setupCommon();
        this.setupServer();
    }
    private void initialize(boolean isClient,Runnable listener,HolderLookup.Provider registryAccess)
    {
        if (this.initialized)
            throw new IllegalStateException("Cannot initialize the data twice!");
        this.initialized = true;
        this.isClient = isClient;
        this.listener = listener;
        this.registryAccess = registryAccess;
    }
    protected void setupClient() {}
    protected void setupServer() {}
    protected void setupCommon() {}

    public abstract FancyDataType<?> getType();

    public final void setChanged() { this.listener.run(); }

    public abstract void onPlayerJoin(ServerPlayer player);

    public abstract void syncTick();

    public void onServerShutdown() {}

    protected final Iterable<ServerPlayer> getPossibleTargets() { return this.server == null ? ImmutableList.of() : this.server.getPlayerList().getPlayers(); }

    protected final void sendPacketToAll(FancyPacketMap message) { this.sendPacket(null,message); }
    protected final void sendPacketToTarget(Player target, FancyPacketMap message) { this.sendPacket(target,message); }
    private void sendPacket(@Nullable Player target, FancyPacketMap message)
    {
        if(this.server == null)
            return;
        //Assume a change whenever a packet needs to be sent
        this.setChanged();
        SPacketSyncFancyData packet = new SPacketSyncFancyData(this.getType(),message);
        if(target == null)
            packet.sendToAll();
        else
            packet.sendTo(target);
    }

    public final void receivePacket(FancyPacketMap data)
    {
        //Only accept sync packets on the logical client
        if(this.isClient())
            this.handleSyncPacket(data);
    }

    protected abstract void handleSyncPacket(FancyPacketMap data);

}