package io.github.lightman314.lightmanscurrency.api.traders.trade;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;

public enum TradeDirection { SALE(0), PURCHASE(1), BARTER(2), OTHER(-1);

    public static final Codec<TradeDirection> CODEC = EnumUtil.buildCodec(TradeDirection.class,"Trade Direction");
    public static final StreamCodec<ByteBuf,TradeDirection> STREAM_CODEC = EnumUtil.streamCodec(TradeDirection.class,"Trade Direction");

    public final int index;
    TradeDirection(int index) { this.index = index; }
    public static TradeDirection fromIndex(int index) {
        for(TradeDirection d : TradeDirection.values())
        {
            if(d.index == index)
                return d;
        }
        return TradeDirection.SALE;
    }
    public final MutableComponent getName() { return LCText.GUI_TRADE_DIRECTION.get(this).get(); }
    public final MutableComponent getActionPhrase() { return LCText.GUI_TRADE_DIRECTION_ACTION.get(this).get(); }
}
