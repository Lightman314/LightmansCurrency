package io.github.lightman314.lightmanscurrency.network.message.config;

import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketTrackServerFile extends ClientToServerPacket {

    public static final Type<CPacketTrackServerFile> TYPE = new Type<>(VersionUtil.lcResource("c_config_track"));
    public static final Handler<CPacketTrackServerFile> HANDLER = new H();

    private final ResourceLocation fileID;
    private final boolean tracking;
    public CPacketTrackServerFile(ResourceLocation fileID, boolean tracking) {
        super(TYPE);
        this.fileID = fileID;
        this.tracking = tracking;
    }

    private static void encode(FriendlyByteBuf buffer, CPacketTrackServerFile message) { buffer.writeResourceLocation(message.fileID).writeBoolean(message.tracking); }
    private static CPacketTrackServerFile decode(FriendlyByteBuf buffer) { return new CPacketTrackServerFile(buffer.readResourceLocation(),buffer.readBoolean()); }

    private static class H extends Handler<CPacketTrackServerFile>
    {
        private H() { super(TYPE, StreamCodec.of(CPacketTrackServerFile::encode,CPacketTrackServerFile::decode)); }
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
