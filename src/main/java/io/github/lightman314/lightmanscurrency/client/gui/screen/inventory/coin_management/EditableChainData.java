package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_management;

import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.CoinInputType;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.coin.CoinEntry;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_management.display_data.EditableDisplayData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class EditableChainData {

    public String chainID;
    public Component chainName;
    public final List<EditableCoinEntry> coreChain = new ArrayList<>();
    public final List<EditableSideChain> sideChains = new ArrayList<>();
    public CoinInputType inputType = CoinInputType.DEFAULT;
    public EditableDisplayData editableDisplayData;

    private EditableChainData() {}
    public EditableChainData(@Nonnull String chain, @Nonnull ItemLike rootCoin, @Nonnull String chainName) { this(chain,rootCoin,EasyText.literal(chainName)); }
    public EditableChainData(@Nonnull String chain, @Nonnull ItemLike rootCoin, @Nonnull Component chainName)
    {
        this.chainID = chain;
        this.coreChain.add(new EditableCoinEntry(rootCoin.asItem(),0,null));
        this.chainName = chainName;
    }

    public EditableChainData(@Nonnull ChainData chain)
    {
        //Copy data from the chain data
        this.chainID = chain.chain;
        //Collect core chain
        for(CoinEntry e : chain.getCoreChain())
            this.coreChain.add(new EditableCoinEntry(e));
        for(List<CoinEntry> sc : chain.getSideChains())
            this.sideChains.add(new EditableSideChain(sc));
        //this.editableDisplayData;
        this.chainName = chain.getDisplayName();
    }

    public EditableChainData copy() {
        EditableChainData copy = new EditableChainData();
        copy.chainID = this.chainID;
        copy.chainName = this.chainName.copy();
        for(EditableCoinEntry e : this.coreChain)
            copy.coreChain.add(e.copy());
        for(EditableSideChain s : this.sideChains)
            copy.sideChains.add(s.copy());
        copy.inputType = this.inputType;
        if(this.editableDisplayData != null)
            copy.editableDisplayData = this.editableDisplayData.copy();
        return copy;
    }

    public static boolean isSaveable(@Nonnull Collection<EditableChainData> wipChains)
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
