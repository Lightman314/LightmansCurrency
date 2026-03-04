package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.events.client.RegisterClientTraderAttachmentsEvent;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.client.util.ClientRegistry;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.function.Function;

@EventBusSubscriber
public class ClientTraderNode<T extends TraderNode> {

    public final T node;
    public ClientTraderNode(T node) { this.node = node; }

    private static final ClientRegistry<TraderNodeType<?>,ClientNodeFactory<?>> AUTO_NODES = new ClientRegistry<>(LCRegistries.TRADER_NODE,"Client Node Factory");

    public static <T extends TraderNode> void registerClientNode(TraderNodeType<T> type, Function<T,ClientTraderNode<T>> builder)
    {
        AUTO_NODES.register(type,new ClientNodeFactory<>(builder));
    }

    //High priority so that "default" priority event listeners will see these, but you can still access the event before the auto-nodes are present
    @SubscribeEvent(priority = EventPriority.HIGH)
    private static void addAttachments(RegisterClientTraderAttachmentsEvent event)
    {
        TraderData trader = event.getTrader();
        AUTO_NODES.forEach((type,builder) -> {
            if(trader.hasNode(type))
            {
                ClientTraderNode<?> clientNode = builder.build(trader.getNode(type));
                if(clientNode != null)
                    event.register(clientNode);
            }
        });
    }

    private record ClientNodeFactory<T extends TraderNode>(Function<T,ClientTraderNode<T>> builder)
    {
        private ClientTraderNode<?> build(TraderNode node)
        {
            try { return this.builder.apply((T)node);
            }catch (ClassCastException ignored) {}
            return null;
        }
    }

}
