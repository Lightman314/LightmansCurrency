package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber
public class ModStats {

    public static void init() {}

    private static final Map<ResourceLocation,StatFormatter> formatterRegistry = new HashMap<>();

    public static final ResourceLocation STAT_TRADES;
    public static final ResourceLocation STAT_AUCTION_BIDS;
    public static final ResourceLocation STAT_AUCTION_WINS;

    static {
        STAT_TRADES = registerCustom("trade_interactions",StatFormatter.DEFAULT);
        STAT_AUCTION_BIDS = registerCustom("auction_bids",StatFormatter.DEFAULT);
        STAT_AUCTION_WINS = registerCustom("auction_wins",StatFormatter.DEFAULT);
    }

    private static ResourceLocation registerCustom(String name, StatFormatter formatter)
    {
        ResourceLocation id = VersionUtil.lcResource(name);
        ModRegistries.CUSTOM_STAT.register(name, () -> id);
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
