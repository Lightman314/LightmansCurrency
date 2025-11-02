package io.github.lightman314.lightmanscurrency.network.message.data;

import io.github.lightman314.lightmanscurrency.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomData;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomDataType;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.data.ClientCustomDataCache;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SPacketSyncCustomData extends ServerToClientPacket {

    public static final Handler<SPacketSyncCustomData> HANDLER = new H();

    private final ResourceLocation dataType;
    private final LazyPacketData data;
    public SPacketSyncCustomData(CustomDataType<?> type, LazyPacketData data) { this(LCRegistries.CUSTOM_DATA.getKey(type),data); }
    protected SPacketSyncCustomData(ResourceLocation dataType, LazyPacketData data) {
        this.dataType = Objects.requireNonNull(dataType,"CustomDataType");
        this.data = data;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.dataType.toString());
        this.data.encode(buffer);
    }

    private static class H extends Handler<SPacketSyncCustomData>
    {
        private H() {}
        
        @Override
        public SPacketSyncCustomData decode(FriendlyByteBuf buffer) { return new SPacketSyncCustomData(VersionUtil.parseResource(buffer.readUtf()),LazyPacketData.decode(buffer)); }

        @Override
        protected void handle(SPacketSyncCustomData message, Player player) {

            CustomDataType<?> type = LCRegistries.CUSTOM_DATA.getValue(message.dataType);
            if(type == null)
            {
                LightmansCurrency.LogWarning("Recieved sync packet for custom data of type '" + message.dataType + "' from the server, but no such data is registered on the client!");
                return;
            }
            CustomData data = ClientCustomDataCache.getData(type);
            if(data == null)
            {
                LightmansCurrency.LogError("Error getting client copy of the '" + message.dataType + "' custom data!");
                return;
            }
            data.receivePacket(message.data);
        }
    }

}
