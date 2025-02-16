package io.github.lightman314.lightmanscurrency.api.config.event;

import io.github.lightman314.lightmanscurrency.api.config.SyncedConfigFile;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SyncedConfigEvent extends ConfigEvent {

    private final SyncedConfigFile file;
    public SyncedConfigFile getConfig() { return this.file; }

    public SyncedConfigEvent(SyncedConfigFile file) { super(file); this.file = file; }

    /**
     * Called on the logical client whenever a {@link SyncedConfigFile} receives a sync packet from the server<br>
     * Has a {@link Pre Pre} and {@link Post Post} variant that is called before & after the sync data is processed/loaded
     */
    public static abstract class ConfigReceivedSyncDataEvent extends SyncedConfigEvent
    {
        public ConfigReceivedSyncDataEvent(SyncedConfigFile file) { super(file); }

        public static class Pre extends ConfigReceivedSyncDataEvent
        {
            public Pre(SyncedConfigFile file) { super(file); }
        }

        public static class Post extends ConfigReceivedSyncDataEvent
        {
            public Post(SyncedConfigFile file) { super(file); }
        }

    }

}
