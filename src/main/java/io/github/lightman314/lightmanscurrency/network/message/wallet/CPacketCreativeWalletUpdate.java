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

    public static final Type<CPacketCreativeWalletUpdate> TYPE = cType("creative_wallet_slot");
    public static final StreamCodec<RegistryFriendlyByteBuf,CPacketCreativeWalletUpdate> STREAM_CODEC = StreamHelper.C2S_ITEM_STACK.map(CPacketCreativeWalletUpdate::new, p -> p.item);

    private final ItemStack item;
    public CPacketCreativeWalletUpdate(ItemStack item) {
        super(TYPE);
        this.item = item.copy();
    }

    @Override
    protected void handle(IPayloadContext context, Player player) {
        //Only actually process if the player is in creative on the server as well :)
        if(player.isCreative())
            player.getData(LCDataAttachments.WALLET).setWallet(this.item);
    }

}
