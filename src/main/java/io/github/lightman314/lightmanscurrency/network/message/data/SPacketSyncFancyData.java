package io.github.lightman314.lightmanscurrency.network.message.data;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.data.FancyData;
import io.github.lightman314.lightmanscurrency.api.data.FancyDataType;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.client.data.ClientFancyDataCache;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SPacketSyncFancyData extends ServerToClientPacket {

    private static final Type<SPacketSyncFancyData> TYPE = sType("sync_fancy_data");
    private static final StreamCodec<RegistryFriendlyByteBuf,SPacketSyncFancyData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(LCRegistries.Data.FANCY_DATA_KEY),p -> p.type,
            FancyPacketMap.STREAM_CODEC,p -> p.data,
            SPacketSyncFancyData::new);
    public static final Handler<SPacketSyncFancyData> HANDLER = new H();

    private final FancyDataType<?> type;
    private final FancyPacketMap data;
    public SPacketSyncFancyData(FancyDataType<?> type, FancyPacketMap data) {
        super(TYPE);
        this.type = type;
        this.data = data.immutable();
    }

    private static class H extends Handler<SPacketSyncFancyData>
    {
        protected H() { super(TYPE,STREAM_CODEC); }
        @Override
        protected void handle(SPacketSyncFancyData message, IPayloadContext context, Player player) {
            FancyData data = ClientFancyDataCache.getData(message.type);
            if(data != null)
                data.receivePacket(message.data);
            else
                LightmansCurrency.LogError("Error getting client copy of the " + message.type + " data!");
        }
    }

}
