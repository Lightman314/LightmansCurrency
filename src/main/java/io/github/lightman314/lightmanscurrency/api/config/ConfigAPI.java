package io.github.lightman314.lightmanscurrency.api.config;


import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.Collection;

public interface ConfigAPI {

    void registerCustomReloadable(ConfigReloadable reloadable);
    @Nullable
    ConfigReloadable getReloadable(Identifier id);
    Collection<ConfigReloadable> getReloadablesInOrder();

}
