package io.github.lightman314.lightmanscurrency.client.gui.screen.config.master_coin_list.data;

import io.github.lightman314.lightmanscurrency.api.money.coins.atm.data.ATMData;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.data.ATMExchangeButtonData;

import java.util.ArrayList;
import java.util.List;

public class MutableATMData {

    public final List<MutableATMExchangeButtonData> exchangeButtons = new ArrayList<>();
    public MutableATMData() { }

    public void copyFrom(ATMData original)
    {
        this.exchangeButtons.clear();
        for(ATMExchangeButtonData data : original.getExchangeButtons())
            this.exchangeButtons.add(new MutableATMExchangeButtonData(data));
    }

}
