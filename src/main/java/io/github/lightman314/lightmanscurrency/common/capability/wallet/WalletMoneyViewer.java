package io.github.lightman314.lightmanscurrency.common.capability.wallet;

import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.MoneyViewer;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class WalletMoneyViewer extends MoneyViewer {

    private final ItemStack walletStack;

    public WalletMoneyViewer(@Nonnull ItemStack walletStack) {
        this.walletStack = walletStack;
    }

    @Override
    protected void collectStoredMoney(@Nonnull MoneyView.Builder builder) {
        IMoneyHandler containerHandler = MoneyAPI.API.GetContainersMoneyHandler(WalletItem.getWalletInventory(this.walletStack),s -> {}, IClientTracker.forClient());
        builder.merge(containerHandler.getStoredMoney());
    }

}
