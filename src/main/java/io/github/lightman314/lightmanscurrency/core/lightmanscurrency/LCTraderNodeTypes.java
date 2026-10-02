package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.*;
import io.github.lightman314.lightmanscurrency.features.trader.gacha.nodes.GachaStorageNode;
import io.github.lightman314.lightmanscurrency.features.trader.item.nodes.ItemTradesNode;
import io.github.lightman314.lightmanscurrency.features.trader.item_common.ItemStorageNode;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCTraderNodeTypes {

    private LCTraderNodeTypes() {}

    public static final DeferredRegister<TraderNodeType<?>> REGISTER = DeferredRegister.create(LCRegistries.Trader.TRADER_NODE_TYPE,LCApi.MODID);

    static {
        //Normal/Default Nodes
        register("owner",OwnerNode.TYPE);
        register("world",WorldNode.TYPE);
        register("display",DisplayNode.TYPE);
        register("allies",AlliesNode.TYPE);
        register("money_storage",MoneyStorageNode.TYPE);
        register("upgrades",UpgradeNode.TYPE);
        register("notifications",NotificationNode.TYPE);
        register("statistics",TraderStatsNode.TYPE);
        register("trade_rules",TradeRulesNode.TYPE);
        register("settings",SettingsNode.TYPE);
        //Network Trader Nodes
        register("network",NetworkNode.TYPE);

        //Persistent Trader Nodes
        register("persistent",PersistentDataNode.TYPE);

        //Input Trader Nodes
        register("external_interactions",ExternalInteractionsNode.TYPE);

        //Item Traders
        register("item_storage",ItemStorageNode.TYPE);
        register("item_trades",ItemTradesNode.TYPE);
        register("gacha_storage",GachaStorageNode.TYPE);

    }

    private static void register(String name,TraderNodeType<?> type) {
        REGISTER.register(name,() -> type);
    }

}