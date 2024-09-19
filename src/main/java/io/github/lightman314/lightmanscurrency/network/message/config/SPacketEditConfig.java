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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;
import java.util.Map;

public class SPacketEditConfig extends ServerToClientPacket {

    private static final Type<SPacketEditConfig> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"s_config_edit"));
    public static final Handler<SPacketEditConfig> HANDLER = new H();

    private final String fileName;
    private final String option;
    private final String input;

    public SPacketEditConfig(@Nonnull String fileName, @Nonnull String option, @Nonnull String input)
    {
        super(TYPE);
        this.fileName = fileName;
        this.option = option;
        this.input = input;
    }

    private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull SPacketEditConfig message) {
        buffer.writeUtf(message.fileName);
        buffer.writeUtf(message.option);
        buffer.writeUtf(message.input);
    }

    private static SPacketEditConfig decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketEditConfig(buffer.readUtf(),buffer.readUtf(),buffer.readUtf()); }

    private static class H extends Handler<SPacketEditConfig>
    {

        protected H() { super(TYPE, easyCodec(SPacketEditConfig::encode,SPacketEditConfig::decode)); }
        @Override
        protected void handle(@Nonnull SPacketEditConfig message, @Nonnull IPayloadContext context, @Nonnull Player player) {
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
                            LightmansCurrency.getProxy().sendClientMessage(LCText.COMMAND_CONFIG_EDIT_FAIL_PARSE.get(result.getSecond().getMessage()).withStyle(ChatFormatting.RED));
                            return;
                        }
                        LightmansCurrency.getProxy().sendClientMessage(LCText.COMMAND_CONFIG_EDIT_SUCCESS.get(message.option, message.input));
                        return;
                    }
                    else
                        LightmansCurrency.getProxy().sendClientMessage(LCText.COMMAND_CONFIG_FAIL_MISSING.get(message.option).withStyle(ChatFormatting.RED));
                }
            }
        }
    }

}
