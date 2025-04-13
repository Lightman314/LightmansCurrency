package io.github.lightman314.lightmanscurrency.network.message.config;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Map;

public class SPacketViewConfig extends ServerToClientPacket {

    public static final Handler<SPacketViewConfig> HANDLER = new H();

    private final ResourceLocation fileID;
    private final String option;

    public SPacketViewConfig(@Nonnull ResourceLocation fileID, @Nonnull String option)
    {
        this.fileID = fileID;
        this.option = option;
    }

    @Override
    public void encode(@Nonnull FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(this.fileID);
        buffer.writeUtf(this.option);
    }

    private static class H extends Handler<SPacketViewConfig>
    {

        @Nonnull
        @Override
        public SPacketViewConfig decode(@Nonnull FriendlyByteBuf buffer) {
            return new SPacketViewConfig(buffer.readResourceLocation(), buffer.readUtf());
        }

        @Override
        protected void handle(@Nonnull SPacketViewConfig message, @Nullable ServerPlayer sender) {
            ConfigFile file = ConfigFile.lookupFile(message.fileID);
            if(file != null && file.isClientOnly())
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
