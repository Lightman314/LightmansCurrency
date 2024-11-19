package io.github.lightman314.lightmanscurrency.network.message.player;

import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;
import java.util.UUID;

public class CPacketRequestID extends ClientToServerPacket {

    private static final Type<CPacketRequestID> TYPE = new Type<>(VersionUtil.lcResource("c_player_id_request"));
    public static final Handler<CPacketRequestID> HANDLER = new H();

    private final String playerName;

    public CPacketRequestID(@Nonnull String playerName) {
        super(TYPE);
        this.playerName = playerName;
    }

    private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull CPacketRequestID message)
    {
        buffer.writeUtf(message.playerName);
    }

    private static CPacketRequestID decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketRequestID(buffer.readUtf()); }

    private static class H extends Handler<CPacketRequestID>
    {

        protected H() { super(TYPE, StreamCodec.of(CPacketRequestID::encode, CPacketRequestID::decode)); }

        @Override
        protected void handle(@Nonnull CPacketRequestID message, @Nonnull IPayloadContext context, @Nonnull Player player) {
            UUID id = PlayerReference.getPlayerID(message.playerName);
            if(id != null)
            {
                //Get proper name as capitalization may be different
                String name = PlayerReference.getPlayerName(id);
                context.reply(new SPacketUpdatePlayerCache(id,name == null ? message.playerName : name));
            }
        }

    }

}
