package io.github.lightman314.lightmanscurrency.network.message.config;

import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SPacketReloadConfig extends ServerToClientPacket {

    private static final Type<SPacketReloadConfig> TYPE = sType("config_reload");
    private static final StreamCodec<ByteBuf,SPacketReloadConfig> STREAM_CODEC = ResourceLocation.STREAM_CODEC
            .map(SPacketReloadConfig::new,p -> p.file);
    public static final Handler<SPacketReloadConfig> HANDLER = new H();

    private final ResourceLocation file;
    public SPacketReloadConfig(ResourceLocation file) {
        super(TYPE);
        this.file = file;
    }
    
    private static class H extends Handler<SPacketReloadConfig>
    {
        protected H() { super(TYPE,STREAM_CODEC); }
        @Override
        protected void handle(SPacketReloadConfig message, IPayloadContext context, Player player) {
            //ConfigFile.reloadClientFiles();
            ConfigFile file = ConfigFile.lookupFile(message.file);
            if(file != null && file.isClientOnly())
                file.reload();
        }
    }

}
