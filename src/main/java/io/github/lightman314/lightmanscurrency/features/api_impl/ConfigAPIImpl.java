package io.github.lightman314.lightmanscurrency.features.api_impl;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.config.ConfigAPI;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.ConfigReloadable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class ConfigAPIImpl implements ConfigAPI {

    public static final ConfigAPIImpl INSTANCE = new ConfigAPIImpl();

    private ConfigAPIImpl() {}

    private final List<ConfigReloadable> customReloadables = new ArrayList<>();
    private List<ConfigReloadable> sortedReloadables = null;


    @Override
    public void registerCustomReloadable(ConfigReloadable reloadable) {
        this.customReloadables.add(reloadable);
        this.sortedReloadables = null;
    }

    @Override
    public Collection<ConfigReloadable> getReloadablesInOrder() {
        if(this.sortedReloadables == null)
        {
            //Populate the list
            List<ConfigReloadable> temp = new ArrayList<>(ConfigFile.getAvailableFiles());
            temp.addAll(this.customReloadables);
            //Sort the list
            temp.sort(Comparator.comparingInt(ConfigReloadable::getDelayPriority));
            this.sortedReloadables = ImmutableList.copyOf(temp);
        }
        return this.sortedReloadables;
    }
}
