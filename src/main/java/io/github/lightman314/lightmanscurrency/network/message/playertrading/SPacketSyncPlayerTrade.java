package io.github.lightman314.lightmanscurrency.network.message.playertrading;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.playertrading.ClientPlayerTrade;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SPacketSyncPlayerTrade extends ServerToClientPacket {

    public static final Handler<SPacketSyncPlayerTrade> HANDLER = new H();

    private final ClientPlayerTrade data;

    public SPacketSyncPlayerTrade(ClientPlayerTrade data) { this.data = data; }

    public void encode(@Nonnull FriendlyByteBuf buffer) { this.data.encode(buffer); }

    private static class H extends Handler<SPacketSyncPlayerTrade>
    {
        @Nonnull
        @Override
        public SPacketSyncPlayerTrade decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketSyncPlayerTrade(ClientPlayerTrade.decode(buffer)); }
        @Override
        protected void handle(@Nonnull SPacketSyncPlayerTrade message, @Nullable ServerPlayer sender) {
            LightmansCurrency.PROXY.loadPlayerTrade(message.data);
        }
    }

}
