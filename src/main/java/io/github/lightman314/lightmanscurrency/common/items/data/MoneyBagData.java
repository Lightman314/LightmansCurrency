package io.github.lightman314.lightmanscurrency.common.items.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.codecs.LCCodecs;
import io.github.lightman314.lightmanscurrency.common.blockentity.MoneyBagBlockEntity;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jetbrains.annotations.Range;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public record MoneyBagData(List<ItemStack> contents, @Range(from = 0,to = 3) int size) implements TooltipProvider {

    public static final MoneyBagData EMPTY = new MoneyBagData(ImmutableList.of(),0);

    public static MoneyBagData of(List<ItemStack> contents)
    {
        int size = MoneyBagBlockEntity.getBlockSize(contents);
        return new MoneyBagData(ImmutableList.copyOf(InventoryUtil.copyList(contents)),size);
    }

    public static final Codec<MoneyBagData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(LCCodecs.UNLIMITED_ITEM_OPTIONAL.listOf().fieldOf("contents").forGetter(MoneyBagData::contents),
                    Codec.INT.fieldOf("size").forGetter(MoneyBagData::size))
                    .apply(builder,MoneyBagData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf,MoneyBagData> STREAM_CODEC = StreamCodec.of(
            (b,d) -> {
                b.writeInt(d.contents.size());
                for(ItemStack i : d.contents)
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(b,i);
                b.writeInt(d.size);
            },
            (b) -> {
                int itemCount = b.readInt();
                List<ItemStack> list = new ArrayList<>();
                for(int i = 0; i < itemCount; ++i)
                    list.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(b));
                return new MoneyBagData(ImmutableList.copyOf(list),b.readInt());
            });

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {

    }
}
