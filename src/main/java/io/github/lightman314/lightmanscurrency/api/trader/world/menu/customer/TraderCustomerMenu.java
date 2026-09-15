package io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.IRegistryAccess;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.MoneyStorageNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IAdminSettingProvider;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.BuiltInPermissions;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeCustomer;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeIndexes;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.IValidatedMenu;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

public interface TraderCustomerMenu extends TraderCustomerAccess,IValidatedMenu,ISidedContext,IRegistryAccess {

    TextEntry TOOLTIP_TRADER_OPEN_STORAGE = TextEntry.tooltip(LCApi.MODID,"trader.open_storage");
    TextEntry TOOLTIP_TRADER_COLLECT_MONEY = TextEntry.tooltip(LCApi.MODID,"trader.collect_coins");
    TextEntry TOOLTIP_TRADER_NETWORK_BACK = TextEntry.tooltip(LCApi.MODID,"trader.network.back");

    Player getPlayer();
    TradeCustomer getCustomer();
    RandomSource getRandom();

    void quickCollectMoney();
    default boolean canQuickCollectMoney() {
        TraderData trader = this.getSimpleTrader();
        //TODO also ignore if the trader is linked to a bank account
        return trader != null && trader.getPermission(this.getPlayer(),BuiltInPermissions.COLLECT_MONEY) && trader.hasNode(MoneyStorageNode.TYPE) && IAdminSettingProvider.shouldStorePrice(trader);
    }

    void openStorage();
    default boolean canOpenStorage() {
        TraderData trader = this.getSimpleTrader();
        return trader != null && trader.getPermission(this.getPlayer(),BuiltInPermissions.OPEN_STORAGE);
    }

    void openTerminal();
    default boolean canOpenTerminal() { return this.getValidator().isNetworkAccess(); }

    void attemptTrade(TradeIndexes tradeIndexes);

    default TradeContext.Builder getContext(TraderData trader) { return this.buildContext(TradeContext.builder(trader,this.getCustomer(),this.getRandom(),this.getValidator().isNetworkAccess())); }
    TradeContext.Builder buildContext(TradeContext.Builder builder);

}