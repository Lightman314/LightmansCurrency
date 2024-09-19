package io.github.lightman314.lightmanscurrency.common.util;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

@EventBusSubscriber
public class LookupHelper {

    private static RegistryAccess clientAccessCache = null;
    private static RegistryAccess serverAccessCache = null;

    //Collect RegistryAccess from the TagsUpdatedEvent so that we have access to it before the level is loaded
    @SubscribeEvent
    private static void tagReloadedEvent(TagsUpdatedEvent event)
    {
        if(event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD)
            serverAccessCache = event.getRegistryAccess();
        else
            clientAccessCache = event.getRegistryAccess();
    }

    @Nonnull
    public static RegistryAccess getRegistryAccess(boolean isClient)
    {
        if(isClient)
        {
            RegistryAccess lookup = LightmansCurrency.getProxy().getClientRegistryHolder();
            return lookup != null ? lookup : clientAccessCache;
        }
        else
        {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if(server != null)
                return server.registryAccess();
            else //Get cached access, and if the cache is empty, attempt to get the client access as a fallback
                return serverAccessCache != null ? serverAccessCache : getRegistryAccess(true);
        }
    }

    @Nullable
    public static Holder<Enchantment> lookupEnchantment(@Nonnull HolderLookup.Provider lookup, @Nonnull ResourceKey<Enchantment> enchantment)
    {
        AtomicReference<Holder<Enchantment>> result = new AtomicReference<>(null);
        lookup.lookup(Registries.ENCHANTMENT).flatMap(registry -> registry.get(enchantment)).ifPresent(result::set);
        return result.get();
    }

}
