package io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerServer;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.builtin.EmptyMoneyResource;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderSource;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.MoneyStorageNode;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.managers.MenuTrackingManager;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.TrackingLevel;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeCustomer;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeIndexes;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeResult;
import io.github.lightman314.lightmanscurrency.api.world.menu.MessageMenu;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

public abstract class AbstractCustomerMenu extends MessageMenu.Validated implements TraderCustomerMenu, ITickerServer {

    private final TraderSource traderSource;
    @Override
    public final TraderSource getTraderSource() { return this.traderSource; }
    @Override
    public TradeCustomer getCustomer() { return TradeCustomer.of(this.getPlayer()); }
    @Override
    public RandomSource getRandom() { return this.getPlayer().getRandom(); }

    private boolean isSourceValid() { return !this.getTraders().isEmpty(); }

    private final MenuTrackingManager trackingManager;

    protected AbstractCustomerMenu(@Nullable MenuType<?> menuType,int containerId,Player player,MenuValidator validator,TraderSource source) {
        super(menuType, containerId, player, validator);
        this.traderSource = source;
        this.addCheck(this::isSourceValid);
        this.trackingManager = new MenuTrackingManager(this.traderSource,player,TrackingLevel.CUSTOMER);
    }

    public void quickCollectMoney() {
        if(this.isClient())
        {
            this.sendToServer(FancyPacketMap.flag("CollectMoney"));
            return;
        }
        if(!this.canQuickCollectMoney())
            return;
        TraderData trader = this.getSimpleTrader();
        if(trader != null && trader.hasNode(MoneyStorageNode.TYPE))
        {
            MoneyResourceHandler storage = trader.getNodeValue(MoneyStorageNode.TYPE,MoneyStorageNode::getStorage,EmptyMoneyResource.INSTANCE);
            MoneyResourceHandler playersMoney = LCApi.getMoneyAPI().getPlayersMoneyHandler(this.getPlayer());
            try(Transaction transaction = Transaction.openRoot()) {
                boolean success = false;
                for(MoneyValue storedMoney : storage.getAllResources()) {
                    try(Transaction tx = Transaction.openRoot()) {
                        MoneyValue inserted = playersMoney.insert(storedMoney,tx);
                        //If the money couldn't be inserted, don't bother continuing for this
                        if(inserted.isEmpty())
                            continue;
                        MoneyValue extracted = storage.extract(inserted,tx);
                        if(inserted.equals(extracted))
                        {
                            tx.commit();
                            success = true;
                        }
                    }
                }
                if(success) //Only commit if at least one of the collections actually succeeded
                    transaction.commit();
            }
        }
    }

    @Override
    public void openStorage() {
        if(this.isClient())
        {
            this.sendToServer(FancyPacketMap.flag("OpenStorage"));
            return;
        }
        if(!this.canOpenStorage())
            return;
        TraderData trader = this.getSimpleTrader();
        if(trader != null)
            trader.openStorageMenu(this.getPlayer(),this.getValidator(),false);
    }

    @Override
    public void openTerminal() {
        if(this.isClient())
        {
            this.sendToServer(FancyPacketMap.flag("OpenTerminal"));
            return;
        }
        //TODO actually open the terminal menu
        LightmansCurrency.LogDebug("Not yet implemented");
    }

    @Override
    public void attemptTrade(TradeIndexes location)
    {
        if(this.isClient())
        {
            this.sendToServer(FancyPacketMap.map()
                    .setList("AttemptTrade",LCFancyPacketTypes.INT, location.asData()));
            return;
        }
        TraderData t = location.getTrader(this.traderSource);
        if(t == null)
            return;
        TradeResult result = t.attemptTrade(this.getContext(t),location.nodeIndex(),location.tradeIndex());
        if(result.isFailure()) //Log the failure
            LightmansCurrency.LogDebug("Trade Failed: " + result.getMessage().getString());
    }

    @Override
    public void handleMessage(FancyPacketMap message) {
        if(message.contains("CollectMoney"))
            this.quickCollectMoney();
        if(message.contains("AttemptTrade"))
        {
            TradeIndexes location = TradeIndexes.fromData(message.getList("AttemptTrade",LCFancyPacketTypes.INT));
            if(location != null)
                this.attemptTrade(location);
        }
        if(message.contains("OpenStorage"))
            this.openStorage();
        if(message.contains("OpenTerminal"))
            this.openTerminal();
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void removed(Player player) {
        super.removed(player);
        this.trackingManager.onClose();
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void serverTick() { this.trackingManager.tick(); }

}
