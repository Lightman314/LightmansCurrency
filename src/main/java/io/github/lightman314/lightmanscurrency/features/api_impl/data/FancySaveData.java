package io.github.lightman314.lightmanscurrency.features.api_impl.data;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.data.FancyData;
import io.github.lightman314.lightmanscurrency.api.data.FancyDataType;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerCommon;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
@EventBusSubscriber
public class FancySaveData<T extends FancyData> extends SavedData {

    private static final Map<FancyDataType<?>,FancyData> serverDataCache = new HashMap<>();

    @Nullable
    public static <T extends FancyData> T getData(FancyDataType<T> type)
    {
        if(!serverDataCache.containsKey(type))
        {
            LightmansCurrency.LogWarning("Attempted to get " + type + " before the server started!",new Throwable());
            return null;
        }
        return (T)serverDataCache.get(type);
    }

    public static boolean isLoaded(FancyDataType<?> type) { return serverDataCache.containsKey(type); }

    private static void initServerData(MinecraftServer server)
    {
        serverDataCache.clear();
        Map<FancyDataType<?>,Runnable> listeners = new HashMap<>();
        for(FancyDataType<?> type : LCRegistries.Data.FANCY_DATA)
        {
            FancySaveData<?> data = server.getDataStorage().computeIfAbsent(buildType(type));
            serverDataCache.put(type,data.data);
            listeners.put(type,data::setDirty);
        }
        //Run server-init **after** all data is fully loaded so that any cross-data initialization will function properly
        serverDataCache.forEach((type,data) -> data.initServer(listeners.get(type),server));
    }

    private static <T extends FancyData> SavedDataType<FancySaveData<T>> buildType(FancyDataType<T> type)
    {
        return new SavedDataType<>(
                LCRegistries.Data.FANCY_DATA.getKey(type),
                () -> new FancySaveData<>(type.create()),
                type.getCodec().xmap(FancySaveData::new,d -> d.data));
    }

    private final T data;
    private FancySaveData(T data) { this.data = data; }

    @SubscribeEvent
    private static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if(event.getEntity() instanceof ServerPlayer sp)
        {
            for(FancyData data : serverDataCache.values())
                data.onPlayerJoin(sp);
        }
    }

    @SubscribeEvent
    private static void onServerTick(ServerTickEvent.Pre event)
    {
        for(FancyData data : serverDataCache.values())
        {
            if(data instanceof ITickerCommon t)
                t.tick();
            if(data instanceof ITickerServer t)
                t.serverTick();
        }
    }

    @SubscribeEvent
    private static void afterServerTick(ServerTickEvent.Post event)
    {
        for(FancyData data : serverDataCache.values())
            data.syncTick();
    }

    @SubscribeEvent
    private static void onServerStart(ServerStartedEvent event)
    {
        initServerData(event.getServer());
    }

    @SubscribeEvent
    private static void onServerStop(ServerStoppedEvent event)
    {
        for(FancyData data : serverDataCache.values())
            data.onServerShutdown();
        //Clear the cache when the server stops
        serverDataCache.clear();
    }

}