package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_management;

import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.CoinInputType;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.coin.CoinEntry;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_management.display_data.EditableDisplayData;
import net.minecraft.world.item.Item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class EditableChainData {

    String chainID;
    List<EditableCoinEntry> coreChain = new ArrayList<>();
    List<EditableSideChain> sideChains = new ArrayList<>();
    CoinInputType inputType = CoinInputType.DEFAULT;
    EditableDisplayData editableDisplayData;

    public EditableChainData(@Nonnull String chain, @Nonnull Item rootCoin)
    {
        this.chainID = chain;
        this.coreChain.add(new EditableCoinEntry(rootCoin,0,null));
    }

    public EditableChainData(@Nonnull ChainData chain)
    {
        //Copy data from the chain data
        this.chainID = chain.chain;
        //Collect core chain
        List<CoinEntry> coreEntries = chain.getCoreChain();
        for(CoinEntry e : chain.getCoreChain())
            this.coreChain.add(new EditableCoinEntry(e));
        for(List<CoinEntry> sc : chain.getSideChains())
            this.sideChains.add(new EditableSideChain(sc));
    }

    public static boolean isSaveable(@Nonnull List<EditableChainData> wipChains)
    {
        List<Item> allCoins = new ArrayList<>();
        for(EditableChainData chain : wipChains)
        {
            if(chain.isNotSaveable(allCoins))
                return false;
        }
        return true;
    }

    private boolean isNotCoreCoin(@Nullable Item coin)
    {
        if(coin == null)
            return true;
        this.coreChain.stream().anyMatch(e -> e.coin == coin);
        return false;
    }

    private boolean isNotSaveable(@Nonnull List<Item> items)
    {
        for(EditableCoinEntry e : this.coreChain)
        {
            if(compareAndAdd(items,e.coin))
                return true;
        }
        for(EditableSideChain sc : this.sideChains)
        {
            if(this.isNotCoreCoin(sc.getParentCoin()))
                return true;
            for(EditableCoinEntry e : sc.entries)
            {
                if(compareAndAdd(items,e.coin))
                    return true;
            }
        }
        return false;
    }

    private static boolean compareAndAdd(@Nonnull List<Item> items, @Nonnull Item item)
    {
        if(items.contains(item))
            return true;
        items.add(item);
        return false;
    }



}
