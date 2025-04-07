package io.github.lightman314.lightmanscurrency.network.message.config;

import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketReloadConfig extends ServerToClientPacket {

    private static final Type<SPacketReloadConfig> TYPE = new Type<>(VersionUtil.lcResource("s_config_reload"));
    public static final SPacketReloadConfig INSTANCE = new SPacketReloadConfig();
    public static final Handler<SPacketReloadConfig> HANDLER = new H();

    private SPacketReloadConfig() { super(TYPE); }

    private static class H extends SimpleHandler<SPacketReloadConfig>
    {
        protected H() { super(TYPE, INSTANCE); }
        @Override
        protected void handle(@Nonnull SPacketReloadConfig message, @Nonnull IPayloadContext context, @Nonnull Player player) {
            ConfigFile.reloadClientFiles();
        }
    }

}
