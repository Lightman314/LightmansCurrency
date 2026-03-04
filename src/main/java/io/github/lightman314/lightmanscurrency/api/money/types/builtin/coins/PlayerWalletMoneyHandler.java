package io.github.lightman314.lightmanscurrency.api.money.types.builtin.coins;

import io.github.lightman314.lightmanscurrency.api.money.capability.MoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.types.IPlayerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import net.minecraft.world.entity.player.Player;

public class PlayerWalletMoneyHandler extends MoneyHandler implements IPlayerMoneyHandler {

    private WalletHandler walletHandler;
    private final boolean requireWallet;

    public PlayerWalletMoneyHandler(Player player, boolean requireWallet) { this.updatePlayer(player); this.requireWallet = requireWallet; }

    @Override
    public MoneyValue insertMoney(MoneyValue insertAmount, boolean simulation) {
        if(this.allowInteraction())
            return this.walletHandler.insertMoney(insertAmount, simulation);
        return insertAmount;
    }

    @Override
    public MoneyValue extractMoney(MoneyValue extractAmount, boolean simulation) {
        if(this.allowInteraction())
            return this.walletHandler.extractMoney(extractAmount, simulation);
        return extractAmount;
    }

    @Override
    public boolean isMoneyTypeValid(MoneyValue value) { return value instanceof CoinValue; }

    @Override
    public void updatePlayer(Player player) {
        this.walletHandler = WalletHandler.get(player);
    }

    @Override
    protected void collectStoredMoney(MoneyView.Builder builder) {
        if(this.walletHandler != null)
            builder.merge(this.walletHandler.getStoredMoney());
    }

    private boolean allowInteraction() { return this.walletHandler != null && (!this.requireWallet || WalletItem.isWallet(this.walletHandler.getWallet())); }

}
