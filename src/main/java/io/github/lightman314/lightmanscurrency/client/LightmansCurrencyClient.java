package io.github.lightman314.lightmanscurrency.client;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.events.RegisterClientMenuTabEvent;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed.ClientMenuTab;
import io.github.lightman314.lightmanscurrency.api.coins.atm.client.ATMIconRenderer;
import io.github.lightman314.lightmanscurrency.api.coins.atm.client.builtin.*;
import io.github.lightman314.lightmanscurrency.api.coins.atm.icons.builtin.*;
import io.github.lightman314.lightmanscurrency.api.coins.client.ClientCoinType;
import io.github.lightman314.lightmanscurrency.api.coins.value.CoinValue;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.ConfigSelectionScreen;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.MultiIcon;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.TextIcon;
import io.github.lightman314.lightmanscurrency.api.icon.client.IconRenderer;
import io.github.lightman314.lightmanscurrency.api.icon.client.builtin.ItemIconRenderer;
import io.github.lightman314.lightmanscurrency.api.icon.client.builtin.MultiIconRenderer;
import io.github.lightman314.lightmanscurrency.api.icon.client.builtin.SpriteIconRenderer;
import io.github.lightman314.lightmanscurrency.api.icon.client.builtin.TextIconRenderer;
import io.github.lightman314.lightmanscurrency.api.money.client.ClientMoneyValueType;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.builtin.*;
import io.github.lightman314.lightmanscurrency.api.trader.client.permissions.ClientPermission;
import io.github.lightman314.lightmanscurrency.api.trader.client.permissions.builtin.*;
import io.github.lightman314.lightmanscurrency.api.trader.client.trade.TradeButtonDisplay;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.customer.TraderCustomerScreen;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.customer.builtin.NormalCustomerClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.*;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.info.InfoClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules.*;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules.builtin.*;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.SettingsClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.terminal.TradingTerminalScreen;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.terminal.builtin.NetworkTraderSelectionClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.*;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.builtin.BooleanPermissionType;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.builtin.EnumPermissionType;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.builtin.IntegerPermissionType;
import io.github.lightman314.lightmanscurrency.api.trader.rules.builtin.*;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin.*;
import io.github.lightman314.lightmanscurrency.api.trader.client.trade.price.ClientTradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.client.trade.price.builtin.*;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.AbstractTabbedCustomerMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.builtin.*;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.*;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.rules.*;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.terminal.builtin.*;
import io.github.lightman314.lightmanscurrency.client.features.atm.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.features.atm.tabs.*;
import io.github.lightman314.lightmanscurrency.client.features.player_data.PlayerDataScreen;
import io.github.lightman314.lightmanscurrency.client.features.player_data.tabs.*;
import io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.RotationHandler;
import io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.rotation.*;
import io.github.lightman314.lightmanscurrency.client.features.trader.item.ItemTradeEditClientTab;
import io.github.lightman314.lightmanscurrency.client.features.trader.item.block_entity.*;
import io.github.lightman314.lightmanscurrency.client.features.trader.item.nodes.ClientItemStorageNode;
import io.github.lightman314.lightmanscurrency.client.proxy.LCClientProxy;
import io.github.lightman314.lightmanscurrency.core.LCBlockEntities;
import io.github.lightman314.lightmanscurrency.core.LCMenuTypes;
import io.github.lightman314.lightmanscurrency.features.atm.tabs.*;
import io.github.lightman314.lightmanscurrency.features.player_data.tabs.*;
import io.github.lightman314.lightmanscurrency.features.player_data.tabs.PlayerStatsTab;
import io.github.lightman314.lightmanscurrency.features.trader.gacha.nodes.GachaStorageNode;
import io.github.lightman314.lightmanscurrency.features.trader.item.menu.ItemTradeEditTab;
import io.github.lightman314.lightmanscurrency.features.trader.item.trade.*;
import io.github.lightman314.lightmanscurrency.features.trader.item_common.*;
import io.github.lightman314.lightmanscurrency.client.features.trader.item_common.ItemStorageClientTab;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = LCApi.MODID,dist = Dist.CLIENT)
@EventBusSubscriber(Dist.CLIENT)
public class LightmansCurrencyClient {

    public LightmansCurrencyClient(ModContainer container) {
        //Initialize the client proxy
        LCClientProxy.initialize();
        //Setup client shenanigans like the config screens
        container.registerExtensionPoint(IConfigScreenFactory.class,ConfigSelectionScreen.mixedFactory(
                LCConfig.SERVER,LCConfig.COMMON,LCConfig.CLIENT));
    }

    //Setup misc client things
    @SubscribeEvent
    private static void clientSetup(FMLClientSetupEvent event)
    {

        //Setup Rotation Handler
        RotationHandler.initialize();

        //Register Block Entity Renderers
        BlockEntityRenderers.register(LCBlockEntities.ITEM_TRADER.get(),ItemTraderBlockEntityRenderer.PROVIDER);
        BlockEntityRenderers.register(LCBlockEntities.ARMOR_TRADER.get(), ArmorDisplayBlockEntityRenderer.PROVIDER);

        //Register Icon Renderers
        IconRenderer.REGISTRY.register(ItemIcon.TYPE, ItemIconRenderer.INSTANCE);
        IconRenderer.REGISTRY.register(SpriteIcon.TYPE, SpriteIconRenderer.INSTANCE);
        IconRenderer.REGISTRY.register(TextIcon.TYPE,TextIconRenderer.INSTANCE);
        IconRenderer.REGISTRY.register(MultiIcon.TYPE, MultiIconRenderer.INSTANCE);

        //Register ATM Icon Renderers
        ATMIconRenderer.REGISTRY.register(ATMArrowIcon.TYPE,ATMArrowIconRenderer.INSTANCE);
        ATMIconRenderer.REGISTRY.register(ATMItemIcon.TYPE,ATMItemIconRenderer.INSTANCE);
        ATMIconRenderer.REGISTRY.register(ATMSpriteIcon.TYPE,ATMSpriteIconRenderer.INSTANCE);

        //Initialize the Client Menu Tabs
        ClientMenuTab.initialize();

        //Register Trade Rule Tabs
        RuleClientTab.TAB_BUILDERS.register(DailyTrades.TYPE,DailyTradesClientTab::new);
        RuleClientTab.TAB_BUILDERS.register(DemandPricing.TYPE,DemandPricingClientTab::new);
        RuleClientTab.TAB_BUILDERS.register(FreeSample.TYPE,FreeSampleClientTab::new);
        RuleClientTab.TAB_BUILDERS.register(GlobalTradeLimit.TYPE,GlobalTradeLimitClientTab::new);
        RuleClientTab.TAB_BUILDERS.register(PriceFluctuation.TYPE,PriceFluctuationClientTab::new);
        RuleClientTab.TAB_BUILDERS.register(PlayerDiscounts.TYPE,PlayerDiscountsClientTab::new);
        RuleClientTab.TAB_BUILDERS.register(PlayerInteractionListing.TYPE,PlayerInteractionListingClientTab::new);
        RuleClientTab.TAB_BUILDERS.register(PlayerTradeLimit.TYPE,PlayerTradeLimitClientTab::new);
        RuleClientTab.TAB_BUILDERS.register(TimedSale.TYPE,TimedSaleClientTab::new);

        //Register Trade Displays
        TradeButtonDisplay.REGISTRY.register(ItemTradeData.TYPE, ItemTradeButtonDisplay.INSTANCE);
        TradeButtonDisplay.REGISTRY.register(ArmorTradeData.TYPE, ItemTradeButtonDisplay.INSTANCE);

        //Register Trade Price Displays
        ClientTradePrice.REGISTRY.register(MoneyPrice.TYPE,MoneyPriceClient.INSTANCE);
        ClientTradePrice.REGISTRY.register(ItemPrice.TYPE,ItemPriceClient.INSTANCE);

        //Register Client Permissions
        ClientPermission.REGISTRY.register(BooleanPermissionType.INSTANCE,ClientBooleanPermission.INSTANCE);
        ClientPermission.REGISTRY.register(IntegerPermissionType.INSTANCE,ClientIntegerPermission.INSTANCE);
        ClientPermission.REGISTRY.register(EnumPermissionType.TRI_STATE,ClientEnumPermission.TRI_STATE);

        //Register Client Trader Nodes
        ClientTraderNode.REGISTRY.register(OwnerNode.TYPE,ClientOwnerNode.INSTANCE);
        ClientTraderNode.REGISTRY.register(DisplayNode.TYPE,ClientDisplayNode.INSTANCE);
        ClientTraderNode.REGISTRY.register(NotificationNode.TYPE,ClientNotificationNode.INSTANCE);
        ClientTraderNode.REGISTRY.register(TraderStatsNode.TYPE,ClientTraderStatsNode.INSTANCE);
        ClientTraderNode.REGISTRY.register(ExternalInteractionsNode.TYPE,ClientExternalInteractionsNode.INSTANCE);
        ClientTraderNode.REGISTRY.register(ItemStorageNode.TYPE,ClientItemStorageNode.INSTANCE);
        ClientTraderNode.REGISTRY.register(GachaStorageNode.TYPE,ClientItemStorageNode.INSTANCE);
        ClientTraderNode.REGISTRY.register(AdminSettingsNode.TYPE,ClientAdminSettingsNode.INSTANCE);
        ClientTraderNode.REGISTRY.register(AlliesNode.TYPE,ClientAlliesNode.INSTANCE);

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
                .register(CoinExchangeTab.CLIENT_KEY,CoinExchangeClientTab.BUILDER)
                .register(AccountSelectionTab.CLIENT_KEY,AccountSelectionClientTab.BUILDER)
                .register(AccountInteractionTab.CLIENT_KEY,AccountInteractionClientTab.BUILDER)
                .register(AccountSettingsTab.CLIENT_KEY,AccountSettingsClientTab.BUILDER)
                .register(AccountLogsTab.CLIENT_KEY,AccountLogsClientTab.BUILDER)
                .register(MoneyTransferTab.CLIENT_KEY,MoneyTransferClientTab.BUILDER);
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
                .register(SettingsClipboardTab.KEY,SettingsClipboardClientTab.BUILDER)
                .register(GlobalTradeRuleTab.KEY,TradeRulesClientTab.BUILDER)
                .register(TradeTradeRuleTab.KEY,TradeRulesClientTab.BUILDER)
                .register(InfoTab.KEY,InfoClientTab.BUILDER);
        //Terminal Menu
        event.forMenu(LCMenuTypes.TRADING_TERMINAL,TradingTerminalScreen.class)
                .register(NetworkTraderSelectionTab.KEY,NetworkTraderSelectionClientTab.BUILDER);
        //Player Data Menu
        event.forMenu(LCMenuTypes.PLAYER_DATA,PlayerDataScreen.class)
                .register(NotificationTab.CLIENT_KEY,NotificationClientTab.BUILDER)
                .register(PlayerStatsTab.CLIENT_KEY,PlayerStatsClientTab.BUILDER);

    }

}