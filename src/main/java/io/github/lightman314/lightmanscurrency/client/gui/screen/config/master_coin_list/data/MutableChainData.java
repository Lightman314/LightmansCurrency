package io.github.lightman314.lightmanscurrency.client.gui.screen.config.master_coin_list.data;

import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.CoinInputType;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.coin.CoinEntry;
import io.github.lightman314.lightmanscurrency.api.money.coins.display.ValueDisplayData;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MutableChainData {

    public final MutableMasterCoinList parent;

    private String chain;
    public String getChain() { return this.chain; }
    public void renameChain(String newChain) {
        if(this.parent.getData().containsKey(newChain))
            return;
        this.parent.getData().remove(this.chain);
        this.chain = newChain;
        this.parent.getData().put(this.chain,this);
    }
    public Component displayName = EasyText.empty();
    public boolean isEvent = false;
    public CoinInputType inputType = CoinInputType.DEFAULT;
    public ValueDisplayData displayData = null;
    public final MutableATMData atmData = new MutableATMData();

    public final MutableCoreChain coreChain = new MutableCoreChain();
    public final List<MutableSideChain> sideChains = new ArrayList<>();

    public MutableChainData(MutableMasterCoinList parent,String chain) { this.parent = parent; this.chain = chain; }
    public MutableChainData(MutableMasterCoinList parent,ChainData original)
    {
        this(parent, original.chain);
        this.displayName = original.getDisplayName();
        this.isEvent = original.isEvent;
        this.inputType = original.getInputType();
        this.displayData = original.getDisplayData();
        this.atmData.copyFrom(original.getAtmData());
        this.coreChain.copyFrom(original.getCoreChain());
        for(List<CoinEntry> sideChain : original.getSideChains())
            this.sideChains.add(new MutableSideChain(sideChain));
    }

}