package io.github.lightman314.lightmanscurrency.util;


import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Utility class to make it easier to code certain vanilla constructors that have changed across minecraft versions
 */
public class VersionUtil {

    @Nonnull
    public static ResourceLocation parseResource(@Nonnull String resourceString) { return new ResourceLocation(resourceString); }
    @Nonnull
    public static ResourceLocation modResource(@Nonnull String namespace, @Nonnull String path) { return new ResourceLocation(namespace,path); }
    @Nonnull
    public static ResourceLocation lcResource(@Nonnull String path) { return modResource(LightmansCurrency.MODID,path); }
    @Nonnull
    public static ResourceLocation vanillaResource(@Nonnull String path) { return new ResourceLocation(path); }

}
