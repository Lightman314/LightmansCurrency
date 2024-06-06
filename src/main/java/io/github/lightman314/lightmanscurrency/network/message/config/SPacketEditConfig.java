package io.github.lightman314.lightmanscurrency.network.message.config;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Map;

public class SPacketEditConfig extends ServerToClientPacket {

    public static final Handler<SPacketEditConfig> HANDLER = new H();

    private final String fileName;
    private final String option;
    private final String input;

    public SPacketEditConfig(@Nonnull String fileName, @Nonnull String option, @Nonnull String input)
    {
        this.fileName = fileName;
        this.option = option;
        this.input = input;
    }

    @Override
    public void encode(@Nonnull FriendlyByteBuf buffer) {
        buffer.writeUtf(this.fileName);
        buffer.writeUtf(this.option);
        buffer.writeInt(this.input.length());
        buffer.writeUtf(this.input, this.input.length());
    }

    private static class H extends Handler<SPacketEditConfig>
    {

        @Nonnull
        @Override
        public SPacketEditConfig decode(@Nonnull FriendlyByteBuf buffer) {
            String fileName = buffer.readUtf();
            String option = buffer.readUtf();
            int inputLength = buffer.readInt();
            return new SPacketEditConfig(fileName, option, buffer.readUtf(inputLength));
        }

        @Override
        protected void handle(@Nonnull SPacketEditConfig message, @Nullable ServerPlayer sender) {
            for(ConfigFile file : ConfigFile.getAvailableFiles())
            {
                if(file.isClientOnly() && file.getFileName().equals(message.fileName))
                {
                    Map<String, ConfigOption<?>> optionMap = file.getAllOptions();
                    if(optionMap.containsKey(message.option))
                    {
                        ConfigOption<?> option = optionMap.get(message.option);
                        Pair<Boolean,ConfigParsingException> result = option.load(message.input, ConfigOption.LoadSource.COMMAND);
                        if(!result.getFirst())
                        {
                            LightmansCurrency.PROXY.sendClientMessage(LCText.COMMAND_CONFIG_EDIT_FAIL_PARSE.get(result.getSecond().getMessage()).withStyle(ChatFormatting.RED));
                            return;
                        }
                        LightmansCurrency.PROXY.sendClientMessage(LCText.COMMAND_CONFIG_EDIT_SUCCESS.get(message.option, message.input));
                        return;
                    }
                    else
                        LightmansCurrency.PROXY.sendClientMessage(LCText.COMMAND_CONFIG_FAIL_MISSING.get(message.option).withStyle(ChatFormatting.RED));
                }
            }
        }

    }

}
