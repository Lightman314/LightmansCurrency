package io.github.lightman314.lightmanscurrency.util;


import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Utility class to make it easier to code certain vanilla constructors that have changed across minecraft versions
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class VersionUtil {

    public static ResourceLocation parseResource(String resourceString) { return new ResourceLocation(resourceString); }
    public static ResourceLocation modResource(String namespace, String path) { return new ResourceLocation(namespace,path); }
    public static ResourceLocation lcResource(String path) { return modResource(LightmansCurrency.MODID,path); }
    public static ResourceLocation vanillaResource(String path) { return new ResourceLocation(path); }

    public static <T extends Event> T postEvent(T event) {
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

}
