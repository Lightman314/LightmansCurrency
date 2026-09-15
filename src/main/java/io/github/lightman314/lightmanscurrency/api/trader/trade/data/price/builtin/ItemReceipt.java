package io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceReceipt;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceReceiptType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ItemReceipt extends TradePriceReceipt {

    private static final MapCodec<ItemReceipt> MAP_CODEC = CodecHelper.UNLIMITED_ITEM_LIST.fieldOf("items").xmap(ItemReceipt::new,r -> r.items);
    private static final StreamCodec<RegistryFriendlyByteBuf,ItemReceipt> STREAM_CODEC = ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).map(ItemReceipt::new,r -> r.items);

    public static final TradePriceReceiptType<ItemReceipt> TYPE = new TradePriceReceiptType<>(MAP_CODEC,STREAM_CODEC);

    private final List<ItemStack> items;
    public List<ItemStack> getItems() { return ItemHelper.copyList(this.items); }

    public ItemReceipt(List<ItemStack> items) { this.items = List.copyOf(ItemHelper.combineStacks(items)); }

    @Override
    public TradePriceReceiptType<?> getType() { return TYPE; }
    @Override
    public Component getText() { return ItemHelper.formatItemNames(this.items); }

    @Override
    protected boolean equals(TradePriceReceipt other) { return other instanceof ItemReceipt r && ItemHelper.listsMatch(this.items,r.items); }

    @Override
    protected int hash() { return ItemHelper.hashList(this.items); }

}
