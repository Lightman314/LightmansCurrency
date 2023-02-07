package io.github.lightman314.lightmanscurrency.network.message.auction;

import io.github.lightman314.lightmanscurrency.blockentity.AuctionStandBlockEntity;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SMessageSyncAuctionStandDisplay {

    private final List<ItemStack> items;

    public SMessageSyncAuctionStandDisplay(List<ItemStack> items) { this.items = InventoryUtil.copyList(items); }

    public static void encode(SMessageSyncAuctionStandDisplay message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.items.size());
        for(ItemStack item : message.items)
            buffer.writeItem(item);
    }

    public static SMessageSyncAuctionStandDisplay decode(FriendlyByteBuf buffer) {
        List<ItemStack> items = new ArrayList<>();
        int count = buffer.readInt();
        for(int i = 0; i < count; ++i)
            items.add(buffer.readItem());
        return new SMessageSyncAuctionStandDisplay(items);
    }

    public static void handle(SMessageSyncAuctionStandDisplay message, Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> AuctionStandBlockEntity.syncItemsFromServer(message.items));
        supplier.get().setPacketHandled(true);
    }


}