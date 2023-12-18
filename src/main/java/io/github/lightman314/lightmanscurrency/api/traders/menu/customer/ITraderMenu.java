package io.github.lightman314.lightmanscurrency.api.traders.menu.customer;

import io.github.lightman314.lightmanscurrency.api.traders.ITraderSource;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface ITraderMenu extends IClientTracker {

    @Nullable
    ITraderSource getTraderSource();
    @Nonnull
    Player getPlayer();
    @Nonnull
    TradeContext getContext(@Nullable TraderData trader);
    @Nonnull
    List<Slot> getSlots();
    @Nonnull
    ItemStack getHeldItem();
    void setHeldItem(@Nonnull ItemStack stack);

}
