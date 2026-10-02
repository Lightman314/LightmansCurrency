package io.github.lightman314.lightmanscurrency.client.features.config;

import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(Dist.CLIENT)
public final class ClientConfigEvents {

    private ClientConfigEvents() {}

    @SubscribeEvent
    private static void clientSetup(FMLClientSetupEvent event) {
        ConfigFile.loadClientFiles(ConfigFile.LoadPhase.SETUP);
    }

    @SubscribeEvent
    private static void onServerJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        ConfigFile.loadClientFiles(ConfigFile.LoadPhase.GAME_START);
    }

}