package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral;

import dan200.computercraft.api.peripheral.IPeripheral;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class TerminalPeripheral extends MultiTraderPeripheral {

    public TerminalPeripheral() {}

    @Override
    public String getType() { return BASE_TYPE; }
    @Override
    public Set<String> getAdditionalTypes() { return Set.of(); }

    @Nonnull
    @Override
    protected List<TraderData> getAccessibleTraders() { return TraderAPI.getApi().GetAllNetworkTraders(false); }

    @Override
    protected boolean stillAccessible(TraderData trader) { return trader.showOnTerminal(); }

    @Override
    public boolean equals(@Nullable IPeripheral peripheral) {
        if(peripheral instanceof TerminalPeripheral other)
            return super.equals(peripheral);
        return false;
    }

}