package io.github.lightman314.lightmanscurrency.integration.discord;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class LCDiscord {

    public static void setup() {
        MinecraftForge.EVENT_BUS.register(DiscordListenerRegistration.class);
        FMLJavaModLoadingContext.get().getModEventBus().register(CurrencyMessages.class);
    }

}
