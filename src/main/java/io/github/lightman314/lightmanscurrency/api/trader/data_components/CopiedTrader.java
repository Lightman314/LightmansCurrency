package io.github.lightman314.lightmanscurrency.api.trader.data_components;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class CopiedTrader implements TooltipProvider {

    public static final Codec<CopiedTrader> CODEC = CompoundTag.CODEC.xmap(CopiedTrader::new,ct -> ct.data);
    public static final StreamCodec<ByteBuf,CopiedTrader> STREAM_CODEC;

    public static final TextEntry WITH_COPY = TextEntry.tooltip(LCApi.MODID,"trader.item.contains_copy");

    static {
        StreamCodec<ByteBuf,CompoundTag> temp = StreamCodec.of(FriendlyByteBuf::writeNbt,b -> (CompoundTag)FriendlyByteBuf.readNbt(b,NbtAccounter.unlimitedHeap()));
        STREAM_CODEC = temp.map(CopiedTrader::new,ct -> ct.data);
    }

    private final CompoundTag data;
    @Nullable
    public final TraderData tryCreate(HolderLookup.Provider registries) {
        try {
            return TraderData.CODEC.decode(registries.createSerializationContext(NbtOps.INSTANCE),this.data).getOrThrow(IllegalStateException::new).getFirst();
        } catch (IllegalStateException e) {
            LightmansCurrency.LogError("Error decoding a copied trader!",e);
            return null;
        }
    }

    public CopiedTrader(CompoundTag rawData) { this.data = rawData; }
    public CopiedTrader(TraderData trader,HolderLookup.Provider registries) throws IllegalStateException {
        this.data = (CompoundTag)TraderData.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE),trader).getOrThrow(IllegalStateException::new);
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> builder, TooltipFlag flag, DataComponentGetter components) {
        builder.accept(WITH_COPY.getWithStyle(ChatFormatting.GRAY));
    }

    @Override
    public int hashCode() { return this.data.hashCode(); }
    @Override
    public boolean equals(Object obj) { return obj instanceof CopiedTrader c && this.data.equals(c.data); }
    @Override
    public String toString() { return this.data.toString(); }

}
