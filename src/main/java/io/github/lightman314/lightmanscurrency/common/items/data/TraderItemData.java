package io.github.lightman314.lightmanscurrency.common.items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.lightman314.lightmanscurrency.LCText;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Consumer;

public record TraderItemData(long traderID) implements TooltipProvider {

    public static final Codec<TraderItemData> CODEC = Codec.LONG.comapFlatMap(traderID -> DataResult.success(new TraderItemData(traderID)),TraderItemData::traderID);
    public static final StreamCodec<FriendlyByteBuf,TraderItemData> STREAM_CODEC = StreamCodec.of((b,d) -> b.writeLong(d.traderID),b -> new TraderItemData(b.readLong()));

    @Override
    public void addToTooltip(@Nonnull Item.TooltipContext context, @Nonnull Consumer<Component> consumer, @Nonnull TooltipFlag flag) {
        consumer.accept(LCText.TOOLTIP_TRADER_ITEM_WITH_DATA.getWithStyle(ChatFormatting.GRAY));
        if(flag.isAdvanced())
            consumer.accept(LCText.TOOLTIP_TRADER_ITEM_WITH_DATA_TRADER_ID.get(this.traderID).withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public int hashCode() { return Objects.hash(traderID); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof TraderItemData other)
            return other.traderID == this.traderID;
        return false;
    }

}
