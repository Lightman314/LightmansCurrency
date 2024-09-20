package io.github.lightman314.lightmanscurrency.common.impl;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.types.IPlayerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.MoneyHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.List;

public class PlayerMoneyHolder extends MoneyHolder {

    private final List<IPlayerMoneyHandler> handlers;

    public PlayerMoneyHolder(@Nonnull List<IPlayerMoneyHandler> handlers) {
        this.handlers = ImmutableList.copyOf(handlers);
    }

    @Nonnull
    public PlayerMoneyHolder updatePlayer(@Nonnull Player player) {
        for(IPlayerMoneyHandler handler : this.handlers)
            handler.updatePlayer(player);
        return this;
    }

    @Nonnull
    @Override
    public MoneyValue insertMoney(@Nonnull MoneyValue insertAmount, boolean simulation) {
        for(IPlayerMoneyHandler handler : this.handlers)
        {
            if(handler.isMoneyTypeValid(insertAmount))
            {
                insertAmount = handler.insertMoney(insertAmount, simulation);
                if(insertAmount.isEmpty())
                    return MoneyValue.empty();
            }
        }
        return insertAmount;
    }

    @Nonnull
    @Override
    public MoneyValue extractMoney(@Nonnull MoneyValue extractAmount, boolean simulation) {
        for(IPlayerMoneyHandler handler : this.handlers)
        {
            extractAmount = handler.extractMoney(extractAmount, simulation);
            if(extractAmount.isEmpty())
                return MoneyValue.empty();
        }
        return extractAmount;
    }

    @Override
    public boolean isMoneyTypeValid(@Nonnull MoneyValue value) { return this.handlers.stream().anyMatch(h -> h.isMoneyTypeValid(value)); }

    @Override
    protected void collectStoredMoney(@Nonnull MoneyView.Builder builder) {
        for(IPlayerMoneyHandler handler : this.handlers)
            builder.merge(handler.getStoredMoney());
    }

    @Override
    public Component getTooltipTitle() { return LCText.TOOLTIP_MONEY_SOURCE_PLAYER.get(); }

}
