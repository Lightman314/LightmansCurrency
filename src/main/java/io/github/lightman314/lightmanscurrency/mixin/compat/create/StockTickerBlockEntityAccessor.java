package io.github.lightman314.lightmanscurrency.mixin.compat.create;

import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.foundation.item.SmartInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = StockTickerBlockEntity.class,remap = false)
public interface StockTickerBlockEntityAccessor {

    @Accessor("receivedPayments")
    SmartInventory getReceivedPayments();

}