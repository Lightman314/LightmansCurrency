package io.github.lightman314.lightmanscurrency.network.message.config;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;
import java.util.Map;

public class SPacketEditListConfig extends ServerToClientPacket {

    private static final Type<SPacketEditListConfig> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"s_config_edit_list"));
    public static final Handler<SPacketEditListConfig> HANDLER = new H();


    private final String fileName;
    private final String option;
    private final String input;
    private final int listIndex;
    private final boolean isEdit;

    public SPacketEditListConfig(@Nonnull String fileName, @Nonnull String option, @Nonnull String input, int listIndex, boolean isEdit)
    {
        super(TYPE);
        this.fileName = fileName;
        this.option = option;
        this.input = input;
        this.listIndex = listIndex;
        this.isEdit = isEdit;
    }

    private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull SPacketEditListConfig message) {
        buffer.writeUtf(message.fileName);
        buffer.writeUtf(message.option);
        buffer.writeUtf(message.input);
        buffer.writeInt(message.listIndex);
        buffer.writeBoolean(message.isEdit);
    }
    private static SPacketEditListConfig decode(@Nonnull FriendlyByteBuf buffer) {
        return new SPacketEditListConfig(buffer.readUtf(),buffer.readUtf(), buffer.readUtf(), buffer.readInt(), buffer.readBoolean());
    }

    private static class H extends Handler<SPacketEditListConfig>
    {
        protected H() { super(TYPE, easyCodec(SPacketEditListConfig::encode,SPacketEditListConfig::decode)); }
        @Override
        protected void handle(@Nonnull SPacketEditListConfig message, @Nonnull IPayloadContext context, @Nonnull Player player) {
            for(ConfigFile file : ConfigFile.getAvailableFiles())
            {
                if(file.isClientOnly() && file.getFileName().equals(message.fileName))
                {
                    Map<String, ConfigOption<?>> optionMap = file.getAllOptions();
                    if(optionMap.containsKey(message.option) && optionMap.get(message.option) instanceof ListOption<?> option)
                    {
                        Pair<Boolean,ConfigParsingException> result = option.editList(message.input, message.listIndex, message.isEdit);
                        if(!result.getFirst())
                        {
                            LightmansCurrency.PROXY.sendClientMessage(LCText.COMMAND_CONFIG_EDIT_FAIL_PARSE.get(result.getSecond().getMessage()).withStyle(ChatFormatting.RED));
                            return;
                        }
                        if(!message.isEdit)
                            LightmansCurrency.PROXY.sendClientMessage(LCText.COMMAND_CONFIG_EDIT_LIST_REMOVE_SUCCESS.get(message.option + "[" + message.listIndex + "]"));
                        int listIndex = message.listIndex;
                        if(listIndex < 0)
                            listIndex = option.get().size() - 1;
                        LightmansCurrency.PROXY.sendClientMessage(LCText.COMMAND_CONFIG_EDIT_SUCCESS.get(message.option + "[" + listIndex + "]", message.input));
                        return;
                    }
                    LightmansCurrency.PROXY.sendClientMessage(LCText.COMMAND_CONFIG_FAIL_MISSING.get(message.option).withStyle(ChatFormatting.RED));
                }
            }
        }
    }

}
