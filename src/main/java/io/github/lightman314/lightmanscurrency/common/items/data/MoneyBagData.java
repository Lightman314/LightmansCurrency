package io.github.lightman314.lightmanscurrency.common.items.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.common.blockentity.MoneyBagBlockEntity;
import io.github.lightman314.lightmanscurrency.util.ItemHandlerUtil;
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

import java.util.List;
import java.util.function.Consumer;

public record MoneyBagData(List<ItemStack> contents, @Range(from = 0,to = 3) int size) implements TooltipProvider {

    public static final Codec<MoneyBagData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(CodecHelper.UNLIMITED_ITEM_LIST.fieldOf("contents").forGetter(MoneyBagData::contents),
                            Codec.INT.fieldOf("size").forGetter(MoneyBagData::size))
                    .apply(builder,MoneyBagData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf,MoneyBagData> STREAM_CODEC = StreamCodec.composite(
            ItemStack.LIST_STREAM_CODEC,MoneyBagData::contents,
            ByteBufCodecs.INT,MoneyBagData::size,
            MoneyBagData::new);

    public static final MoneyBagData EMPTY = new MoneyBagData(ImmutableList.of(),0);

    public static MoneyBagData of(List<ItemStack> contents)
    {
        int size = MoneyBagBlockEntity.getBlockSize(contents);
        return new MoneyBagData(ImmutableList.copyOf(ItemHandlerUtil.copyList(contents)),size);
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> adder, TooltipFlag flag) {
        if(!this.contents.isEmpty())
        {
            if(flag.hasControlDown())
            {
                for (ItemStack coin : this.contents) {
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
