package io.github.lightman314.lightmanscurrency.common.util;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

public class LookupHelper {

    @Nonnull
    public static RegistryAccess getRegistryAccess(boolean isClient)
    {
        if(isClient)
            return LightmansCurrency.PROXY.getClientRegistryHolder();
        else
        {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if(server != null)
                return server.registryAccess();
            else
                return LightmansCurrency.PROXY.getClientRegistryHolder();
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
