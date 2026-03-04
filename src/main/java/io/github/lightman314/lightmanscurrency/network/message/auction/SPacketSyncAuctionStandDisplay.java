package io.github.lightman314.lightmanscurrency.network.message.auction;

import io.github.lightman314.lightmanscurrency.common.blockentity.AuctionStandBlockEntity;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.util.ItemHandlerUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public class SPacketSyncAuctionStandDisplay extends ServerToClientPacket {

    private static final Type<SPacketSyncAuctionStandDisplay> TYPE = sType("auction_stand_sync");
    private static final StreamCodec<RegistryFriendlyByteBuf,SPacketSyncAuctionStandDisplay> STREAM_CODEC = ItemStack.LIST_STREAM_CODEC.map(SPacketSyncAuctionStandDisplay::new,p -> p.items);
    public static final Handler<SPacketSyncAuctionStandDisplay> HANDLER = new H();

    private final List<ItemStack> items;

    public SPacketSyncAuctionStandDisplay(List<ItemStack> items) { super(TYPE); this.items = ItemHandlerUtil.copyList(items); }

    private static class H extends Handler<SPacketSyncAuctionStandDisplay>
    {
        protected H() { super(TYPE,STREAM_CODEC); }
        @Override
        protected void handle(SPacketSyncAuctionStandDisplay message, IPayloadContext context, Player player) {
            AuctionStandBlockEntity.syncItemsFromServer(message.items);
        }
    }

}
