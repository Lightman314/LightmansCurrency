package io.github.lightman314.lightmanscurrency.common.items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public record ItemStackData(ItemStack stack) {

    public static final ItemStackData EMPTY = new ItemStackData(ItemStack.EMPTY);
    public static final Codec<ItemStackData> CODEC = ItemStack.OPTIONAL_CODEC.comapFlatMap(s -> DataResult.success(new ItemStackData(s)),ItemStackData::stack);
    public static final StreamCodec<RegistryFriendlyByteBuf,ItemStackData> STREAM_CODEC = ItemStack.OPTIONAL_STREAM_CODEC.map(ItemStackData::new,ItemStackData::stack);

    @Override
    public boolean equals(Object obj) { return obj instanceof ItemStackData other && InventoryUtil.ItemsFullyMatch(this.stack,other.stack); }
    @Override
    public int hashCode() { return Objects.hash(ItemStack.hashItemAndComponents(this.stack),this.stack.getCount()); }
}
