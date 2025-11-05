package io.github.lightman314.lightmanscurrency.client.gui.screen.config.master_coin_list.data;

import io.github.lightman314.lightmanscurrency.api.money.coins.atm.data.ATMExchangeButtonData;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.ATMIconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;

import java.util.ArrayList;
import java.util.List;

public class MutableATMExchangeButtonData {

    public ScreenPosition position;
    public int width = 50;
    public int height = 18;
    public String command = "exchangeUp-undefined";
    public final List<ATMIconData> iconData = new ArrayList<>();

    public MutableATMExchangeButtonData() { }
    public MutableATMExchangeButtonData(ATMExchangeButtonData original)
    {
        this.position = original.position;
        this.width = original.width;
        this.height = original.height;
        this.command = original.command;
        this.iconData.addAll(original.getIcons());
    }

}
