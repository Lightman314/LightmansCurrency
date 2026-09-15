package io.github.lightman314.lightmanscurrency.client;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.events.RegisterClientMenuTabEvent;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed.ClientMenuTab;
import io.github.lightman314.lightmanscurrency.api.coins.atm.client.ATMIconRenderer;
import io.github.lightman314.lightmanscurrency.api.coins.atm.client.builtin.*;
import io.github.lightman314.lightmanscurrency.api.coins.atm.icons.builtin.*;
import io.github.lightman314.lightmanscurrency.api.coins.client.ClientCoinType;
import io.github.lightman314.lightmanscurrency.api.coins.value.CoinValue;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.MultiIcon;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.icon.client.IconRenderer;
import io.github.lightman314.lightmanscurrency.api.icon.client.builtin.ItemRenderer;
import io.github.lightman314.lightmanscurrency.api.icon.client.builtin.MultiRenderer;
import io.github.lightman314.lightmanscurrency.api.icon.client.builtin.SpriteRenderer;
import io.github.lightman314.lightmanscurrency.api.money.client.ClientMoneyValueType;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.builtin.ClientDisplayNode;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.builtin.ClientNotificationNode;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.builtin.ClientOwnerNode;
import io.github.lightman314.lightmanscurrency.api.trader.client.trade.TradeButtonDisplay;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.customer.TraderCustomerScreen;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.customer.builtin.NormalCustomerClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.MoneyStorageClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.SimpleTradeEditClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.info.InfoClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules.RuleClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules.builtin.*;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.SettingsClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.DisplayNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.NotificationNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.OwnerNode;
import io.github.lightman314.lightmanscurrency.api.trader.rules.builtin.*;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin.ItemPrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin.MoneyPrice;
import io.github.lightman314.lightmanscurrency.api.trader.client.trade.price.ClientTradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.client.trade.price.builtin.ItemPriceClient;
import io.github.lightman314.lightmanscurrency.api.trader.client.trade.price.builtin.MoneyPriceClient;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.AbstractTabbedCustomerMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.builtin.NormalCustomerTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.InfoTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.MoneyStorageTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.SettingsTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.SimpleTradeEditTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.rules.GlobalTradeRuleTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.rules.TradeTradeRuleTab;
import io.github.lightman314.lightmanscurrency.client.features.atm.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.features.atm.tabs.CoinExchangeClientTab;
import io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.RotationHandler;
import io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.rotation.*;
import io.github.lightman314.lightmanscurrency.client.features.trader.item.ItemTradeEditClientTab;
import io.github.lightman314.lightmanscurrency.client.features.trader.item.block_entity.ItemTraderBlockEntityRenderer;
import io.github.lightman314.lightmanscurrency.client.proxy.LCClientProxy;
import io.github.lightman314.lightmanscurrency.core.LCBlockEntities;
import io.github.lightman314.lightmanscurrency.core.LCMenuTypes;
import io.github.lightman314.lightmanscurrency.features.atm.tabs.CoinExchangeTab;
import io.github.lightman314.lightmanscurrency.features.trader.item.menu.ItemTradeEditTab;
import io.github.lightman314.lightmanscurrency.features.trader.item.trade.ArmorTradeData;
import io.github.lightman314.lightmanscurrency.features.trader.item.trade.ItemTradeData;
import io.github.lightman314.lightmanscurrency.features.trader.item.trade.ItemTradeButtonDisplay;
import io.github.lightman314.lightmanscurrency.features.trader.item_common.ItemStorageTab;
import io.github.lightman314.lightmanscurrency.client.features.trader.item_common.ItemStorageClientTab;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = LCApi.MODID,dist = Dist.CLIENT)
@EventBusSubscriber(Dist.CLIENT)
public class LightmansCurrencyClient {

    public LightmansCurrencyClient(ModContainer container) {
        //Initialize the client proxy
        LCClientProxy.initialize();
        //Setup client shenanigans like the config screens
    }

    //Setup misc client things
    @SubscribeEvent
    private static void clientSetup(FMLClientSetupEvent event)
    {

        //Setup Rotation Handler
        RotationHandler.initialize();

        //Register Block Entity Renderers
        BlockEntityRenderers.register(LCBlockEntities.ITEM_TRADER.get(),ItemTraderBlockEntityRenderer.PROVIDER);

        //Register Icon Renderers
        IconRenderer.REGISTRY.register(ItemIcon.TYPE,ItemRenderer.INSTANCE);
        IconRenderer.REGISTRY.register(SpriteIcon.TYPE,SpriteRenderer.INSTANCE);
        IconRenderer.REGISTRY.register(MultiIcon.TYPE,MultiRenderer.INSTANCE);

        //Register ATM Icon Renderers
        ATMIconRenderer.REGISTRY.register(ATMArrowIcon.TYPE,ATMArrowIconRenderer.INSTANCE);
        ATMIconRenderer.REGISTRY.register(ATMItemIcon.TYPE,ATMItemIconRenderer.INSTANCE);
        ATMIconRenderer.REGISTRY.register(ATMSpriteIcon.TYPE,ATMSpriteIconRenderer.INSTANCE);

        //Initialize the Client Menu Tabs
        ClientMenuTab.initialize();

        //Register Trade Rule Tabs
        RuleClientTab.TAB_BUILDERS.register(FreeSample.TYPE,FreeSampleClientTab::new);
        RuleClientTab.TAB_BUILDERS.register(PlayerTradeLimit.TYPE,PlayerTradeLimitClientTab::new);

        //Register Trade Displays
        TradeButtonDisplay.REGISTRY.register(ItemTradeData.TYPE, ItemTradeButtonDisplay.INSTANCE);
        TradeButtonDisplay.REGISTRY.register(ArmorTradeData.TYPE, ItemTradeButtonDisplay.INSTANCE);

        //Register Trade Price Displays
        ClientTradePrice.REGISTRY.register(MoneyPrice.TYPE, MoneyPriceClient.INSTANCE);
        ClientTradePrice.REGISTRY.register(ItemPrice.TYPE, ItemPriceClient.INSTANCE);

        //Register Client Trader Nodes
        ClientTraderNode.REGISTRY.register(OwnerNode.TYPE,ClientOwnerNode.INSTANCE);
        ClientTraderNode.REGISTRY.register(DisplayNode.TYPE,ClientDisplayNode.INSTANCE);
        ClientTraderNode.REGISTRY.register(NotificationNode.TYPE,ClientNotificationNode.INSTANCE);

        //Register Money Price Displays
        ClientMoneyValueType.REGISTRY.register(CoinValue.TYPE,ClientCoinType.INSTANCE);

    }

    @SubscribeEvent
    private static void registerRotationHandlers(RegisterRotationHandlerEvent event) {
        //Register Rotation Handlers
        event.register(LCApi.id("spinning"),SpinningRotation.MAP_CODEC);
        event.register(LCApi.id("facing"),FacingRotation.MAP_CODEC);
        event.register(LCApi.id("facing_up"),FacingUpRotation.MAP_CODEC);
    }

    @SubscribeEvent
    private static void registerClientMenuTabs(RegisterClientMenuTabEvent event) {
        //ATM Tabs
        event.forMenu(LCMenuTypes.ATM,ATMScreen.class)
                .register(CoinExchangeTab.CLIENT_KEY,CoinExchangeClientTab.BUILDER);
        //Trader Customer Menu
        event.forMenu(AbstractTabbedCustomerMenu.MENU_KEY,AbstractTabbedCustomerMenu.class,TraderCustomerScreen.class)
                .register(NormalCustomerTab.CLIENT_KEY,NormalCustomerClientTab.BUILDER);
        //Trader Storage Menu
        event.forMenu(LCMenuTypes.TRADER_STORAGE,TraderStorageScreen.class)
                .register(SimpleTradeEditTab.KEY,SimpleTradeEditClientTab.BUILDER)
                .register(ItemStorageTab.KEY,ItemStorageClientTab.BUILDER)
                .register(ItemTradeEditTab.KEY,ItemTradeEditClientTab.BUILDER)
                .register(MoneyStorageTab.KEY,MoneyStorageClientTab.BUILDER)
                .register(SettingsTab.KEY,SettingsClientTab.BUILDER)
                .register(GlobalTradeRuleTab.KEY,TradeRulesClientTab.BUILDER)
                .register(TradeTradeRuleTab.KEY,TradeRulesClientTab.BUILDER)
                .register(InfoTab.KEY,InfoClientTab.BUILDER);
    }

}