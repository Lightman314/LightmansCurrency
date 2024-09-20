package io.github.lightman314.lightmanscurrency.common.capability.wallet;

import io.github.lightman314.lightmanscurrency.api.money.types.builtin.coins.CoinContainerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.MoneyViewer;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class WalletMoneyViewer extends MoneyViewer {

    private final ItemStack walletStack;

    public WalletMoneyViewer(@Nonnull ItemStack walletStack) {
        this.walletStack = walletStack;
    }

    @Override
    protected void collectStoredMoney(@Nonnull MoneyView.Builder builder) {
        CoinContainerMoneyHandler.queryContainerContents(WalletItem.getWalletInventory(this.walletStack), builder);
    }

}
