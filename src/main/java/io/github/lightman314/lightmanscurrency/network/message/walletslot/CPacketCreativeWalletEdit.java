package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketCreativeWalletEdit extends ClientToServerPacket {

    private static final Type<CPacketCreativeWalletEdit> TYPE = cType("wallet_creative_edit");
    private static final StreamCodec<RegistryFriendlyByteBuf,CPacketCreativeWalletEdit> STREAM_CODEC = ItemStack.OPTIONAL_STREAM_CODEC
            .map(CPacketCreativeWalletEdit::new,p -> p.newWallet);
    public static final Handler<CPacketCreativeWalletEdit> HANDLER = new H();

    private final ItemStack newWallet;
    public CPacketCreativeWalletEdit(ItemStack wallet) { super(TYPE); this.newWallet = wallet; }

    private static class H extends Handler<CPacketCreativeWalletEdit>
    {
        protected H() { super(TYPE,STREAM_CODEC); }
        @Override
        protected void handle(CPacketCreativeWalletEdit message, IPayloadContext context, Player player) {
            if(player.isCreative())
            {
                WalletHandler walletHandler = WalletHandler.get(player);
                //LightmansCurrency.LogDebug("Updated wallet stack on server from client-side interaction.");
                walletHandler.setWallet(message.newWallet);
            }
            else
                LightmansCurrency.LogWarning(player.getName().getString() + " attempted to set their wallet stack from the client, but they're not currently in creative mode!");
        }
    }

}
