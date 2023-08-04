package io.github.lightman314.lightmanscurrency.common.taxes;

import io.github.lightman314.lightmanscurrency.common.traders.TraderData;

import javax.annotation.Nonnull;
import java.util.List;

public class TaxManager {

    public static List<TaxEntry> GetTaxesForTrader(@Nonnull TraderData trader) { return GetTaxes(trader.isClient(), trader); }

    public static List<TaxEntry> GetTaxes(boolean isClient, @Nonnull ITaxable taxable) { return TaxSaveData.GetAllTaxEntries(isClient).stream().filter(e -> e.ShouldTax(taxable)).toList(); }

}