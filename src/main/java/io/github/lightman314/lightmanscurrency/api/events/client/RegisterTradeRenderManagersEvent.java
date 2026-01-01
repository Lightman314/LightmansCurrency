package io.github.lightman314.lightmanscurrency.api.events.client;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeRenderManager;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class RegisterTradeRenderManagersEvent extends Event implements IModBusEvent {

    private final Map<Class<? extends TradeData>,Function<TradeData,TradeRenderManager<?>>> results = new HashMap<>();
    public Map<Class<? extends TradeData>,Function<TradeData,TradeRenderManager<?>>> getResults() { return ImmutableMap.copyOf(this.results); }

    private static <T extends TradeData> Function<TradeData,TradeRenderManager<?>> castFunction(Function<T,TradeRenderManager<? extends T>> builder) {
        return (t) -> builder.apply((T)t);
    }

    @SafeVarargs
    public final <T extends TradeData> void register(Function<T,TradeRenderManager<? extends T>> builder,Class<? extends T>... dataClass)
    {
        Function<TradeData,TradeRenderManager<?>> b = castFunction(builder);
        for(Class<? extends T> clazz : dataClass)
        {
            if(this.results.put(clazz,b) != null)
                LightmansCurrency.LogWarning("Duplicate Trade Render Manager was registered for " + clazz.getName());
        }
    }

}