package io.github.lightman314.lightmanscurrency.integration;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraftforge.fml.ModList;
import org.apache.commons.lang3.ObjectUtils;

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

    public static Runnable SafeEnqueueWork(@Nonnull String modid, @Nonnull Runnable runnable, @Nullable String error) { return () -> SafeRunIfLoaded(modid, runnable, error); }

}
