package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_management;

import io.github.lightman314.lightmanscurrency.api.money.coins.data.coin.CoinEntry;
import net.minecraft.world.item.Item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EditableSideChain {

    @Nonnull
    public List<EditableCoinEntry> entries = new ArrayList<>();

    @Nullable
    public Item getParentCoin() {
        if(this.entries.isEmpty())
            return null;
        return this.entries.get(0).sideChainParent;
    }

    public EditableSideChain(@Nonnull List<CoinEntry> sideChainList)
    {
        if(sideChainList.isEmpty())
            return;
        for(CoinEntry e : sideChainList)
            this.entries.add(new EditableCoinEntry(e));
    }
    public EditableSideChain(@Nonnull Item parentCoin, @Nonnull Item rootCoin, int exchangeRate) { this.entries.add(new EditableCoinEntry(rootCoin,exchangeRate,parentCoin)); }

}