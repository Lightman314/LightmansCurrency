package io.github.lightman314.lightmanscurrency.common.data;

import io.github.lightman314.lightmanscurrency.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTicker;
import io.github.lightman314.lightmanscurrency.api.misc.IEasyTickable;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomData;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomDataType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(Dist.CLIENT)
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
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase != TickEvent.Phase.START)
            return;
        for(CustomData data : clientDataCache.values())
        {
            if(data instanceof IEasyTickable ticker)
                ticker.tick();
            if(data instanceof IClientTicker ticker)
                ticker.clientTick();
        }
    }

    @SubscribeEvent
    public static void onLeaveServer(ClientPlayerNetworkEvent.LoggingOut event)
    {
        clientDataCache.clear();
    }

}