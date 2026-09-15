package io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer;

import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderSource;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface TraderCustomerAccess {

    TraderSource getTraderSource();
    default boolean isSimpleTrader() { return this.getTraderSource().isSimple(); }
    @Nullable
    default TraderData getSimpleTrader() { return this.getTraderSource().getSimpleTrader(); }
    default List<TraderData> getTraders() { return this.getTraderSource().getTraders(); }
    @Nullable
    default Component getNameOverride() { return this.getTraderSource().getNameOverride(); }
    default boolean forceSearch() { return this.getTraderSource().forceSearch(); }

}