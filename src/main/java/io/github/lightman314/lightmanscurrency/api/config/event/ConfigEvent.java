package io.github.lightman314.lightmanscurrency.api.config.event;

import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ConfigEvent extends Event {

    protected final ConfigFile file;
    public ConfigFile getConfig() { return this.file; }
    public ConfigEvent(ConfigFile file) { this.file = file; }

    /**
     * Called before a specific config file is reloaded<br>
     * Is also posted at the config files initial load (can be checked with {@link #isFirstLoad()})<br>
     * Has a {@link Pre Pre} and {@link Post Post} variant that is called before & after the config file is reloaded
     */
    public static abstract class ConfigReloadedEvent extends ConfigEvent
    {

        private final boolean isFirstLoad;
        public boolean isFirstLoad() { return this.isFirstLoad; }

        public ConfigReloadedEvent(ConfigFile file, boolean isFirstLoad) { super(file); this.isFirstLoad = isFirstLoad; }

        public static class Pre extends ConfigReloadedEvent
        {
            public Pre(ConfigFile file, boolean isFirstLoad) { super(file,isFirstLoad); }
        }

        public static class Post extends ConfigReloadedEvent
        {
            public Post(ConfigFile file, boolean isFirstLoad) { super(file,isFirstLoad); }
        }
    }

    public static abstract class ConfigReceivedSyncDataEvent extends ConfigEvent
    {
        public ConfigReceivedSyncDataEvent(ConfigFile file) { super(file); }

        public static class Pre extends ConfigReceivedSyncDataEvent
        {
            public Pre(ConfigFile file) { super(file); }
        }

        public static class Post extends ConfigReceivedSyncDataEvent
        {
            public Post(ConfigFile file) { super(file); }
        }
    }

}