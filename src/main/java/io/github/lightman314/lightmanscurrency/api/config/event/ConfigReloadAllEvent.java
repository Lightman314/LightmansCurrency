package io.github.lightman314.lightmanscurrency.api.config.event;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.neoforged.bus.api.Event;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Event called whenever the <code>/lcconfig reload</code> command is run
 * or if some other method calls the {@link ConfigFile#reloadServerFiles()} or {@link ConfigFile#reloadClientFiles()} functions<br>
 * Is NOT called on the config files {@link ConfigFile#loadFiles(boolean, ConfigFile.LoadPhase) initial load}<br>
 * Posted on both the logical client & server depending on the permissions of the player
 * who triggered the reload and which config files are being reloaded<br>
 * Has a {@link Pre Pre} and {@link Post Post} variant that is called before & after the reloading occurs.
 */
public abstract class ConfigReloadAllEvent extends Event implements IClientTracker {

    private final boolean logicalClient;
    @Override
    public boolean isClient() { return this.logicalClient; }

    //Cache files that will be reloaded
    private List<ConfigFile> reloadingFiles = null;
    @Nonnull
    public List<ConfigFile> reloadedFiles() {
        if(this.reloadingFiles == null)
        {
            List<ConfigFile> results = new ArrayList<>();
            for(ConfigFile file : ConfigFile.getAvailableFiles())
            {
                if(file.shouldReload(this.logicalClient))
                    results.add(file);
            }
            this.reloadingFiles = ImmutableList.copyOf(results);
        }
        return this.reloadingFiles;
    }

    public ConfigReloadAllEvent(boolean logicalClient) { this.logicalClient = logicalClient; }

    public static class Pre extends ConfigReloadAllEvent { public Pre(boolean logicalClient) { super(logicalClient); } }
    public static class Post extends ConfigReloadAllEvent { public Post(boolean logicalClient) { super(logicalClient); } }


}
