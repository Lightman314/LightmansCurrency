package io.github.lightman314.lightmanscurrency.common.capability.wallet;

import io.github.lightman314.lightmanscurrency.api.money.types.builtin.coins.CoinContainerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.MoneyViewer;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class WalletMoneyViewer extends MoneyViewer {

    private final ItemStack walletStack;
    private SimpleContainer cachedContents;

    public WalletMoneyViewer(@Nonnull ItemStack walletStack) {
        this.walletStack = walletStack;
        this.cachedContents = WalletItem.getWalletInventory(this.walletStack);
    }

    @Override
    protected boolean hasStoredMoneyChanged() {
        Container currentContents = WalletItem.getWalletInventory(this.walletStack);
        return !InventoryUtil.ContainerMatches(this.cachedContents, currentContents);
    }

    @Override
    protected void collectStoredMoney(@Nonnull MoneyView.Builder builder) {
        this.cachedContents = WalletItem.getWalletInventory(this.walletStack);
        CoinContainerMoneyHandler.queryContainerContents(this.cachedContents, builder);
    }

}
