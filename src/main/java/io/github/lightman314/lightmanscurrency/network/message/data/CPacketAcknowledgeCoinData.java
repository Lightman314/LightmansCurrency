package io.github.lightman314.lightmanscurrency.network.message.data;

import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketAcknowledgeCoinData extends ClientToServerPacket {

    private static final Type<CPacketAcknowledgeCoinData> TYPE = cType("acknowledge_master_coin_list");
    private static final CPacketAcknowledgeCoinData INSTANCE = new CPacketAcknowledgeCoinData();
    public static final Handler<CPacketAcknowledgeCoinData> HANDLER = new H();

    public static void send() { INSTANCE.sendToServer(); }

    protected CPacketAcknowledgeCoinData() { super(TYPE); }

    private static class H extends SimpleHandler<CPacketAcknowledgeCoinData>
    {
        protected H() { super(TYPE,INSTANCE); }
        @Override
        protected void handle(CPacketAcknowledgeCoinData message, IPayloadContext context, Player player) {
            context.finishCurrentTask(SPacketSyncCoinData.CONFIG_TYPE);
        }
    }

}
