package io.github.lightman314.lightmanscurrency.common.items.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.util.ItemHandlerUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ImmutableInventory {

    public static final Codec<ImmutableInventory> CODEC = ItemStack.OPTIONAL_CODEC.listOf().xmap(ImmutableInventory::new,i -> i.items);
    public static final StreamCodec<RegistryFriendlyByteBuf,ImmutableInventory> STREAM_CODEC = ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list())
            .map(ImmutableInventory::new,i -> i.items);

    private final ImmutableList<ItemStack> items;

    public static final ImmutableInventory EMPTY = new ImmutableInventory();

    private ImmutableInventory() { this.items = ImmutableList.of(); }
    private ImmutableInventory(List<ItemStack> items) { this.items = ImmutableList.copyOf(ItemHandlerUtil.copyList(items)); }

    public static ImmutableInventory ofSize(int size) {
        if(size <= 0)
            return EMPTY;
        return new ImmutableInventory(NonNullList.withSize(size,ItemStack.EMPTY));
    }
    public static ImmutableInventory ofList(List<ItemStack> items) { return new ImmutableInventory(items); }
    public static ImmutableInventory ofInventory(IItemHandler inventory) { return new ImmutableInventory(ItemHandlerUtil.toList(inventory)); }

    public static ImmutableInventory ofCombinedList(List<ItemStack> items) {
        List<ItemStack> result = new ArrayList<>(ItemHandlerUtil.combineStacks(items));
        result.removeIf(ItemStack::isEmpty);
        return new ImmutableInventory(result);
    }

    public boolean isEmpty() { return this.items.isEmpty() || this.items.stream().allMatch(ItemStack::isEmpty); }
    public ImmutableList<ItemStack> getStacks() { return ImmutableList.copyOf(ItemHandlerUtil.copyList(this.items)); }

    public <T> T makeMutable(Function<List<ItemStack>,T> builder) { return builder.apply(ItemHandlerUtil.copyList(this.items)); }

}
