package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.core.neoforge.LCDataAttachments;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketSetWalletVisibility extends ClientToServerPacket {

    public static final Type<CPacketSetWalletVisibility> TYPE = cType("wallet_visibility");
    public static final StreamCodec<ByteBuf,CPacketSetWalletVisibility> STREAM_CODEC = ByteBufCodecs.BOOL.map(CPacketSetWalletVisibility::new,p -> p.visible);

    private final boolean visible;
    public CPacketSetWalletVisibility(Entity player) { this(!player.getData(LCDataAttachments.WALLET).isVisible()); }
    public CPacketSetWalletVisibility(boolean visible) { super(TYPE); this.visible = visible; }

    @Override
    protected void handle(IPayloadContext context, Player player) {
        player.getData(LCDataAttachments.WALLET).setVisible(this.visible);
    }

}
