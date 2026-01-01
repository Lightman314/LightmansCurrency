package io.github.lightman314.lightmanscurrency.integration.computercraft;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;

import javax.annotation.Nullable;
import java.util.function.Function;

public interface TraderPeripheralSource {

    @Nullable
    AccessTrackingPeripheral tryCreate(TraderBlockEntity<?> be);
    @Nullable
    AccessTrackingPeripheral tryCreate(TraderData trader);

    static TraderPeripheralSource simple(Function<TraderBlockEntity<?>,AccessTrackingPeripheral> blockSource,Function<TraderData,AccessTrackingPeripheral> dataSource) { return new Simple(blockSource,dataSource); }
    static TraderPeripheralSource blockOnly(Function<TraderBlockEntity<?>,AccessTrackingPeripheral> blockSource) { return new Simple(blockSource,d -> null); }
    static TraderPeripheralSource dataOnly(Function<TraderData,AccessTrackingPeripheral> dataSource) { return new Simple(b -> null,dataSource); }

    class Simple implements TraderPeripheralSource
    {
        private final Function<TraderBlockEntity<?>,AccessTrackingPeripheral> blockSource;
        private final Function<TraderData,AccessTrackingPeripheral> traderSource;
        private Simple(Function<TraderBlockEntity<?>,AccessTrackingPeripheral> blockSource,Function<TraderData,AccessTrackingPeripheral> traderSource) { this.blockSource = blockSource; this.traderSource = traderSource; }
        @Nullable
        @Override
        public AccessTrackingPeripheral tryCreate(TraderBlockEntity<?> be) { return this.blockSource.apply(be); }
        @Nullable
        @Override
        public AccessTrackingPeripheral tryCreate(TraderData trader) { return this.traderSource.apply(trader); }
    }

}