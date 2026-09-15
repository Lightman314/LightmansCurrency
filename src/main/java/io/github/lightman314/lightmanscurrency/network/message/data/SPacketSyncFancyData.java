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

    public static final Type<SPacketSyncFancyData> TYPE = sType("sync_fancy_data");
    public static final StreamCodec<RegistryFriendlyByteBuf,SPacketSyncFancyData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(LCRegistries.Data.FANCY_DATA_KEY),p -> p.type,
            FancyPacketMap.STREAM_CODEC,p -> p.data,
            SPacketSyncFancyData::new);

    private final FancyDataType<?> type;
    private final FancyPacketMap data;
    public SPacketSyncFancyData(FancyDataType<?> type, FancyPacketMap data) {
        super(TYPE);
        this.type = type;
        this.data = data.immutable();
    }

    @Override
    protected void handle(IPayloadContext context, Player player) {
        FancyData data = ClientFancyDataCache.getData(this.type);
        if(data != null)
            data.receivePacket(this.data);
        else
            LightmansCurrency.LogError("Error getting client copy of the " + this.type + " data!");
    }

}
