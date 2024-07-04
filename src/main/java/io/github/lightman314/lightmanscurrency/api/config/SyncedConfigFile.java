package io.github.lightman314.lightmanscurrency.api.config;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.network.message.config.SPacketSyncConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class SyncedConfigFile extends ConfigFile {

    private static final Map<ResourceLocation,SyncedConfigFile> fileMap = new HashMap<>();

    @Override
    public boolean isServerOnly() { return true; }

    public static void playerJoined(@Nonnull ServerPlayer player) {
        fileMap.values().forEach(c -> c.sendSyncPacket(player));
    }

    public static void handleSyncData(@Nonnull ResourceLocation configID, @Nonnull Map<String,String> data)
    {
        if(fileMap.containsKey(configID))
            fileMap.get(configID).loadSyncData(data);
        else
            LightmansCurrency.LogError("Received config data for '" + configID + "' from the server, however this config is not present on the client!");
    }

    public static void onClientLeavesServer() { fileMap.values().forEach(SyncedConfigFile::clearSyncedData); }

    protected final ResourceLocation id;

    protected SyncedConfigFile(@Nonnull String fileName, @Nonnull ResourceLocation id) {
        super(fileName, LoadPhase.GAME_START); //Lock load phase as game start to ensure the packet can be sent correctly.
        this.id = id;
        if(fileMap.containsKey(this.id))
            throw new IllegalArgumentException("Synced Config " + this.id + " already exists!");
        fileMap.put(this.id, this);
    }


    private boolean loadedSyncData = false;
    @Override
    public boolean isLoaded() { return super.isLoaded() || this.loadedSyncData; }

    @Override
    protected void afterReload() { this.sendSyncPacket(null); }
    @Override
    protected void afterOptionChanged(@Nonnull ConfigOption<?> option) { this.sendSyncPacket(null); }

    public final void clearSyncedData() { this.forEach(ConfigOption::clearSyncedData); this.loadedSyncData = false; }

    @Nonnull
    private Map<String,String> getSyncData()
    {
        Map<String,String> map = new HashMap<>();
        this.getAllOptions().forEach((id, option) -> map.put(id, option.write()));
        return ImmutableMap.copyOf(map);
    }

    private void sendSyncPacket(@Nullable Player target) {
        if(target != null)
            new SPacketSyncConfig(this.id,this.getSyncData()).sendTo(target);
        else
            new SPacketSyncConfig(this.id,this.getSyncData()).sendToAll();
    }

    private void loadSyncData(@Nonnull Map<String,String> syncData)
    {
        LightmansCurrency.LogInfo("Received config data for '" + this.id + "' from the server!");
        this.getAllOptions().forEach((id, option) -> {
            if(syncData.containsKey(id))
                option.load(syncData.get(id), ConfigOption.LoadSource.SYNC);
            else
                LightmansCurrency.LogWarning("Received data for config option '" + id + "' but it is not present on the client!");
            this.loadedSyncData = true;
        });
    }

}
