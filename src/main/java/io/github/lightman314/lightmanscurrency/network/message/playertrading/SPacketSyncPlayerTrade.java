package io.github.lightman314.lightmanscurrency.network.message.playertrading;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.playertrading.ClientPlayerTrade;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SPacketSyncPlayerTrade extends ServerToClientPacket {

    private static final Type<SPacketSyncPlayerTrade> TYPE = sType("player_trade_sync");
    private static final StreamCodec<RegistryFriendlyByteBuf,SPacketSyncPlayerTrade> STREAM_CODEC = ClientPlayerTrade.STREAM_CODEC
            .map(SPacketSyncPlayerTrade::new,p -> p.data);
    public static final Handler<SPacketSyncPlayerTrade> HANDLER = new H();

    private final ClientPlayerTrade data;

    public SPacketSyncPlayerTrade(ClientPlayerTrade data) { super(TYPE); this.data = data; }

    private static class H extends Handler<SPacketSyncPlayerTrade>
    {
        protected H() { super(TYPE,STREAM_CODEC); }
        @Override
        protected void handle(SPacketSyncPlayerTrade message, IPayloadContext context, Player player) {
            LightmansCurrency.getProxy().loadPlayerTrade(message.data);
        }
    }

}
