package io.github.lightman314.lightmanscurrency.client.gui.screen.config.master_coin_list.data;

import io.github.lightman314.lightmanscurrency.api.money.coins.data.coin.CoinEntry;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.coin.SideBaseCoinEntry;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public class MutableSideChain {

    public Item splitTarget;
    public final List<MutableCoinEntry> entries = new ArrayList<>();

    public MutableSideChain(Item splitTarget) { this.splitTarget = splitTarget; }

    public MutableSideChain(List<CoinEntry> entries)
    {
        if(entries.isEmpty())
            return;
        if(entries.get(0) instanceof SideBaseCoinEntry sideBase)
        {
            this.splitTarget = sideBase.parentCoin.getCoin();
            for(CoinEntry entry : entries)
                this.entries.add(new MutableCoinEntry(entry));
        }
    }

}