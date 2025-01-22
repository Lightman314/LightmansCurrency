package io.github.lightman314.lightmanscurrency.network.message.data;

import io.github.lightman314.lightmanscurrency.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomData;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomDataType;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.data.ClientCustomDataCache;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;
import java.util.Objects;

public class SPacketSyncCustomData extends ServerToClientPacket {

    private static final Type<SPacketSyncCustomData> TYPE = new Type<>(VersionUtil.lcResource("s_sync_custom_data"));
    public static final Handler<SPacketSyncCustomData> HANDLER = new H();

    private final ResourceLocation dataType;
    private final LazyPacketData data;
    public SPacketSyncCustomData(@Nonnull CustomDataType<?> type, @Nonnull LazyPacketData data) { this(LCRegistries.CUSTOM_DATA.getKey(type),data); }
    protected SPacketSyncCustomData(@Nonnull ResourceLocation dataType, @Nonnull LazyPacketData data) {
        super(TYPE);
        this.dataType = Objects.requireNonNull(dataType,"CustomDataType");
        this.data = data;
    }

    private static void encode(@Nonnull RegistryFriendlyByteBuf buffer, @Nonnull SPacketSyncCustomData message) { buffer.writeUtf(message.dataType.toString()); message.data.encode(buffer); }
    private static SPacketSyncCustomData decode(@Nonnull RegistryFriendlyByteBuf buffer) { return new SPacketSyncCustomData(VersionUtil.parseResource(buffer.readUtf()),LazyPacketData.decode(buffer)); }

    private static class H extends Handler<SPacketSyncCustomData>
    {

        protected H() { super(TYPE,fancyCodec(SPacketSyncCustomData::encode,SPacketSyncCustomData::decode)); }

        @Override
        public void handle(@Nonnull SPacketSyncCustomData message, @Nonnull IPayloadContext context, @Nonnull Player player) {

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
