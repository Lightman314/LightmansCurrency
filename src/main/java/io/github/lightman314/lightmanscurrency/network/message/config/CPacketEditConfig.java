package io.github.lightman314.lightmanscurrency.network.message.config;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketEditConfig extends ClientToServerPacket {

    private static final Type<CPacketEditConfig> TYPE = new Type<>(VersionUtil.lcResource("c_config_edit"));
    public static final Handler<CPacketEditConfig> HANDLER = new H();

    private final ResourceLocation fileID;
    private final String option;
    private final String input;

    public CPacketEditConfig(ResourceLocation fileID, String option, String input) {
        super(TYPE);
        this.fileID = fileID;
        this.option = option;
        this.input = input;
    }

    private static void encode(FriendlyByteBuf buffer, CPacketEditConfig message) {
        buffer.writeResourceLocation(message.fileID);
        buffer.writeUtf(message.option);
        buffer.writeUtf(message.input);
    }

    private static CPacketEditConfig decode(FriendlyByteBuf buffer) { return new CPacketEditConfig(buffer.readResourceLocation(),buffer.readUtf(),buffer.readUtf()); }

    private static class H extends Handler<CPacketEditConfig>
    {

        private H() { super(TYPE, StreamCodec.of(CPacketEditConfig::encode,CPacketEditConfig::decode)); }
        @Override
        protected void handle(CPacketEditConfig message, IPayloadContext context, Player player) {

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
