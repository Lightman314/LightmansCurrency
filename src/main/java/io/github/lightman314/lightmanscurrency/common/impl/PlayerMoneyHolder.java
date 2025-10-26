package io.github.lightman314.lightmanscurrency.common.impl;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.types.IPlayerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.MoneyHolder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PlayerMoneyHolder extends MoneyHolder {

    private final List<IPlayerMoneyHandler> handlers;

    public PlayerMoneyHolder(List<IPlayerMoneyHandler> handlers) {
        this.handlers = ImmutableList.copyOf(handlers);
    }

    public PlayerMoneyHolder updatePlayer(Player player) {
        for(IPlayerMoneyHandler handler : this.handlers)
            handler.updatePlayer(player);
        return this;
    }

    @Override
    public MoneyValue insertMoney(MoneyValue insertAmount, boolean simulation) {
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

    
    @Override
    public MoneyValue extractMoney(MoneyValue extractAmount, boolean simulation) {
        for(IPlayerMoneyHandler handler : this.handlers)
        {
            extractAmount = handler.extractMoney(extractAmount, simulation);
            if(extractAmount.isEmpty())
                return MoneyValue.empty();
        }
        return extractAmount;
    }

    @Override
    public boolean isMoneyTypeValid(MoneyValue value) { return this.handlers.stream().anyMatch(h -> h.isMoneyTypeValid(value)); }

    @Override
    protected void collectStoredMoney(MoneyView.Builder builder) {
        for(IPlayerMoneyHandler handler : this.handlers)
            builder.merge(handler.getStoredMoney());
    }

    @Override
    public Component getTooltipTitle() { return LCText.TOOLTIP_MONEY_SOURCE_PLAYER.get(); }

}
