package io.github.lightman314.lightmanscurrency.network.message.playertrading;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.playertrading.ClientPlayerTrade;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SPacketSyncPlayerTrade extends ServerToClientPacket {

    public static final Handler<SPacketSyncPlayerTrade> HANDLER = new H();

    private final ClientPlayerTrade data;

    public SPacketSyncPlayerTrade(ClientPlayerTrade data) { this.data = data; }

    public void encode(FriendlyByteBuf buffer) { this.data.encode(buffer); }

    private static class H extends Handler<SPacketSyncPlayerTrade>
    {
        @Override
        public SPacketSyncPlayerTrade decode(FriendlyByteBuf buffer) { return new SPacketSyncPlayerTrade(ClientPlayerTrade.decode(buffer)); }
        @Override
        protected void handle(SPacketSyncPlayerTrade message, Player player) {
            LightmansCurrency.getProxy().loadPlayerTrade(message.data);
        }
    }

}
