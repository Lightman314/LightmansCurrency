package io.github.lightman314.lightmanscurrency.network.message.player;

import io.github.lightman314.lightmanscurrency.api.client.ClientPlayerNameCache;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class SPacketPlayerCacheResult extends ServerToClientPacket {

    public static final Type<SPacketPlayerCacheResult> TYPE = sType("player_cache_response");
    public static final StreamCodec<ByteBuf,SPacketPlayerCacheResult> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,p -> p.playerID,
            ByteBufCodecs.STRING_UTF8,p -> p.playerName,
            SPacketPlayerCacheResult::new);

    public final UUID playerID;
    public final String playerName;
    public SPacketPlayerCacheResult(UUID playerID, String name) {
        super(TYPE);
        this.playerID = playerID;
        this.playerName = name;
    }

    @Override
    protected void handle(IPayloadContext context, Player player) {
        ClientPlayerNameCache.handlePacket(this);
    }

}
