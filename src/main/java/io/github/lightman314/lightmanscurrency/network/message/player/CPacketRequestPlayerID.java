package io.github.lightman314.lightmanscurrency.network.message.player;

import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketRequestPlayerID extends ClientToServerPacket {

    public static final Type<CPacketRequestPlayerID> TYPE = cType("request_player_id");
    public static final StreamCodec<ByteBuf, CPacketRequestPlayerID> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(CPacketRequestPlayerID::new, p -> p.playerName);

    private final String playerName;
    public CPacketRequestPlayerID(String playerName) { super(TYPE); this.playerName = playerName; }

    @Override
    protected void handle(IPayloadContext context, Player player) {
        if(player.level() instanceof ServerLevel sl) {
            MinecraftServer server = sl.getServer();
            server.services().profileResolver().fetchByName(this.playerName)
                    .ifPresent(profile -> context.reply(new SPacketPlayerCacheResult(profile.id(),profile.name())));
        }
    }

}
