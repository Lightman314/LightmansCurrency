package io.github.lightman314.lightmanscurrency.network.message.config;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.MapLikeOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;
import java.util.Map;

public class SPacketEditMapConfig extends ServerToClientPacket {

    private static final Type<SPacketEditMapConfig> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"s_config_edit_map"));
    public static final Handler<SPacketEditMapConfig> HANDLER = new H();

    private final String fileName;
    private final String option;
    private final String input;
    private final String key;
    private final boolean isSet;

    public SPacketEditMapConfig(@Nonnull String fileName, @Nonnull String option, @Nonnull String input, String key, boolean isSet)
    {
        super(TYPE);
        this.fileName = fileName;
        this.option = option;
        this.input = input;
        this.key = key;
        this.isSet = isSet;
    }

    private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull SPacketEditMapConfig message) {
        buffer.writeUtf(message.fileName);
        buffer.writeUtf(message.option);
        buffer.writeUtf(message.input);
        buffer.writeUtf(message.key);
        buffer.writeBoolean(message.isSet);
    }
    private static SPacketEditMapConfig decode(@Nonnull FriendlyByteBuf buffer) {
        return new SPacketEditMapConfig(buffer.readUtf(),buffer.readUtf(), buffer.readUtf(), buffer.readUtf(), buffer.readBoolean());
    }

    private static class H extends Handler<SPacketEditMapConfig>
    {
        protected H() { super(TYPE, easyCodec(SPacketEditMapConfig::encode, SPacketEditMapConfig::decode)); }
        @Override
        protected void handle(@Nonnull SPacketEditMapConfig message, @Nonnull IPayloadContext context, @Nonnull Player player) {
            for(ConfigFile file : ConfigFile.getAvailableFiles())
            {
                if(file.isClientOnly() && file.getFileName().equals(message.fileName))
                {
                    Map<String, ConfigOption<?>> optionMap = file.getAllOptions();
                    if(optionMap.containsKey(message.option) && optionMap.get(message.option) instanceof MapLikeOption<?> option)
                    {
                        Pair<Boolean,ConfigParsingException> result = option.editMap(message.input, message.key, message.isSet);
                        if(!result.getFirst())
                        {
                            LightmansCurrency.getProxy().sendClientMessage(LCText.COMMAND_CONFIG_EDIT_FAIL_PARSE.get(result.getSecond().getMessage()).withStyle(ChatFormatting.RED));
                            return;
                        }
                        if(!message.isSet)
                            LightmansCurrency.getProxy().sendClientMessage(LCText.COMMAND_CONFIG_EDIT_LIST_REMOVE_SUCCESS.get(message.option + "[" + message.key + "]"));
                        LightmansCurrency.getProxy().sendClientMessage(LCText.COMMAND_CONFIG_EDIT_SUCCESS.get(message.option + "[" + message.key + "]", message.input));
                        return;
                    }
                    LightmansCurrency.getProxy().sendClientMessage(LCText.COMMAND_CONFIG_FAIL_MISSING.get(message.option).withStyle(ChatFormatting.RED));
                }
            }
        }
    }

}
