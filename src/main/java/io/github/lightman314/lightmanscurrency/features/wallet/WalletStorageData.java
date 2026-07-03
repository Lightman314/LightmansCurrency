package io.github.lightman314.lightmanscurrency.features.wallet;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class WalletStorageData {

    public static final Codec<WalletStorageData> CODEC = ItemStack.OPTIONAL_CODEC.listOf(1,WalletItem.MAX_WALLET_SLOTS).xmap(WalletStorageData::new,WalletStorageData::storage);
    public static final StreamCodec<RegistryFriendlyByteBuf,WalletStorageData> STREAM_CODEC = ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list(WalletItem.MAX_WALLET_SLOTS)).map(WalletStorageData::new,WalletStorageData::storage);

    public static final WalletStorageData EMPTY = new WalletStorageData(ImmutableList.of());

    private final ImmutableList<ItemStack> storage;
    public List<ItemStack> storage() { return ItemHelper.copyList(this.storage); }
    public WalletStorageData(WalletStorage storage) { this(ItemHelper.copyList(storage.getContents())); }
    private WalletStorageData(List<ItemStack> items) {
        this.storage = ImmutableList.copyOf(items);
    }

    @Override
    public int hashCode() { return ItemHelper.hashList(this.storage); }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        if(obj instanceof WalletStorageData other)
            return ItemHelper.listsMatch(this.storage,other.storage);
        return false;
    }

}