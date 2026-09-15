package io.github.lightman314.lightmanscurrency.features.atm.tabs;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.coins.atm.commands.ATMCommand;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import io.github.lightman314.lightmanscurrency.features.atm.ATMMenu;
import io.github.lightman314.lightmanscurrency.features.atm.ATMTab;
import net.minecraft.resources.Identifier;

public class CoinExchangeTab extends ATMTab.SafeAccess {

    public static final Identifier CLIENT_KEY = LCApi.id("coin_exchange");

    public static final TextEntry TOOLTIP = TextEntry.tooltip(LCApi.MODID,"atm.exchange");

    public CoinExchangeTab(ATMMenu menu) { super(menu); }

    @Override
    public boolean usesMoneySlots() { return true; }

    @Override
    public Identifier getClientTabKey() { return CLIENT_KEY; }

    public void runATMCommand(ATMCommand command) {
        if(this.isClient()) {
            this.send(FancyPacketMap.map()
                    .set("atmCommand",LCFancyPacketTypes.ATM_COMMAND,command));
            return;
        }
        command.execute(this.getMoneyStorage(),null);
    }

    @Override
    public void handleMessage(FancyPacketMap message) {
        if(message.contains("atmCommand"))
            this.runATMCommand(message.get("atmCommand",LCFancyPacketTypes.ATM_COMMAND));
    }
}
