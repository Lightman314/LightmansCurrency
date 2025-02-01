package io.github.lightman314.lightmanscurrency.api.money.types.builtin.coins;

import io.github.lightman314.lightmanscurrency.api.capability.money.MoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.types.IPlayerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.WalletCapability;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

public class CoinPlayerMoneyHandler extends MoneyHandler implements IPlayerMoneyHandler {


    private IWalletHandler walletHandler;

    public CoinPlayerMoneyHandler(@Nonnull Player player) { this.updatePlayer(player); }

    @Nonnull
    @Override
    public MoneyValue insertMoney(@Nonnull MoneyValue insertAmount, boolean simulation) {
        if(this.walletHandler != null)
            return this.walletHandler.insertMoney(insertAmount, simulation);
        return insertAmount;
    }

    @Nonnull
    @Override
    public MoneyValue extractMoney(@Nonnull MoneyValue extractAmount, boolean simulation) {
        if(this.walletHandler != null)
            return this.walletHandler.extractMoney(extractAmount, simulation);
        return extractAmount;
    }

    @Override
    public boolean isMoneyTypeValid(@Nonnull MoneyValue value) { return value instanceof CoinValue; }

    @Override
    public void updatePlayer(@Nonnull Player player) {
        this.walletHandler = WalletCapability.lazyGetWalletHandler(player);
    }

    @Override
    protected void collectStoredMoney(@Nonnull MoneyView.Builder builder) {
        if(this.walletHandler != null)
            builder.merge(this.walletHandler.getStoredMoney());
    }

}
