package io.github.lightman314.lightmanscurrency.network.message.config;

import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SPacketReloadConfig extends ServerToClientPacket {

    public static final Type<SPacketReloadConfig> TYPE = sType("config_reload");
    public static final StreamCodec<ByteBuf,SPacketReloadConfig> STREAM_CODEC = Identifier.STREAM_CODEC.map(SPacketReloadConfig::new,p -> p.fileID);

    private final Identifier fileID;
    public SPacketReloadConfig(Identifier fileID) {
        super(TYPE);
        this.fileID = fileID;
    }

    @Override
    protected void handle(IPayloadContext context, Player player) {
        ConfigFile file = ConfigFile.lookupFile(this.fileID);
        if(file != null && file.isClientOnly())
            file.reload();
    }

}
