package io.github.lightman314.lightmanscurrency.network.message.config;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
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
public class SPacketEditConfig extends ServerToClientPacket {

    public static final Handler<SPacketEditConfig> HANDLER = new H();

    private final ResourceLocation fileID;
    private final String option;
    private final String input;

    public SPacketEditConfig(ResourceLocation fileID, String option, String input)
    {
        this.fileID = fileID;
        this.option = option;
        this.input = input;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(this.fileID);
        buffer.writeUtf(this.option);
        buffer.writeInt(this.input.length());
        buffer.writeUtf(this.input, this.input.length());
    }

    private static class H extends Handler<SPacketEditConfig>
    {

        @Override
        public SPacketEditConfig decode(FriendlyByteBuf buffer) {
            ResourceLocation fileID = buffer.readResourceLocation();
            String option = buffer.readUtf();
            int inputLength = buffer.readInt();
            return new SPacketEditConfig(fileID, option, buffer.readUtf(inputLength));
        }

        @Override
        protected void handle(SPacketEditConfig message, Player player) {
            ConfigFile file = ConfigFile.lookupFile(message.fileID);
            if(file != null && file.isClientOnly())
            {
                Map<String, ConfigOption<?>> optionMap = file.getAllOptions();
                if(optionMap.containsKey(message.option))
                {
                    ConfigOption<?> option = optionMap.get(message.option);
                    Pair<Boolean,ConfigParsingException> result = option.load(message.input, ConfigOption.LoadSource.COMMAND);
                    if(!result.getFirst())
                    {
                        LightmansCurrency.getProxy().sendClientMessage(LCText.COMMAND_CONFIG_EDIT_FAIL_PARSE.get(result.getSecond().getMessage()).withStyle(ChatFormatting.RED));
                        return;
                    }
                    LightmansCurrency.getProxy().sendClientMessage(LCText.COMMAND_CONFIG_EDIT_SUCCESS.get(message.option, message.input));
                }
                else
                    LightmansCurrency.getProxy().sendClientMessage(LCText.COMMAND_CONFIG_FAIL_MISSING.get(message.option).withStyle(ChatFormatting.RED));
            }
        }

    }

}
