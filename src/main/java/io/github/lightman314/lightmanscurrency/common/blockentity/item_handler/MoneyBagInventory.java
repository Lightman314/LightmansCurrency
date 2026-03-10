package io.github.lightman314.lightmanscurrency.common.blockentity.item_handler;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.FlexibleSlotItemHandler;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MoneyBagInventory extends FlexibleSlotItemHandler {

    public static final Codec<MoneyBagInventory> CODEC = createCodec(MoneyBagInventory::new);
    public static final StreamCodec<RegistryFriendlyByteBuf,MoneyBagInventory> STREAM_CODEC = createStreamCodec(MoneyBagInventory::new);

    private final NonNullList<ItemStack> contents = NonNullList.withSize(0, ItemStack.EMPTY);

    public MoneyBagInventory() {}
    public MoneyBagInventory(List<ItemStack> stacks) { super(stacks); }

    @Override
    public MoneyBagInventory copy() { return new MoneyBagInventory(this.stacks); }

    @Override
    protected boolean isItemValid(ItemStack stack) { return CoinAPI.getApi().IsAllowedInCoinContainer(stack, false); }
}
