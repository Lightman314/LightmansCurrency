package io.github.lightman314.lightmanscurrency.network.message.config;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;

public class CPacketEditConfig extends ClientToServerPacket {

    public static final Type<CPacketEditConfig> TYPE = cType("config_edit");
    public static final StreamCodec<ByteBuf,CPacketEditConfig> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,p -> p.fileID,
            ByteBufCodecs.STRING_UTF8,p -> p.option,
            ByteBufCodecs.STRING_UTF8,p -> p.input,
            CPacketEditConfig::new);

    private final Identifier fileID;
    private final String option;
    private final String input;
    public CPacketEditConfig(Identifier fileID,String option,String input) {
        super(TYPE);
        this.fileID = fileID;
        this.option = option;
        this.input = input;
    }

    @Override
    protected void handle(IPayloadContext context, Player player) {
        ConfigFile file = ConfigFile.lookupFile(this.fileID);
        if(file != null && !file.isClientOnly() && player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            Map<String,ConfigOption<?>> optionMap = file.getAllOptions();
            ConfigOption<?> option = optionMap.get(this.option);
            if(option != null) {
                LightmansCurrency.LogDebug(player.getName().getString() + " changed " + file.getFileID() + " -> " + this.option + " to " + this.input);
                option.load(this.input,ConfigOption.LoadSource.COMMAND);
            }
            else
                LightmansCurrency.LogWarning("Failed to load config option edit packet!");
        }
        if(file != null && file.isClientOnly())
            LightmansCurrency.LogWarning("Attempted to change a client-only config on the server!");
    }

}
