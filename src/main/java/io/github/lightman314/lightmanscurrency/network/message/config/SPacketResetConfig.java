package io.github.lightman314.lightmanscurrency.network.message.config;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SPacketResetConfig extends ServerToClientPacket {

    public static final Handler<SPacketResetConfig> HANDLER = new H();

    private final ResourceLocation fileID;
    private final String option;

    public SPacketResetConfig(ResourceLocation fileID, String option)
    {
        this.fileID = fileID;
        this.option = option;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(this.fileID);
        buffer.writeUtf(this.option);
    }

    private static class H extends Handler<SPacketResetConfig>
    {

        @Override
        public SPacketResetConfig decode(FriendlyByteBuf buffer) {
            return new SPacketResetConfig(buffer.readResourceLocation(), buffer.readUtf());
        }

        @Override
        protected void handle(SPacketResetConfig message, Player player) {
            ConfigFile file = ConfigFile.lookupFile(message.fileID);
            if(file != null && file.isClientOnly())
            {
                Map<String, ConfigOption<?>> optionMap = file.getAllOptions();
                if(optionMap.containsKey(message.option))
                {
                    ConfigOption<?> option = optionMap.get(message.option);
                    option.setToDefault();
                    LightmansCurrency.getProxy().sendClientMessage(LCText.COMMAND_CONFIG_EDIT_SUCCESS.get(option.getName(), option.write()));
                }
                else
                    LightmansCurrency.getProxy().sendClientMessage(LCText.COMMAND_CONFIG_FAIL_MISSING.get(message.option).withStyle(ChatFormatting.RED));
            }
        }

    }

}
