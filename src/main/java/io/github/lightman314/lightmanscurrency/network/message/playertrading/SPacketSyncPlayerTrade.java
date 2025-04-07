package io.github.lightman314.lightmanscurrency.network.message.playertrading;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.playertrading.ClientPlayerTrade;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketSyncPlayerTrade extends ServerToClientPacket {

    private static final Type<SPacketSyncPlayerTrade> TYPE = new Type<>(VersionUtil.lcResource("s_player_trade_sync"));
    public static final Handler<SPacketSyncPlayerTrade> HANDLER = new H();

    private final ClientPlayerTrade data;

    public SPacketSyncPlayerTrade(ClientPlayerTrade data) { super(TYPE); this.data = data; }

    private static void encode(@Nonnull RegistryFriendlyByteBuf buffer, @Nonnull SPacketSyncPlayerTrade message) { message.data.encode(buffer); }
    private static SPacketSyncPlayerTrade decode(@Nonnull RegistryFriendlyByteBuf buffer) { return new SPacketSyncPlayerTrade(ClientPlayerTrade.decode(buffer)); }

    private static class H extends Handler<SPacketSyncPlayerTrade>
    {
        protected H() { super(TYPE, fancyCodec(SPacketSyncPlayerTrade::encode,SPacketSyncPlayerTrade::decode)); }
        @Override
        protected void handle(@Nonnull SPacketSyncPlayerTrade message, @Nonnull IPayloadContext context, @Nonnull Player player) {
            LightmansCurrency.getProxy().loadPlayerTrade(message.data);
        }
    }

}
