package io.github.lightman314.lightmanscurrency.network.message.config;

import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketTrackServerFile extends ClientToServerPacket {

    public static final Type<CPacketTrackServerFile> TYPE = cType("config_track");
    public static final StreamCodec<ByteBuf,CPacketTrackServerFile> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,p -> p.fileID,
            ByteBufCodecs.BOOL,p -> p.tracking,
            CPacketTrackServerFile::new);

    private final Identifier fileID;
    private final boolean tracking;
    public CPacketTrackServerFile(Identifier fileID,boolean tracking) {
        super(TYPE);
        this.fileID = fileID;
        this.tracking = tracking;
    }

    @Override
    protected void handle(IPayloadContext context, Player player) {
        ConfigFile file = ConfigFile.lookupFile(this.fileID);
        if(file != null) {
            if(this.tracking) {
                file.addTrackingPlayer(player);
                file.sendSyncPacket(player);
            }
            else
                file.removeTrackingPlayer(player);
        }
    }

}
