package io.github.lightman314.lightmanscurrency.api.trader.data_components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.TooltipHelper;
import io.github.lightman314.lightmanscurrency.api.text.MultiLineTextEntry;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IDisplayNode;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public record StoredTrader(long traderID,boolean stillAccessible) implements TooltipProvider {
    public StoredTrader(long traderID) { this(traderID,false); }

    public StoredTrader(TraderData trader,boolean adminAccessible) { this(trader.getID(),false); }

    public static final TextEntry WITH_DATA = TextEntry.tooltip(LCApi.MODID,"trader.item.contains_data");
    public static final TextEntry WITH_DATA_ID = TextEntry.tooltip(LCApi.MODID,"trader.item.contains_data.trader_id");
    public static final MultiLineTextEntry STILL_ACCESSIBLE = MultiLineTextEntry.tooltip(LCApi.MODID,"trader.item.contains_data.accessible");

    public static final Codec<StoredTrader> CODEC = Codec.withAlternative(RecordCodecBuilder.create(builder -> builder.group(
            Codec.LONG.fieldOf("id").forGetter(StoredTrader::traderID),
            Codec.BOOL.optionalFieldOf("accessible",false).forGetter(StoredTrader::stillAccessible)
    ).apply(builder,StoredTrader::new)),Codec.LONG.xmap(StoredTrader::new,StoredTrader::traderID));
    public static final StreamCodec<ByteBuf,StoredTrader> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.LONG,StoredTrader::traderID,
            ByteBufCodecs.BOOL,StoredTrader::stillAccessible,
            StoredTrader::new);

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> builder, TooltipFlag flag, DataComponentGetter components) {
        builder.accept(WITH_DATA.getWithStyle(ChatFormatting.GRAY));
        Level level = context.level();
        if(level != null) {
            TraderData trader = LCApi.getTraderAPI().getTrader(level.isClientSide(),this.traderID);
            if(trader != null)
                builder.accept(IDisplayNode.getTraderName(trader).copy().withStyle(ChatFormatting.GRAY));
        }
        TooltipHelper.splitTooltips(STILL_ACCESSIBLE.get(),ChatFormatting.GRAY).forEach(builder);
        if(flag.isAdvanced())
            builder.accept(WITH_DATA_ID.get(this.traderID).withStyle(ChatFormatting.DARK_GRAY));
    }
    
}