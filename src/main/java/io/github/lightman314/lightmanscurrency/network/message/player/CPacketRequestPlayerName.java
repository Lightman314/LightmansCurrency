package io.github.lightman314.lightmanscurrency.network.message.player;

import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class CPacketRequestPlayerName extends ClientToServerPacket {

    public static final Type<CPacketRequestPlayerName> TYPE = cType("request_player_name");
    public static final StreamCodec<ByteBuf, CPacketRequestPlayerName> STREAM_CODEC = UUIDUtil.STREAM_CODEC.map(CPacketRequestPlayerName::new, p -> p.playerID);

    private final UUID playerID;
    public CPacketRequestPlayerName(UUID playerID) { super(TYPE); this.playerID = playerID; }

    @Override
    protected void handle(IPayloadContext context, Player player) {
        if(player.level() instanceof ServerLevel sl) {
            MinecraftServer server = sl.getServer();
            server.services().profileResolver().fetchById(this.playerID)
                    .ifPresent(profile -> context.reply(new SPacketPlayerCacheResult(profile.id(),profile.name())));
        }
    }

}
