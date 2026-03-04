package io.github.lightman314.lightmanscurrency.api.misc.item_handlers;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class NonEmptyHandler extends LCItemStackHandler {

    public static final Codec<NonEmptyHandler> CODEC = createCodec(NonEmptyHandler::new);
    public static final StreamCodec<RegistryFriendlyByteBuf,NonEmptyHandler> STREAM_CODEC = createStreamCodec(NonEmptyHandler::new);

    public NonEmptyHandler(List<ItemStack> items) { super(filterList(items)); }

    private static List<ItemStack> filterList(List<ItemStack> list)
    {
        list.removeIf(ItemStack::isEmpty);
        return list;
    }

    @Override
    public NonEmptyHandler copy() { return new NonEmptyHandler(this.stacks); }

    @Override
    protected void onContentsChanged(int slot) {
        //Delete the slot if the item in that slot is now empty
        ItemStack stack = this.stacks.get(slot);
        if(stack.isEmpty())
            this.stacks.remove(slot);
        super.onContentsChanged(slot);
    }

}
