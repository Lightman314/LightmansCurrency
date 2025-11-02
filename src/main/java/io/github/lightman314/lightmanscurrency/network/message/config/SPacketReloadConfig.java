package io.github.lightman314.lightmanscurrency.network.message.config;

import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SPacketReloadConfig extends ServerToClientPacket {

    public static final Handler<SPacketReloadConfig> HANDLER = new H();

    private final ResourceLocation file;
    public SPacketReloadConfig(ResourceLocation file) { this.file = file; }

    @Override
    public void encode(FriendlyByteBuf buffer) { buffer.writeResourceLocation(this.file); }

    private static class H extends Handler<SPacketReloadConfig>
    {
        @Override
        public SPacketReloadConfig decode(FriendlyByteBuf buffer) { return new SPacketReloadConfig(buffer.readResourceLocation()); }

        @Override
        protected void handle(SPacketReloadConfig message, Player player) {
            //ConfigFile.reloadClientFiles();
            ConfigFile file = ConfigFile.lookupFile(message.file);
            if(file != null && file.isClientOnly())
                file.reload();
        }
    }

}
