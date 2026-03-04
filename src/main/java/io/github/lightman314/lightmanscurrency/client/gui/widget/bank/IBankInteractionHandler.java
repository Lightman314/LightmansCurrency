package io.github.lightman314.lightmanscurrency.client.gui.widget.bank;

import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyViewer;

public interface IBankInteractionHandler {

    IBankAccount getBankAccount();
    IMoneyViewer getCoinAccess();
}
