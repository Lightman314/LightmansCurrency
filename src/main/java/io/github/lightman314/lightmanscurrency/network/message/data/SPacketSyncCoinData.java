package io.github.lightman314.lightmanscurrency.network.message.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.features.api_impl.CoinAPIImpl;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public final class SPacketSyncCoinData extends ServerToClientPacket {

    private static final Gson GSON = new GsonBuilder().create();

    public static final Type<SPacketSyncCoinData> TYPE = sType("s_sync_master_coin_list");
    public static final StreamCodec<FriendlyByteBuf,SPacketSyncCoinData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new,ByteBufCodecs.STRING_UTF8,ByteBufCodecs.STRING_UTF8.map(GsonHelper::parse,GSON::toJson)),SPacketSyncCoinData::getJson,
            SPacketSyncCoinData::new);

    private final Map<String,JsonObject> json;
    public Map<String,JsonObject> getJson() { return this.json; }
    public SPacketSyncCoinData(Map<String,JsonObject> json) { super(TYPE); this.json = json; }

    @Override
    protected void handle(IPayloadContext context, Player player) {
        CoinAPIImpl.handleSyncPacket(this,player.registryAccess());
    }


}