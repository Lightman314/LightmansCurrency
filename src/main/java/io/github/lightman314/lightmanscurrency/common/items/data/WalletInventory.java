package io.github.lightman314.lightmanscurrency.common.items.data;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.LCItemStackHandler;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class WalletInventory extends LCItemStackHandler {

    public static final Codec<WalletInventory> CODEC = LCItemStackHandler.createCodec(WalletInventory::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, WalletInventory> STREAM_CODEC = LCItemStackHandler.createStreamCodec(WalletInventory::new);

    public WalletInventory(int size) { super(size); }
    public WalletInventory(List<ItemStack> items) { super(items); }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) { return CoinAPI.getApi().IsAllowedInCoinContainer(stack.getItem(),true); }

    @Override
    public WalletInventory copy() { return new WalletInventory(this.copyStacks()); }
}
