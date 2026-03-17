package io.github.lightman314.lightmanscurrency.network.message.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SPacketSyncCoinData extends ServerToClientPacket {

    public static final ConfigurationTask.Type CONFIG_TYPE = new ConfigurationTask.Type(LightmansCurrency.id("send_coin_data"));


    private static final Type<SPacketSyncCoinData> TYPE = sType("sync_master_coin_list");
    private static final StreamCodec<FriendlyByteBuf,SPacketSyncCoinData> STREAM_CODEC = StreamCodec.of(SPacketSyncCoinData::encode,SPacketSyncCoinData::decode);
    public static final ConfigHandler<SPacketSyncCoinData> HANDLER = new H();

    private static final Gson GSON = new GsonBuilder().create();

    private final JsonObject json;
    public JsonObject getJson() { return this.json; }
    private boolean isConfigTask = false;
    public SPacketSyncCoinData configTask() { this.isConfigTask = true; return this; }
    public SPacketSyncCoinData(JsonObject json) { super(TYPE); this.json = json; }
    private SPacketSyncCoinData(JsonObject json, boolean isConfigTask) { this(json); this.isConfigTask = isConfigTask; }

    private static void encode(FriendlyByteBuf buffer,SPacketSyncCoinData message) {
        String jsonString = GSON.toJson(message.json);
        buffer.writeInt(jsonString.length());
        buffer.writeUtf(jsonString,jsonString.length());
        buffer.writeBoolean(message.isConfigTask);
    }
    private static SPacketSyncCoinData decode(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        String string = buffer.readUtf(size);
        return new SPacketSyncCoinData(GsonHelper.parse(string).getAsJsonObject(), buffer.readBoolean());
    }

    private static class H extends ConfigHandler<SPacketSyncCoinData>
    {
        protected H() { super(TYPE,STREAM_CODEC); }
        @Override
        public void handle(SPacketSyncCoinData message,IPayloadContext context) {
            CoinAPI.getApi().HandleSyncPacket(message);
            if(message.isConfigTask)
                context.reply(new CPacketAcknowledgeCoinData());
        }
    }


}
