package io.github.lightman314.lightmanscurrency.integration.computercraft;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;

import javax.annotation.Nullable;
import java.util.function.Function;

public interface TraderPeripheralSource {

    @Nullable
    LCPeripheral tryCreate(TraderBlockEntity<?> be);
    @Nullable
    LCPeripheral tryCreate(TraderData trader);

    static TraderPeripheralSource simple(Function<TraderBlockEntity<?>,LCPeripheral> blockSource,Function<TraderData,LCPeripheral> dataSource) { return new Simple(blockSource,dataSource); }
    static TraderPeripheralSource blockOnly(Function<TraderBlockEntity<?>,LCPeripheral> blockSource) { return new Simple(blockSource,d -> null); }
    static TraderPeripheralSource dataOnly(Function<TraderData,LCPeripheral> dataSource) { return new Simple(b -> null,dataSource); }

    class Simple implements TraderPeripheralSource
    {
        private final Function<TraderBlockEntity<?>,LCPeripheral> blockSource;
        private final Function<TraderData,LCPeripheral> traderSource;
        private Simple(Function<TraderBlockEntity<?>,LCPeripheral> blockSource,Function<TraderData,LCPeripheral> traderSource) { this.blockSource = blockSource; this.traderSource = traderSource; }
        @Nullable
        @Override
        public LCPeripheral tryCreate(TraderBlockEntity<?> be) { return this.blockSource.apply(be); }
        @Nullable
        @Override
        public LCPeripheral tryCreate(TraderData trader) { return this.traderSource.apply(trader); }
    }

}
