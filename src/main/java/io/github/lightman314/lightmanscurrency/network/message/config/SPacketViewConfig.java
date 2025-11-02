package io.github.lightman314.lightmanscurrency.network.message.config;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
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
public class SPacketViewConfig extends ServerToClientPacket {

    public static final Handler<SPacketViewConfig> HANDLER = new H();

    private final ResourceLocation fileID;
    private final String option;

    public SPacketViewConfig(ResourceLocation fileID, String option)
    {
        this.fileID = fileID;
        this.option = option;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(this.fileID);
        buffer.writeUtf(this.option);
    }

    private static class H extends Handler<SPacketViewConfig>
    {

        @Override
        public SPacketViewConfig decode(FriendlyByteBuf buffer) {
            return new SPacketViewConfig(buffer.readResourceLocation(), buffer.readUtf());
        }

        @Override
        protected void handle(SPacketViewConfig message, Player player) {
            ConfigFile file = ConfigFile.lookupFile(message.fileID);
            if(file != null && file.isClientOnly())
            {
                Map<String, ConfigOption<?>> optionMap = file.getAllOptions();
                if(optionMap.containsKey(message.option))
                {
                    ConfigOption<?> option = optionMap.get(message.option);
                    LightmansCurrency.getProxy().sendClientMessage(LCText.COMMAND_CONFIG_VIEW.get(option.getName()));
                    LightmansCurrency.getProxy().sendClientMessage(EasyText.literal(option.write()));
                }
                else
                    LightmansCurrency.getProxy().sendClientMessage(LCText.COMMAND_CONFIG_FAIL_MISSING.get().withStyle(ChatFormatting.RED));
            }
        }

    }

}
