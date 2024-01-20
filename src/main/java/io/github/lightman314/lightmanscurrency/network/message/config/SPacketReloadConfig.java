package io.github.lightman314.lightmanscurrency.network.message.config;

import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SPacketReloadConfig extends ServerToClientPacket.Simple {

    public static final SPacketReloadConfig INSTANCE = new SPacketReloadConfig();
    public static final Handler<SPacketReloadConfig> HANDLER = new H();

    private SPacketReloadConfig() {}

    private static class H extends SimpleHandler<SPacketReloadConfig>
    {
        protected H() { super(INSTANCE); }
        @Override
        protected void handle(@Nonnull SPacketReloadConfig message, @Nullable ServerPlayer sender) { ConfigFile.reloadFiles(); }
    }

}
