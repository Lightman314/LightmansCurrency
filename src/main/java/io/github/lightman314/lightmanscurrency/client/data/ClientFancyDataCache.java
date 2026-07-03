package io.github.lightman314.lightmanscurrency.client.data;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.data.FancyData;
import io.github.lightman314.lightmanscurrency.api.data.FancyDataType;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerClient;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerCommon;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Should never be directly called<br>
 * Use {@link FancyDataType#get(ISidedContext)} to obtain the data instances
 */
@ApiStatus.Internal
@EventBusSubscriber(Dist.CLIENT)
public final class ClientFancyDataCache {

    private static final Map<FancyDataType<?>,FancyData> clientDataCache = new HashMap<>();

    @Nullable
    public static <T extends FancyData> T getData(FancyDataType<T> type)
    {
        if(type.serverOnly)
            return null;
        if(!clientDataCache.containsKey(type))
        {
            T newInstance = type.create();
            clientDataCache.put(type,newInstance);
            //Initialize only **after** it's actually stored in the cache
            newInstance.initClient(Minecraft.getInstance().getConnection().registryAccess());
        }
        return (T)clientDataCache.get(type);
    }

    @SubscribeEvent
    private static void onClientTick(ClientTickEvent.Pre event)
    {
        for(FancyData data : new HashSet<>(clientDataCache.values()))
        {
            if(data instanceof ITickerCommon t)
                t.tick();
            if(data instanceof ITickerClient t)
                t.clientTick();
        }
    }

    @SubscribeEvent
    private static void onJoinServer(ClientPlayerNetworkEvent.LoggingIn event)
    {
        //Create a fresh instance of each data entry that should be present on the client
        for(FancyDataType<?> type : LCRegistries.Data.FANCY_DATA)
        {
            if(type.serverOnly)
                continue;
            FancyData newInstance = type.create();
            clientDataCache.put(type,newInstance);
            newInstance.initClient(event.getPlayer().registryAccess());
        }
    }

    @SubscribeEvent
    private static void onLeaveServer(ClientPlayerNetworkEvent.LoggingOut event) { clientDataCache.clear(); }

}