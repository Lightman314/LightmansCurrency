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

    private static final Type<SPacketSyncCoinData> TYPE = sType("s_sync_master_coin_list");
    private static final StreamCodec<FriendlyByteBuf,SPacketSyncCoinData> CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new,ByteBufCodecs.STRING_UTF8,ByteBufCodecs.STRING_UTF8.map(GsonHelper::parse,GSON::toJson)),SPacketSyncCoinData::getJson,
            SPacketSyncCoinData::new);
    public static final Handler<SPacketSyncCoinData> HANDLER = new H();

    private final Map<String,JsonObject> json;
    public Map<String,JsonObject> getJson() { return this.json; }
    public SPacketSyncCoinData(Map<String,JsonObject> json) { super(TYPE); this.json = json; }

    private static class H extends Handler<SPacketSyncCoinData>
    {
        protected H() { super(TYPE,CODEC); }
        @Override
        public void handle(SPacketSyncCoinData message,IPayloadContext context,Player player) {
            CoinAPIImpl.handleSyncPacket(message,player.registryAccess());
        }
    }


}