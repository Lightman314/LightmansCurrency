package io.github.lightman314.lightmanscurrency.network.message.config;

import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketTrackServerFile extends ClientToServerPacket {

    public static final Handler<CPacketTrackServerFile> HANDLER = new H();

    private final ResourceLocation fileID;
    private final boolean tracking;
    public CPacketTrackServerFile(ResourceLocation fileID, boolean tracking)
    {
        this.fileID = fileID;
        this.tracking = tracking;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(this.fileID);
        buffer.writeBoolean(this.tracking);
    }

    private static class H extends Handler<CPacketTrackServerFile>
    {
        @Override
        public CPacketTrackServerFile decode(FriendlyByteBuf buffer) { return new CPacketTrackServerFile(buffer.readResourceLocation(),buffer.readBoolean()); }
        @Override
        protected void handle(CPacketTrackServerFile message, Player player) {
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
