package io.github.lightman314.lightmanscurrency.network.message.config;

import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketReloadConfig extends ServerToClientPacket {

    private static final Type<SPacketReloadConfig> TYPE = new Type<>(VersionUtil.lcResource("s_config_reload"));
    public static final Handler<SPacketReloadConfig> HANDLER = new H();

    private final ResourceLocation file;
    public SPacketReloadConfig(ResourceLocation file) {
        super(TYPE);
        this.file = file;
    }

    private static void encode(FriendlyByteBuf buffer,SPacketReloadConfig message) { buffer.writeResourceLocation(message.file); }
    private static SPacketReloadConfig decode(FriendlyByteBuf buffer) { return new SPacketReloadConfig(buffer.readResourceLocation()); }

    private static class H extends Handler<SPacketReloadConfig>
    {
        protected H() { super(TYPE, StreamCodec.of(SPacketReloadConfig::encode,SPacketReloadConfig::decode)); }
        @Override
        protected void handle(@Nonnull SPacketReloadConfig message, @Nonnull IPayloadContext context, @Nonnull Player player) {
            //ConfigFile.reloadClientFiles();
            ConfigFile file = ConfigFile.lookupFile(message.file);
            if(file != null && file.isClientOnly())
                file.reload();
        }
    }

}
