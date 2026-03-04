package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber
public class ModStats {

    public static final DeferredRegister<ResourceLocation> REGISTER = DeferredRegister.create(BuiltInRegistries.CUSTOM_STAT,LightmansCurrency.MODID);

    private static final Map<ResourceLocation,StatFormatter> formatterRegistry = new HashMap<>();

    public static final ResourceLocation STAT_TRADES = registerCustom("trade_interactions",StatFormatter.DEFAULT);
    public static final ResourceLocation STAT_AUCTION_BIDS = registerCustom("auction_bids",StatFormatter.DEFAULT);
    public static final ResourceLocation STAT_AUCTION_WINS = registerCustom("auction_wins",StatFormatter.DEFAULT);

    public static ResourceLocation registerCustom(String name, StatFormatter formatter)
    {
        ResourceLocation id = LightmansCurrency.id(name);
        REGISTER.register(name, () -> id);
        formatterRegistry.put(id,formatter);
        return id;
    }

    //Initialize the formatters during the common setup as it happens after registration
    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            formatterRegistry.forEach(Stats.CUSTOM::get);
            formatterRegistry.clear();
        });
    }

}
