package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_management;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.coin.CoinEntry;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.coin.SideBaseCoinEntry;
import net.minecraft.world.item.Item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EditableCoinEntry {

    @Nonnull
    public Item coin;
    public int exchangeRate;
    @Nullable
    public Item sideChainParent;

    public EditableCoinEntry(@Nonnull CoinEntry entry) {
        this(entry.getCoin(), entry.getExchangeRate(), null);
        if(entry instanceof SideBaseCoinEntry side)
            this.sideChainParent = side.parentCoin.getCoin();
    }
    public EditableCoinEntry(@Nonnull Item coin, int exchangeRate, @Nullable Item sideChainParent) {
        this.coin = coin;
        this.exchangeRate = exchangeRate;
        this.sideChainParent = sideChainParent;
    }

    public void swapCoinsWith(@Nonnull EditableCoinEntry otherEntry)
    {
        Item temp = this.coin;
        this.coin = otherEntry.coin;
        otherEntry.coin = temp;
    }

    public void changeSideChainParent(@Nonnull Item newParent)
    {
        if(this.sideChainParent == null)
            LightmansCurrency.LogWarning("This coin is not a side chain root entry. Should not be changing it's core chain parent");
        else
            this.sideChainParent = newParent;
    }

}
