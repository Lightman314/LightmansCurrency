package io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderSource;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeCustomer;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeResult;
import io.github.lightman314.lightmanscurrency.api.world.menu.MessageMenu;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import javax.annotation.Nullable;
import java.util.List;

public abstract class AbstractCustomerMenu extends MessageMenu.Validated {

    private final TraderSource traderSource;
    public final TraderSource getTraderSource() { return this.traderSource; }

    public final boolean isSimpleTrader() { return this.traderSource.isSimple(); }
    @Nullable
    public final TraderData getSimpleTrader() { return this.traderSource.getSimpleTrader(); }
    public final List<TraderData> getTraders() { return this.traderSource.getTraders(); }
    @Nullable
    public final Component getNameOverride() { return this.traderSource.getNameOverride(); }

    public final boolean forceSearch() { return this.traderSource.forceSearch(); }

    private boolean isSourceValid() { return !this.getTraders().isEmpty(); }

    protected AbstractCustomerMenu(@Nullable MenuType<?> menuType,int containerId,Player player,MenuValidator validator,TraderSource source) {
        super(menuType, containerId, player, validator);
        this.traderSource = source;
        this.addCheck(this::isSourceValid);
    }

    public void collectMoney() {
        if(this.isClient())
        {
            this.sendToServer(FancyPacketMap.newMutable().setFlag("CollectMoney"));
            return;
        }
        TraderData trader = this.getSimpleTrader();
        if(trader != null)
        {
            //TODO collect money from storage
        }
    }

    public void openStorage() {
        if(this.isClient())
        {
            this.sendToServer(FancyPacketMap.newMutable().setFlag("OpenStorage"));
            return;
        }
        TraderData trader = this.getSimpleTrader();
        if(trader != null)
            trader.openStorageMenu(this.getPlayer(),this.getValidator());
    }

    public final TradeContext.Builder buildContext(TraderData trader) { return this.buildContext(TradeContext.builder(trader,TradeCustomer.of(this.getPlayer()))); }
    public abstract TradeContext.Builder buildContext(TradeContext.Builder builder);

    public void attemptSimpleTrade(int tradeIndex) { this.attemptTrade(0,tradeIndex); }

    public void attemptTrade(int traderIndex,int tradeIndex)
    {
        if(this.isClient())
        {
            this.sendToServer(FancyPacketMap.newMutable()
                    .setList("AttemptTrade",LCFancyPacketTypes.INT,
                            ImmutableList.of(traderIndex,tradeIndex)));
            return;
        }
        List<TraderData> traders = this.getTraders();
        if(traderIndex < 0 || traderIndex >= traders.size())
            return;
        TraderData t = traders.get(traderIndex);
        TradeResult result = t.attemptTrade(this.buildContext(t),tradeIndex);
        if(result.isFailure()) //Log the failure
            LightmansCurrency.LogDebug("Trade Failed: " + result.getMessage().getString());
    }

    @Override
    public void handleMessage(FancyPacketMap message) {
        if(message.contains("CollectMoney"))
            this.collectMoney();
        if(message.contains("AttemptTrade"))
        {
            List<Integer> entry = message.getList("AttemptTrade",LCFancyPacketTypes.INT);
            if(entry.size() == 2)
                this.attemptTrade(entry.get(0),entry.get(1));
        }
        if(message.contains("OpenStorage"))
            this.openStorage();
    }

}
