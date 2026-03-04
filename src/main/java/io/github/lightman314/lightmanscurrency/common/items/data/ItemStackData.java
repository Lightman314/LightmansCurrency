package io.github.lightman314.lightmanscurrency.common.items.data;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public final class ItemStackData {

    public static final Codec<ItemStackData> CODEC = ItemStack.OPTIONAL_CODEC.xmap(ItemStackData::of,ItemStackData::stack);
    public static final StreamCodec<RegistryFriendlyByteBuf,ItemStackData> STREAM_CODEC = ItemStack.OPTIONAL_STREAM_CODEC.map(ItemStackData::of,ItemStackData::stack);

    private final ItemStack stack;
    private ItemStackData(ItemStack stack) { this.stack = stack; }
    public ItemStack stack() { return this.stack.copy(); }

    public static final ItemStackData EMPTY = new ItemStackData(ItemStack.EMPTY);

    public static ItemStackData of(ItemStack stack) {
        if(stack.isEmpty())
            return EMPTY;
        return new ItemStackData(stack.copy());
    }

    @Override
    public boolean equals(Object obj) { return obj instanceof ItemStackData other && ItemStack.isSameItemSameComponents(this.stack,other.stack) && this.stack.getCount() == other.stack.getCount(); }
    @Override
    public int hashCode() { return Objects.hash(ItemStack.hashItemAndComponents(this.stack),this.stack.getCount()); }
}
