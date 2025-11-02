package io.github.lightman314.lightmanscurrency.network.message.config;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketEditConfig extends ClientToServerPacket {

    public static final Handler<CPacketEditConfig> HANDLER = new H();

    private final ResourceLocation fileID;
    private final String option;
    private final String input;

    public CPacketEditConfig(ResourceLocation fileID,String option, String input)
    {
        this.fileID = fileID;
        this.option = option;
        this.input = input;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(this.fileID);
        buffer.writeUtf(this.option);
        buffer.writeUtf(this.input);
    }

    private static class H extends Handler<CPacketEditConfig>
    {
        @Override
        public CPacketEditConfig decode(FriendlyByteBuf buffer) { return new CPacketEditConfig(buffer.readResourceLocation(),buffer.readUtf(),buffer.readUtf()); }
        @Override
        protected void handle(CPacketEditConfig message, Player player) {
            ConfigFile file = ConfigFile.lookupFile(message.fileID);
            if(file != null && !file.isClientOnly() && player.hasPermissions(2))
            {
                Map<String, ConfigOption<?>> optionMap = file.getAllOptions();
                ConfigOption<?> option = optionMap.get(message.option);
                if(option != null)
                {
                    LightmansCurrency.LogInfo(player.getName().getString() + " changed " + file.getFileID() + " -> " + message.option + " to " + message.input);
                    option.load(message.input,ConfigOption.LoadSource.COMMAND);
                }
                else
                    LightmansCurrency.LogWarning("Failed to load config option edit packet!");
            }
            if(file != null && file.isClientOnly())
                LightmansCurrency.LogWarning("Attempted to change a client-only config on the server!");
        }
    }

}
