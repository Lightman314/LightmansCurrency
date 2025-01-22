package io.github.lightman314.lightmanscurrency.common.data;

import io.github.lightman314.lightmanscurrency.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTicker;
import io.github.lightman314.lightmanscurrency.api.misc.IEasyTickable;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomData;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomDataType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber
public class ClientCustomDataCache {

    private static final Map<ResourceLocation,CustomData> clientDataCache = new HashMap<>();

    @Nullable
    public static <T extends CustomData> T getData(CustomDataType<T> type)
    {
        ResourceLocation dataID = LCRegistries.CUSTOM_DATA.getKey(type);
        if(dataID == null)
        {
            LightmansCurrency.LogError("Custom Data was not registered!");
            return null;
        }
        if(!clientDataCache.containsKey(dataID))
            clientDataCache.put(dataID,type.create().initClient());

        return (T)clientDataCache.get(dataID);
    }

    @SubscribeEvent
    private static void onClientTick(ClientTickEvent.Pre event)
    {
        for(CustomData data : clientDataCache.values())
        {
            if(data instanceof IEasyTickable ticker)
                ticker.tick();
            if(data instanceof IClientTicker ticker)
                ticker.clientTick();
        }
    }

    @SubscribeEvent
    private static void onLeaveServer(ClientPlayerNetworkEvent.LoggingOut event)
    {
        clientDataCache.clear();
    }

}
