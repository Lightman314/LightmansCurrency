package io.github.lightman314.lightmanscurrency.api.client.gui.widget.price;

import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceType;
import net.minecraft.resources.Identifier;

public record PriceKey(Identifier type,String subType) {
    public PriceKey(Identifier type) { this(type,""); }

    public static PriceKey forType(TradePriceType<?> type) { return new PriceKey(type.getKey()); }

}
