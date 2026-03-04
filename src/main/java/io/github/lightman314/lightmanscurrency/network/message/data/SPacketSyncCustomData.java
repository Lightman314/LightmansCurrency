package io.github.lightman314.lightmanscurrency.network.message.data;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomData;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomDataType;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.data.ClientCustomDataCache;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Objects;

public class SPacketSyncCustomData extends ServerToClientPacket {

    private static final Type<SPacketSyncCustomData> TYPE = sType("sync_custom_data");
    private static final StreamCodec<RegistryFriendlyByteBuf,SPacketSyncCustomData> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,p -> p.dataType,
            LazyPacketData.STREAM_CODEC,p -> p.data,
            SPacketSyncCustomData::new);
    public static final Handler<SPacketSyncCustomData> HANDLER = new H();

    private final ResourceLocation dataType;
    private final LazyPacketData data;
    public SPacketSyncCustomData(CustomDataType<?> type, LazyPacketData data) { this(LCRegistries.CUSTOM_DATA.getKey(type),data); }
    protected SPacketSyncCustomData(ResourceLocation dataType, LazyPacketData data) {
        super(TYPE);
        this.dataType = Objects.requireNonNull(dataType,"CustomDataType");
        this.data = data;
    }

    private static class H extends Handler<SPacketSyncCustomData>
    {

        protected H() { super(TYPE,STREAM_CODEC); }

        @Override
        public void handle(SPacketSyncCustomData message, IPayloadContext context, Player player) {

            CustomDataType<?> type = LCRegistries.CUSTOM_DATA.get(message.dataType);
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
