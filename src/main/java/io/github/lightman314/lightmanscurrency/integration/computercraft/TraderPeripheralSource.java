package io.github.lightman314.lightmanscurrency.integration.computercraft;

import dan200.computercraft.api.peripheral.IPeripheral;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;

import javax.annotation.Nullable;
import java.util.function.Function;

public interface TraderPeripheralSource {

    @Nullable
    IPeripheral tryCreate(TraderBlockEntity<?> be);
    @Nullable
    IPeripheral tryCreate(TraderData trader);

    static TraderPeripheralSource simple(Function<TraderBlockEntity<?>,IPeripheral> blockSource,Function<TraderData,IPeripheral> dataSource) { return new Simple(blockSource,dataSource); }
    static TraderPeripheralSource blockOnly(Function<TraderBlockEntity<?>,IPeripheral> blockSource) { return new Simple(blockSource,d -> null); }
    static TraderPeripheralSource dataOnly(Function<TraderData,IPeripheral> dataSource) { return new Simple(b -> null,dataSource); }

    class Simple implements TraderPeripheralSource
    {
        private final Function<TraderBlockEntity<?>,IPeripheral> blockSource;
        private final Function<TraderData,IPeripheral> traderSource;
        private Simple(Function<TraderBlockEntity<?>,IPeripheral> blockSource,Function<TraderData,IPeripheral> traderSource) { this.blockSource = blockSource; this.traderSource = traderSource; }
        @Nullable
        @Override
        public IPeripheral tryCreate(TraderBlockEntity<?> be) { return this.blockSource.apply(be); }
        @Nullable
        @Override
        public IPeripheral tryCreate(TraderData trader) { return this.traderSource.apply(trader); }
    }

}
