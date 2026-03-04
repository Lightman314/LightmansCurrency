package io.github.lightman314.lightmanscurrency.api.traders.client;

import io.github.lightman314.lightmanscurrency.api.events.client.RegisterClientTraderAttachmentsEvent;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;
import java.util.function.Consumer;

public class TraderClientHooks {

    public static List<Object> collectClientAttachments(TraderData trader) {
        return NeoForge.EVENT_BUS.post(new RegisterClientTraderAttachmentsEvent(trader)).getResults();
    }

    public static <T> void forEach(TraderData trader,Class<T> type,Consumer<T> action)
    {
        for(Object ca : trader.getClientAttachments())
        {
            if(type.isInstance(ca))
                action.accept(type.cast(ca));
        }
    }

    public static void forEach(TraderData trader,Consumer<Object> action)
    {
        for(Object ca : trader.getClientAttachments())
            action.accept(ca);
    }

}