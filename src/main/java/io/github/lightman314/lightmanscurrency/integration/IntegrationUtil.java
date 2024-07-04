package io.github.lightman314.lightmanscurrency.integration;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.neoforged.fml.ModList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IntegrationUtil {

    public static void SafeRunIfLoaded(@Nonnull String modid, @Nonnull Runnable runnable, @Nullable String error)
    {
        try {
            if(ModList.get().isLoaded(modid))
                runnable.run();
        } catch (Throwable t) { if(error != null) LightmansCurrency.LogError(error, t); }
    }

}