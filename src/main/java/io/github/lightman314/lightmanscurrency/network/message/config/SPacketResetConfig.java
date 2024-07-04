package io.github.lightman314.lightmanscurrency.network.message.config;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;
import java.util.Map;

public class SPacketResetConfig extends ServerToClientPacket {

    private static final Type<SPacketResetConfig> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"s_config_reset"));
    public static final Handler<SPacketResetConfig> HANDLER = new H();

    private final String fileName;
    private final String option;

    public SPacketResetConfig(@Nonnull String fileName, @Nonnull String option)
    {
        super(TYPE);
        this.fileName = fileName;
        this.option = option;
    }

    private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull SPacketResetConfig message) {
        buffer.writeUtf(message.fileName);
        buffer.writeUtf(message.option);
    }
    private static SPacketResetConfig decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketResetConfig(buffer.readUtf(),buffer.readUtf()); }

    private static class H extends Handler<SPacketResetConfig>
    {
        protected H() { super(TYPE, easyCodec(SPacketResetConfig::encode,SPacketResetConfig::decode)); }
        @Override
        protected void handle(@Nonnull SPacketResetConfig message, @Nonnull IPayloadContext context, @Nonnull Player player) {
            for(ConfigFile file : ConfigFile.getAvailableFiles())
            {
                if(file.isClientOnly() && file.getFileName().equals(message.fileName))
                {
                    Map<String, ConfigOption<?>> optionMap = file.getAllOptions();
                    if(optionMap.containsKey(message.option))
                    {
                        ConfigOption<?> option = optionMap.get(message.option);
                        option.setToDefault();
                        LightmansCurrency.PROXY.sendClientMessage(LCText.COMMAND_CONFIG_EDIT_SUCCESS.get(option.getName(), option.write()));
                    }
                    else
                        LightmansCurrency.PROXY.sendClientMessage(LCText.COMMAND_CONFIG_FAIL_MISSING.get(message.option).withStyle(ChatFormatting.RED));
                }
            }
        }
    }

}
