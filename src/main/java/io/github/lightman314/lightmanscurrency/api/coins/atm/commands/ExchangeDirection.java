package io.github.lightman314.lightmanscurrency.api.coins.atm.commands;

import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

public enum ExchangeDirection {
    UP,
    DOWN;
    public boolean isUp() { return this == UP; }
    public boolean isDown() { return this == DOWN; }
    public static final StreamCodec<ByteBuf,ExchangeDirection> STREAM_CODEC = EnumHelper.buildStreamCodec(ExchangeDirection.class,"Exchange Direction");
}
