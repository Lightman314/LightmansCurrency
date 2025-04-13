package io.github.lightman314.lightmanscurrency.network.message.config;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;
import java.util.Map;

public class SPacketEditConfig extends ServerToClientPacket {

    private static final Type<SPacketEditConfig> TYPE = new Type<>(VersionUtil.lcResource("s_config_edit"));
    public static final Handler<SPacketEditConfig> HANDLER = new H();

    private final ResourceLocation fileID;
    private final String option;
    private final String input;

    public SPacketEditConfig(@Nonnull ResourceLocation fileID, @Nonnull String option, @Nonnull String input)
    {
        super(TYPE);
        this.fileID = fileID;
        this.option = option;
        this.input = input;
    }

    private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull SPacketEditConfig message) {
        buffer.writeResourceLocation(message.fileID);
        buffer.writeUtf(message.option);
        buffer.writeUtf(message.input);
    }

    private static SPacketEditConfig decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketEditConfig(buffer.readResourceLocation(),buffer.readUtf(),buffer.readUtf()); }

    private static class H extends Handler<SPacketEditConfig>
    {

        protected H() { super(TYPE, easyCodec(SPacketEditConfig::encode,SPacketEditConfig::decode)); }
        @Override
        protected void handle(@Nonnull SPacketEditConfig message, @Nonnull IPayloadContext context, @Nonnull Player player) {
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
