package io.github.lightman314.lightmanscurrency.api.trader.item;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.function.Consumer;

public record StoredTrader(long traderID) implements TooltipProvider {

    public StoredTrader(TraderData trader) { this(trader.getID()); }

    public static final Codec<StoredTrader> CODEC = Codec.LONG.xmap(StoredTrader::new,StoredTrader::traderID);
    public static final StreamCodec<ByteBuf,StoredTrader> STREAM_CODEC = ByteBufCodecs.LONG.map(StoredTrader::new,StoredTrader::traderID);

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components) {
        //TODO translate
        if(flag.isAdvanced())
            consumer.accept(Component.literal("Trader ID:" + this.traderID));
    }

}