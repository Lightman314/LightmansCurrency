package io.github.lightman314.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.bank_account.source.builtin.PlayerBankSource;
import io.github.lightman314.lightmanscurrency.core.RegistrySetup;
import io.github.lightman314.lightmanscurrency.features.loot.LootManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(LCApi.MODID)
@EventBusSubscriber
public class LightmansCurrency {

    private static final Logger LOGGER = LogManager.getLogger();

    public LightmansCurrency(ModContainer modContainer,IEventBus eventBus)
    {
        //Setup Config system
        LCConfig.init();
        //Register stuff I guess :shrug:
        //Might be a little important
        RegistrySetup.initialize(eventBus);
        //Initialize the Loot Manager
        LootManager.INSTANCE.init();
    }

    public static void LogDebug(String message) { LOGGER.debug(message); }
    public static void LogDebug(String message, Object... objects) { LOGGER.debug(message, objects); }

    public static void LogInfo(String message) { LOGGER.info(message); }

    public static void LogInfo(String message, Object... objects) { LOGGER.info(message, objects); }

    public static void LogWarning(String message) { LOGGER.warn(message); }

    public static void LogWarning(String message, Object... objects) { LOGGER.warn(message, objects); }

    public static void LogError(String message) { LOGGER.error(message); }

    public static void LogError(String message, Object... objects) { LOGGER.error(message, objects); }

    @SubscribeEvent
    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            //TODO Team Bank Source
            LCApi.getBankAPI().registerBankAccountSource(PlayerBankSource.INSTANCE);
        });
    }

}
