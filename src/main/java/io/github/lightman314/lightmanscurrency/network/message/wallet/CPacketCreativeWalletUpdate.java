package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.core.neoforge.LCDataAttachments;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketCreativeWalletUpdate extends ClientToServerPacket {

    private static final Type<CPacketCreativeWalletUpdate> TYPE = cType("creative_wallet_slot");
    private static final StreamCodec<RegistryFriendlyByteBuf,CPacketCreativeWalletUpdate> STREAM_CODEC = StreamHelper.C2S_ITEM_STACK.map(CPacketCreativeWalletUpdate::new, p -> p.item);
    public static final Handler<CPacketCreativeWalletUpdate> HANDLER = new H();

    private final ItemStack item;
    public CPacketCreativeWalletUpdate(ItemStack item) {
        super(TYPE);
        this.item = item.copy();
    }

    private static class H extends Handler<CPacketCreativeWalletUpdate> {

        protected H() { super(TYPE,STREAM_CODEC); }
        @Override
        protected void handle(CPacketCreativeWalletUpdate message, IPayloadContext context, Player player) {
            //Only actually process if the player is in creative on the server as well :)
            if(player.isCreative())
                player.getData(LCDataAttachments.WALLET).setWallet(message.item);
        }
    }

}
