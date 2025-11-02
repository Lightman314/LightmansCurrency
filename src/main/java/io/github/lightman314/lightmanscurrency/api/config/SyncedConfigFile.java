package io.github.lightman314.lightmanscurrency.api.config;

import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SyncedConfigFile extends ConfigFile {

    @Override
    public boolean isServerOnly() { return true; }

    public static void playerJoined(ServerPlayer player) {
        for(ConfigFile file : getAvailableFiles())
        {
            if(file instanceof SyncedConfigFile)
                file.sendSyncPacket(player);
        }
    }

    public static void onClientLeavesServer() {
        for(ConfigFile file : getAvailableFiles())
        {
            if(file instanceof SyncedConfigFile)
                file.clearSyncedData();
        }
    }

    protected SyncedConfigFile(String fileName, ResourceLocation id) {
        super(id,fileName,LoadPhase.GAME_START); //Lock load phase as game start to ensure the packet can be sent correctly.
    }

    private boolean loadedSyncData = false;
    @Override
    public boolean isLoaded() { return super.isLoaded() || this.loadedSyncData; }

    @Override
    public void loadSyncData(Map<String, String> syncData) {
        super.loadSyncData(syncData);
        this.loadedSyncData = true;
    }
    @Override
    public void clearSyncedData() {
        super.clearSyncedData();
        this.loadedSyncData = false;
    }
    @Override
    protected void afterReload() { this.sendSyncPacket(null); }
    @Override
    protected void afterOptionChanged(ConfigOption<?> option) { this.sendSyncPacket(null); }
}
