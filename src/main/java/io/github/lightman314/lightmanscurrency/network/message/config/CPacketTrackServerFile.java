package io.github.lightman314.lightmanscurrency.network.message.config;

import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketTrackServerFile extends ClientToServerPacket {

    private static final Type<CPacketTrackServerFile> TYPE = cType("config_track");
    private static final StreamCodec<ByteBuf,CPacketTrackServerFile> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,p -> p.fileID,
            ByteBufCodecs.BOOL,p -> p.tracking,
            CPacketTrackServerFile::new);
    public static final Handler<CPacketTrackServerFile> HANDLER = new H();

    private final ResourceLocation fileID;
    private final boolean tracking;
    public CPacketTrackServerFile(ResourceLocation fileID, boolean tracking) {
        super(TYPE);
        this.fileID = fileID;
        this.tracking = tracking;
    }

    private static class H extends Handler<CPacketTrackServerFile>
    {
        private H() { super(TYPE,STREAM_CODEC); }
        @Override
        protected void handle(CPacketTrackServerFile message, IPayloadContext context, Player player) {
            ConfigFile file = ConfigFile.lookupFile(message.fileID);
            if(file != null)
            {
                if(message.tracking)
                {
                    file.addTrackingPlayer(player);
                    file.sendSyncPacket(player);
                }
                else
                    file.removeTrackingPlayer(player);
            }
        }

    }

}
