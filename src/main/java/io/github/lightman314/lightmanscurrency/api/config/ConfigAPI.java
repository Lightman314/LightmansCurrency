package io.github.lightman314.lightmanscurrency.api.config;


import java.util.Collection;

public interface ConfigAPI {

    void registerCustomReloadable(ConfigReloadable reloadable);
    Collection<ConfigReloadable> getReloadablesInOrder();

}
