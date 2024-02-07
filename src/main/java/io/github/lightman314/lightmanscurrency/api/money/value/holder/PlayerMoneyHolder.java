package io.github.lightman314.lightmanscurrency.api.money.value.holder;

import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

public class PlayerMoneyHolder extends MoneyHolder {

    private Player cachedPlayer;

    public PlayerMoneyHolder(@Nonnull Player player) { this.cachedPlayer = player; }

    @Nonnull
    public PlayerMoneyHolder updatePlayer(@Nonnull Player player) { this.cachedPlayer = player; return this; }

    @Override
    protected void collectStoredMoney(@Nonnull MoneyView.Builder builder) {
        for(CurrencyType type : MoneyAPI.getAllCurrencyTypes())
            type.getAvailableMoney(this.cachedPlayer, builder);
    }

    @Override
    public boolean hasStoredMoneyChanged() {
        for(CurrencyType type : MoneyAPI.getAllCurrencyTypes())
        {
            if(type.hasPlayersMoneyChanged(this.cachedPlayer))
                return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public MoneyValue tryAddMoney(@Nonnull MoneyValue valueToAdd) {
        MoneyAPI.giveMoneyToPlayer(this.cachedPlayer, valueToAdd);
        return MoneyValue.empty();
    }

    @Nonnull
    @Override
    public MoneyValue tryRemoveMoney(@Nonnull MoneyValue valueToRemove) {
        MoneyView contents = this.getStoredMoney();
        MoneyValue actualTake = contents.capValue(valueToRemove);
        if(MoneyAPI.takeMoneyFromPlayer(this.cachedPlayer, actualTake))
            return valueToRemove.subtractValue(actualTake);
        return valueToRemove;
    }

    @Override
    public Component getTooltipTitle() { return EasyText.translatable("tooltip.lightmanscurrency.trader.info.money.player"); }

}
