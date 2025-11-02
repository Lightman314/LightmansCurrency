package io.github.lightman314.lightmanscurrency.api.config;

import io.github.lightman314.lightmanscurrency.common.impl.ConfigAPIImpl;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ConfigAPI {

    private static ConfigAPI instance;
    public static ConfigAPI getApi()
    {
        if(instance == null)
            instance = new ConfigAPIImpl();
        return instance;
    }

    protected ConfigAPI() { if(instance != null) throw new IllegalCallerException("Cannot create a new ConfigAPI instance as one is already present!"); }

    public abstract void registerCustomReloadable(ConfigReloadable reloadable);
    public abstract List<ConfigReloadable> getReloadablesInOrder();

}