package io.github.lightman314.lightmanscurrency.util;


import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Utility class to make it easier to code certain vanilla constructors that have changed across minecraft versions
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class VersionUtil {

    
    public static ResourceLocation parseResource(String resourceString) { return ResourceLocation.parse(resourceString); }
    
    public static ResourceLocation modResource(String namespace,String path) { return ResourceLocation.fromNamespaceAndPath(namespace,path); }
    
    public static ResourceLocation lcResource(String path) { return modResource(LightmansCurrency.MODID,path); }
    
    public static ResourceLocation vanillaResource(String path) { return ResourceLocation.withDefaultNamespace(path); }

    public static <T extends Event> T postEvent(T event) {
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

}