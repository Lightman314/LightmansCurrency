package io.github.lightman314.lightmanscurrency.features.api_impl;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.config.ConfigAPI;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.ConfigReloadable;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.*;

public final class ConfigAPIImpl implements ConfigAPI {

    public static final ConfigAPIImpl INSTANCE = new ConfigAPIImpl();

    private ConfigAPIImpl() {}

    private final Map<Identifier,ConfigReloadable> customReloadables = new HashMap<>();
    private List<ConfigReloadable> sortedReloadables = null;


    @Override
    public void registerCustomReloadable(ConfigReloadable reloadable) {
        if(this.customReloadables.containsKey(reloadable.getID())) {
            throw new IllegalArgumentException("Cannot register a reloadable with the id " + reloadable.getID() + " as one is already present!");
        }
        this.customReloadables.put(reloadable.getID(),reloadable);
        this.sortedReloadables = null;
    }

    @Nullable
    @Override
    public ConfigReloadable getReloadable(Identifier id) { return this.customReloadables.get(id); }

    @Override
    public Collection<ConfigReloadable> getReloadablesInOrder() {
        if(this.sortedReloadables == null)
        {
            //Populate the list
            List<ConfigReloadable> temp = new ArrayList<>(ConfigFile.getAvailableFiles());
            temp.addAll(this.customReloadables.values());
            //Sort the list
            temp.sort(Comparator.comparingInt(ConfigReloadable::getDelayPriority));
            this.sortedReloadables = ImmutableList.copyOf(temp);
        }
        return this.sortedReloadables;
    }
}
