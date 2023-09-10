package io.github.lightman314.lightmanscurrency.client.gui.widget.taxes;

import io.github.lightman314.lightmanscurrency.common.traders.TraderData;

import javax.annotation.Nullable;

public interface ITaxInfoInteractable {

    @Nullable
    TraderData getTrader();
    boolean canPlayerForceIgnore();
    void AcceptTaxCollector(long taxEntryID);
    void ForceIgnoreTaxCollector(long taxEntryID);
    void PardonIgnoredTaxCollector(long taxEntryID);

}