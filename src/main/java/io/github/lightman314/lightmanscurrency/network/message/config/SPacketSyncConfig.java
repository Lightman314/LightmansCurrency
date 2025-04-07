package io.github.lightman314.lightmanscurrency.network.message.config;

import io.github.lightman314.lightmanscurrency.api.config.SyncedConfigFile;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class SPacketSyncConfig extends ServerToClientPacket {

    private static final Type<SPacketSyncConfig> TYPE = new Type<>(VersionUtil.lcResource("s_config_sync"));
    public static final Handler<SPacketSyncConfig> HANDLER = new H();

    private final ResourceLocation configID;
    private final Map<String,String> data;

    public SPacketSyncConfig(@Nonnull ResourceLocation configID, @Nonnull Map<String,String> data) {
        super(TYPE);
        this.configID = configID;
        this.data = data;
    }

    private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull SPacketSyncConfig message){
        buffer.writeResourceLocation(message.configID);
        buffer.writeInt(message.data.size());
        message.data.forEach((id,dat) -> {
            buffer.writeUtf(id);
            buffer.writeUtf(dat);
        });
    }
    private static SPacketSyncConfig decode(@Nonnull FriendlyByteBuf buffer) {
        ResourceLocation configID = buffer.readResourceLocation();
        int count = buffer.readInt();
        Map<String,String> data = new HashMap<>();
        for(int i = 0; i < count; ++i)
            data.put(buffer.readUtf(),buffer.readUtf());
        return new SPacketSyncConfig(configID,data);
    }

    private static class H extends Handler<SPacketSyncConfig>
    {
        protected H() { super(TYPE, easyCodec(SPacketSyncConfig::encode,SPacketSyncConfig::decode)); }
        @Override
        protected void handle(@Nonnull SPacketSyncConfig message, @Nonnull IPayloadContext context, @Nonnull Player player) {
            SyncedConfigFile.handleSyncData(message.configID, message.data);
        }
    }

}
