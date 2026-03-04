package io.github.lightman314.lightmanscurrency.api.traders.menu.customer;

import io.github.lightman314.lightmanscurrency.api.traders.ITraderSource;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public interface ITraderMenu extends IClientTracker {

    @Nullable
    ITraderSource getTraderSource();
    Player getPlayer();
    TradeContext getContext(@Nullable TraderData trader);
    List<Slot> getSlots();
    ItemStack getHeldItem();
    void setHeldItem(ItemStack stack);
    Set<String> getTypedDiscountCodes();
    void submitDiscountCode(String code);
    void removeDiscountCode(String code);
    void clearDiscountCodes();

    void ExecuteTrade(int traderIndex,int tradeIndex);

}
