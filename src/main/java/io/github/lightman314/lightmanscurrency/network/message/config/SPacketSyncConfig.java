package io.github.lightman314.lightmanscurrency.network.message.config;

import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SPacketSyncConfig extends ServerToClientPacket {

    public static final Handler<SPacketSyncConfig> HANDLER = new H();

    private final ResourceLocation configID;
    private final Map<String,String> data;

    public SPacketSyncConfig(ResourceLocation configID, Map<String,String> data) {
        this.configID = configID;
        this.data = data;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(this.configID);
        buffer.writeInt(this.data.size());
        this.data.forEach((id,dat) -> {
            buffer.writeUtf(id);
            buffer.writeUtf(dat);
        });
    }

    private static class H extends Handler<SPacketSyncConfig>
    {

        @Override
        public SPacketSyncConfig decode(FriendlyByteBuf buffer) {
            ResourceLocation configID = buffer.readResourceLocation();
            int count = buffer.readInt();
            Map<String,String> data = new HashMap<>();
            for(int i = 0; i < count; ++i)
            {
                String id = buffer.readUtf();
                String dat = buffer.readUtf();
                data.put(id,dat);
            }
            return new SPacketSyncConfig(configID,data);
        }

        @Override
        protected void handle(SPacketSyncConfig message, Player player) {
            ConfigFile.handleSyncData(message.configID, message.data);
        }
    }

}
