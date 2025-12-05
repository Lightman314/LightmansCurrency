package io.github.lightman314.lightmanscurrency.client.gui.screen.config.master_coin_list.data;

import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.config.MasterCoinListConfigOption;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;

public class MutableMasterCoinList {

    private final Map<String,MutableChainData> data;
    public Map<String,MutableChainData> getData() { return this.data; }

    public MutableChainData createChain(String newChain)
    {
        if(this.data.containsKey(newChain))
            return null;
        MutableChainData data = new MutableChainData(this,newChain);
        this.data.put(newChain,data);
        return data;
    }

    public final boolean canEdit() { return MasterCoinListConfigOption.INSTANCE.canEdit(Minecraft.getInstance()); }

    public MutableMasterCoinList() {
        //Forcibly load the coin data (since it doesn't need any lookup helpers or anything else)
        if(CoinAPI.getApi().NoDataAvailable())
            CoinAPI.getApi().ReloadCoinDataFromFile();
        //Copy the ChainData map from the Coin API
        this.data = new HashMap<>();
        for(ChainData chain : CoinAPI.getApi().AllChainData())
            this.data.put(chain.chain,new MutableChainData(this,chain));
    }

}