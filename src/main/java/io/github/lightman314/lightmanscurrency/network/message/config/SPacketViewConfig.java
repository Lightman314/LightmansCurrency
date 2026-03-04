package io.github.lightman314.lightmanscurrency.network.message.config;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;

public class SPacketViewConfig extends ServerToClientPacket {

    private static final Type<SPacketViewConfig> TYPE = sType("config_view");
    private static final StreamCodec<ByteBuf,SPacketViewConfig> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,p -> p.fileID,
            ByteBufCodecs.STRING_UTF8,p -> p.option,
            SPacketViewConfig::new);
    public static final Handler<SPacketViewConfig> HANDLER = new H();

    private final ResourceLocation fileID;
    private final String option;

    public SPacketViewConfig(ResourceLocation fileID, String option)
    {
        super(TYPE);
        this.fileID = fileID;
        this.option = option;
    }

    private static class H extends Handler<SPacketViewConfig>
    {
        protected H() { super(TYPE,STREAM_CODEC); }
        @Override
        protected void handle(SPacketViewConfig message, IPayloadContext context, Player player) {
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
