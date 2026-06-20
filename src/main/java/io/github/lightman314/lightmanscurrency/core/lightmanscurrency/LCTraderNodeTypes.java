package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.*;
import io.github.lightman314.lightmanscurrency.features.trader.item.nodes.ItemTradesNode;
import io.github.lightman314.lightmanscurrency.features.trader.misc.ItemStorageNode;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCTraderNodeTypes {

    private LCTraderNodeTypes() {}

    public static final DeferredRegister<TraderNodeType<?>> REGISTER = DeferredRegister.create(LCRegistries.Trader.TRADER_NODE_TYPE,LCApi.MODID);

    static {
        //Normal/Default Nodes
        register("owner", OwnerNode.TYPE);
        register("world", WorldNode.TYPE);
        register("display", DisplayNode.TYPE);
        register("allies", AlliesNode.TYPE);
        register("network",NetworkNode.TYPE);
        register("upgrades", UpgradeNode.TYPE);

        //Persistent Trader Nodes

        //Input Trader Nodes


        //Item Traders
        register("item_storage",ItemStorageNode.TYPE);
        register("item_trades",ItemTradesNode.TYPE);

    }

    private static void register(String name,TraderNodeType<?> type) {
        REGISTER.register(name,() -> type);
    }

}