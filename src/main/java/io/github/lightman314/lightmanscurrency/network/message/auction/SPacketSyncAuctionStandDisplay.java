package io.github.lightman314.lightmanscurrency.network.message.auction;

import io.github.lightman314.lightmanscurrency.common.blockentity.AuctionStandBlockEntity;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class SPacketSyncAuctionStandDisplay extends ServerToClientPacket {

    private static final Type<SPacketSyncAuctionStandDisplay> TYPE = new Type<>(VersionUtil.lcResource("s_auction_stand_sync"));
    public static final Handler<SPacketSyncAuctionStandDisplay> HANDLER = new H();

    private final List<ItemStack> items;

    public SPacketSyncAuctionStandDisplay(List<ItemStack> items) { super(TYPE); this.items = InventoryUtil.copyList(items); }

    private static void encode(@Nonnull RegistryFriendlyByteBuf buffer, @Nonnull SPacketSyncAuctionStandDisplay message)
    {
        buffer.writeInt(message.items.size());
        for(ItemStack item : message.items)
            writeItem(buffer,item);
    }
    private static SPacketSyncAuctionStandDisplay decode(@Nonnull RegistryFriendlyByteBuf buffer) {
        List<ItemStack> items = new ArrayList<>();
        int count = buffer.readInt();
        for(int i = 0; i < count; ++i)
            items.add(readItem(buffer));
        return new SPacketSyncAuctionStandDisplay(items);
    }

    private static class H extends Handler<SPacketSyncAuctionStandDisplay>
    {
        protected H() { super(TYPE, fancyCodec(SPacketSyncAuctionStandDisplay::encode,SPacketSyncAuctionStandDisplay::decode)); }
        @Override
        protected void handle(@Nonnull SPacketSyncAuctionStandDisplay message, @Nonnull IPayloadContext context, @Nonnull Player player) {
            AuctionStandBlockEntity.syncItemsFromServer(message.items);
        }
    }

}
