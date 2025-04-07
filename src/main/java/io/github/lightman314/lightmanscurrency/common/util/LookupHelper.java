package io.github.lightman314.lightmanscurrency.common.util;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

@EventBusSubscriber
public class LookupHelper {

    private static RegistryAccess backupCache = null;

    //Collect RegistryAccess from the TagsUpdatedEvent so that we have access to it before the level is loaded
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    private static void tagReloadedEvent(TagsUpdatedEvent event) { backupCache = event.getRegistryAccess(); }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    private static void villagerTradesEvent(VillagerTradesEvent event) { backupCache = event.getRegistryAccess(); }

    @Nullable
    public static RegistryAccess getRegistryAccess()
    {
        //Check server first
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server != null)
            return server.registryAccess();
        //Check client next
        RegistryAccess registryAccess = LightmansCurrency.getProxy().getClientRegistryHolder();
        return registryAccess == null ? backupCache : registryAccess;
    }

    @Nullable
    public static Holder<Enchantment> lookupEnchantment(@Nonnull HolderLookup.Provider lookup, @Nonnull ResourceKey<Enchantment> enchantment)
    {
        AtomicReference<Holder<Enchantment>> result = new AtomicReference<>(null);
        lookup.lookup(Registries.ENCHANTMENT).flatMap(registry -> registry.get(enchantment)).ifPresent(result::set);
        return result.get();
    }

}
