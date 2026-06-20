package io.github.lightman314.lightmanscurrency.client;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed.ClientMenuTab;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.icon.client.IconRenderer;
import io.github.lightman314.lightmanscurrency.api.icon.client.builtin.ItemRenderer;
import io.github.lightman314.lightmanscurrency.api.icon.client.builtin.SpriteRenderer;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.SimpleTradeEditClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.SimpleTradeEditTab;
import io.github.lightman314.lightmanscurrency.client.proxy.LCClientProxy;
import io.github.lightman314.lightmanscurrency.features.trader.misc.ItemStorageTab;
import io.github.lightman314.lightmanscurrency.features.trader.misc.client.ItemStorageClientTab;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = LCApi.MODID,dist = Dist.CLIENT)
@EventBusSubscriber
public class ClientLightmansCurrency {

    public ClientLightmansCurrency(ModContainer container) {
        //Initialize the client proxy
        LCClientProxy.initialize();
        // Setup client shenanigans like the config screens
    }

    //Setup misc client things
    @SubscribeEvent
    private static void clientSetup(FMLClientSetupEvent event)
    {
        //Register Icon Renderers
        IconRenderer.REGISTRY.register(ItemIcon.TYPE,ItemRenderer.INSTANCE);
        IconRenderer.REGISTRY.register(SpriteIcon.TYPE,SpriteRenderer.INSTANCE);

        //Register Client Menu Tabs
        ClientMenuTab.register(TraderStorageTab.defaultClientTabKey(SimpleTradeEditTab.KEY),SimpleTradeEditClientTab.BUILDER);
        ClientMenuTab.register(TraderStorageTab.defaultClientTabKey(ItemStorageTab.KEY),ItemStorageClientTab.BUILDER);

    }

}