package io.github.lightman314.lightmanscurrency.network.message.config;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;
import java.util.Map;

public class SPacketViewConfig extends ServerToClientPacket {

    private static final Type<SPacketViewConfig> TYPE = new Type<>(VersionUtil.lcResource("s_config_view"));
    public static final Handler<SPacketViewConfig> HANDLER = new H();

    private final String fileName;
    private final String option;

    public SPacketViewConfig(@Nonnull String fileName, @Nonnull String option)
    {
        super(TYPE);
        this.fileName = fileName;
        this.option = option;
    }

    private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull SPacketViewConfig message) {
        buffer.writeUtf(message.fileName);
        buffer.writeUtf(message.option);
    }
    private static SPacketViewConfig decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketViewConfig(buffer.readUtf(),buffer.readUtf()); }

    private static class H extends Handler<SPacketViewConfig>
    {
        protected H() { super(TYPE, easyCodec(SPacketViewConfig::encode,SPacketViewConfig::decode)); }
        @Override
        protected void handle(@Nonnull SPacketViewConfig message, @Nonnull IPayloadContext context, @Nonnull Player player) {
            for(ConfigFile file : ConfigFile.getAvailableFiles())
            {
                if(file.isClientOnly() && file.getFileName().equals(message.fileName))
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

}
