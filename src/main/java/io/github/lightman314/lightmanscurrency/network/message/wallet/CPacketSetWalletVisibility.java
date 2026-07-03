package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.core.neoforge.LCDataAttachments;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketSetWalletVisibility extends ClientToServerPacket {

    private static final Type<CPacketSetWalletVisibility> TYPE = cType("wallet_visibility");
    public static final Handler<CPacketSetWalletVisibility> HANDLER = new H();

    private final boolean visible;
    public CPacketSetWalletVisibility(Entity player) { this(!player.getData(LCDataAttachments.WALLET).isVisible()); }
    public CPacketSetWalletVisibility(boolean visible) { super(TYPE); this.visible = visible; }

    private static class H extends Handler<CPacketSetWalletVisibility> {
        private H() { super(TYPE, ByteBufCodecs.BOOL.map(CPacketSetWalletVisibility::new,p -> p.visible)); }

        @Override
        protected void handle(CPacketSetWalletVisibility message, IPayloadContext context, Player player) {
            player.getData(LCDataAttachments.WALLET).setVisible(message.visible);
        }
    }


}
