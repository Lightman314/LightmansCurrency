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
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Map;

public class SPacketEditListConfig extends ServerToClientPacket {

    public static final Handler<SPacketEditListConfig> HANDLER = new H();


    private final String fileName;
    private final String option;
    private final String input;
    private final int listIndex;
    private final boolean isEdit;

    public SPacketEditListConfig(@Nonnull String fileName, @Nonnull String option, @Nonnull String input, int listIndex, boolean isEdit)
    {
        this.fileName = fileName;
        this.option = option;
        this.input = input;
        this.listIndex = listIndex;
        this.isEdit = isEdit;
    }

    @Override
    public void encode(@Nonnull FriendlyByteBuf buffer) {
        buffer.writeUtf(this.fileName);
        buffer.writeUtf(this.option);
        buffer.writeInt(this.input.length());
        buffer.writeUtf(this.input, this.input.length());
        buffer.writeInt(this.listIndex);
        buffer.writeBoolean(this.isEdit);
    }

    private static class H extends Handler<SPacketEditListConfig>
    {

        @Nonnull
        @Override
        public SPacketEditListConfig decode(@Nonnull FriendlyByteBuf buffer) {
            String fileName = buffer.readUtf();
            String option = buffer.readUtf();
            int inputLength = buffer.readInt();
            return new SPacketEditListConfig(fileName, option, buffer.readUtf(inputLength), buffer.readInt(), buffer.readBoolean());
        }

        @Override
        protected void handle(@Nonnull SPacketEditListConfig message, @Nullable ServerPlayer sender) {
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
                            LightmansCurrency.getProxy().sendClientMessage(LCText.COMMAND_CONFIG_EDIT_FAIL_PARSE.get(result.getSecond().getMessage()).withStyle(ChatFormatting.RED));
                            return;
                        }
                        if(!message.isEdit)
                            LightmansCurrency.getProxy().sendClientMessage(LCText.COMMAND_CONFIG_EDIT_LIST_REMOVE_SUCCESS.get(message.option + "[" + message.listIndex + "]"));
                        int listIndex = message.listIndex;
                        if(listIndex < 0)
                            listIndex = option.get().size() - 1;
                        LightmansCurrency.getProxy().sendClientMessage(LCText.COMMAND_CONFIG_EDIT_SUCCESS.get(message.option + "[" + listIndex + "]", message.input));
                        return;
                    }
                    LightmansCurrency.getProxy().sendClientMessage(LCText.COMMAND_CONFIG_FAIL_MISSING.get(message.option).withStyle(ChatFormatting.RED));
                }
            }
        }

    }

}
