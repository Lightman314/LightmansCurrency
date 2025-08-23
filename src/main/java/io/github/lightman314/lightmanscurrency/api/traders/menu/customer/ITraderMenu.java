package io.github.lightman314.lightmanscurrency.api.traders.menu.customer;

import io.github.lightman314.lightmanscurrency.api.traders.ITraderSource;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Set;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
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

}
