package io.github.lightman314.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.BuiltInPermissions;
import io.github.lightman314.lightmanscurrency.core.LCRegistrySetup;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.ParallelDispatchEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

@Mod(LCApi.MODID)
public class LightmansCurrency {

    private static final Logger LOGGER = LogManager.getLogger();

    public LightmansCurrency(ModContainer modContainer,IEventBus eventBus)
    {
        //Setup Wood Types early

        //Setup Config system
        LCConfig.init();
        //Register stuff I guess :shrug:
        //Might be a little important
        LCRegistrySetup.initialize(eventBus);
    }

    @SubscribeEvent
    private void commonSetup(FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            BuiltInPermissions.intialize();
            //TODO other stuff
        });
    }

    public static void LogDebug(String message) { LOGGER.debug(message); }
    public static void LogDebug(String message, Object... objects) { LOGGER.debug(message, objects); }

    public static void LogInfo(String message) { LOGGER.info(message); }

    public static void LogInfo(String message, Object... objects) { LOGGER.info(message, objects); }

    public static void LogWarning(String message) { LOGGER.warn(message); }

    public static void LogWarning(String message, Object... objects) { LOGGER.warn(message, objects); }

    public static void LogError(String message) { LOGGER.error(message); }

    public static void LogError(String message, Object... objects) { LOGGER.error(message, objects); }

    public static void safeEnqueueWork(ParallelDispatchEvent event, String errorMessage, Runnable work) {
        event.enqueueWork(() -> {
            try{
                work.run();
            } catch(Throwable t) {
                LogError(errorMessage, t);
            }
        });
    }

    public static <T extends ParallelDispatchEvent> void safeEnqueueWork(T event, String errorMessage, Consumer<T> work) {
        event.enqueueWork(() -> {
            try{
                work.accept(event);
            } catch(Throwable t) {
                LogError(errorMessage, t);
            }
        });
    }

}
