package io.github.lightman314.lightmanscurrency.common.impl;

import io.github.lightman314.lightmanscurrency.api.config.ConfigAPI;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.ConfigReloadable;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ConfigAPIImpl extends ConfigAPI {

    private final List<ConfigReloadable> customReloadables = new ArrayList<>();
    private List<ConfigReloadable> sortedReloadables = null;

    @Override
    public void registerCustomReloadable(ConfigReloadable reloadable) {
        customReloadables.add(reloadable);
        sortedReloadables = null;
    }

    @Override
    public List<ConfigReloadable> getReloadablesInOrder() {
        if(sortedReloadables == null)
        {
            //Populate the list
            sortedReloadables = new ArrayList<>(ConfigFile.getAvailableFiles());
            sortedReloadables.addAll(customReloadables);
            //Sort the list
            sortedReloadables.sort(Comparator.comparingInt(ConfigReloadable::getDelayPriority));
        }
        return sortedReloadables;
    }

}