package io.github.lightman314.lightmanscurrency.network.message.config;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Map;

public class SPacketResetConfig extends ServerToClientPacket {

    public static final Handler<SPacketResetConfig> HANDLER = new H();

    private final String fileName;
    private final String option;

    public SPacketResetConfig(@Nonnull String fileName, @Nonnull String option)
    {
        this.fileName = fileName;
        this.option = option;
    }

    @Override
    public void encode(@Nonnull FriendlyByteBuf buffer) {
        buffer.writeUtf(this.fileName);
        buffer.writeUtf(this.option);
    }

    private static class H extends Handler<SPacketResetConfig>
    {

        @Nonnull
        @Override
        public SPacketResetConfig decode(@Nonnull FriendlyByteBuf buffer) {
            String fileName = buffer.readUtf();
            String option = buffer.readUtf();
            return new SPacketResetConfig(fileName, option);
        }

        @Override
        protected void handle(@Nonnull SPacketResetConfig message, @Nullable ServerPlayer sender) {
            for(ConfigFile file : ConfigFile.getAvailableFiles())
            {
                if(file.isClientOnly() && file.getFileName().equals(message.fileName))
                {
                    Map<String, ConfigOption<?>> optionMap = file.getAllOptions();
                    if(optionMap.containsKey(message.option))
                    {
                        ConfigOption<?> option = optionMap.get(message.option);
                        option.setToDefault();
                        LightmansCurrency.PROXY.sendClientMessage(EasyText.translatable("command.lightmanscurrency.lcconfig.edit.success", option.getName(), option.write()));
                    }
                    else
                        LightmansCurrency.PROXY.sendClientMessage(EasyText.translatable("command.lightmanscurrency.lcconfig.edit.missing").withStyle(ChatFormatting.RED));
                }
            }
        }

    }

}
