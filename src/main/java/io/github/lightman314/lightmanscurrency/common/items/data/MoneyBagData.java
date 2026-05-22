package io.github.lightman314.lightmanscurrency.common.items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.common.blockentity.MoneyBagBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.item_handler.MoneyBagInventory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jetbrains.annotations.Range;

import java.util.function.Consumer;

public record MoneyBagData(ImmutableInventory items, @Range(from = 0,to = 3) int size) implements TooltipProvider {

    public static final Codec<MoneyBagData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(ImmutableInventory.CODEC.fieldOf("items").forGetter(MoneyBagData::items),
                            Codec.INT.fieldOf("size").forGetter(MoneyBagData::size))
                    .apply(builder,MoneyBagData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf,MoneyBagData> STREAM_CODEC = StreamCodec.composite(
            ImmutableInventory.STREAM_CODEC,MoneyBagData::items,
            ByteBufCodecs.INT,MoneyBagData::size,
            MoneyBagData::new);

    public static final MoneyBagData EMPTY = new MoneyBagData(ImmutableInventory.EMPTY,0);

    public MoneyBagInventory contents() { return this.items.makeMutable(MoneyBagInventory::new); }

    public static MoneyBagData of(MoneyBagInventory contents)
    {
        int size = MoneyBagBlockEntity.getBlockSize(contents);
        return new MoneyBagData(contents.immutable(),size);
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> adder, TooltipFlag flag) {
        if(!this.items.isEmpty())
        {
            if(flag.hasControlDown())
            {
                for (ItemStack coin : this.items.getStacks()) {
                    if (coin.getCount() > 1)
                        adder.accept(LCText.TOOLTIP_COIN_JAR_CONTENTS_MULTIPLE.get(coin.getCount(), coin.getHoverName()));
                    else
                        adder.accept(LCText.TOOLTIP_COIN_JAR_CONTENTS_SINGLE.get(coin.getHoverName()));
                }
            }
            else
                adder.accept(LCText.TOOLTIP_COIN_JAR_HOLD_CTRL.get().withStyle(ChatFormatting.YELLOW));
        }
        if(flag.isAdvanced())
            adder.accept(LCText.TOOLTIP_MONEY_BAG_SIZE.get(this.size).withStyle(ChatFormatting.DARK_GRAY));
    }

}
