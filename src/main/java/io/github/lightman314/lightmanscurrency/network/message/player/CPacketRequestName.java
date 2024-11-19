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

public class CPacketRequestName extends ClientToServerPacket {

    private static final Type<CPacketRequestName> TYPE = new Type<>(VersionUtil.lcResource("c_player_name_request"));
    public static final Handler<CPacketRequestName> HANDLER = new H();

    private final UUID playerID;

    public CPacketRequestName(@Nonnull UUID playerID) {
        super(TYPE);
        this.playerID = playerID;
    }

    private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull CPacketRequestName message)
    {
        buffer.writeUUID(message.playerID);
    }

    private static CPacketRequestName decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketRequestName(buffer.readUUID()); }

    private static class H extends Handler<CPacketRequestName>
    {

        protected H() { super(TYPE, StreamCodec.of(CPacketRequestName::encode,CPacketRequestName::decode)); }

        @Override
        protected void handle(@Nonnull CPacketRequestName message, @Nonnull IPayloadContext context, @Nonnull Player player) {
            String name = PlayerReference.getPlayerName(message.playerID);
            if(name != null)
                context.reply(new SPacketUpdatePlayerCache(message.playerID,name));
        }

    }

}
