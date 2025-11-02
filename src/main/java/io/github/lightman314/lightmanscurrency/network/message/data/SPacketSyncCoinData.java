package io.github.lightman314.lightmanscurrency.network.message.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class SPacketSyncCoinData extends ServerToClientPacket {

    public static final Handler<SPacketSyncCoinData> HANDLER = new H();

    private static final Gson GSON = new GsonBuilder().create();

    private final JsonObject json;
    public JsonObject getJson() { return this.json; }
    public SPacketSyncCoinData(JsonObject json) { this.json = json; }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        String jsonString = GSON.toJson(this.json);
        buffer.writeInt(jsonString.length());
        buffer.writeUtf(jsonString, jsonString.length());
    }

    private static class H extends Handler<SPacketSyncCoinData>
    {
        @Override
        public SPacketSyncCoinData decode(FriendlyByteBuf buffer) {
            int size = buffer.readInt();
            String string = buffer.readUtf(size);
            return new SPacketSyncCoinData(GsonHelper.parse(string).getAsJsonObject());
        }
        @Override
        protected void handle(SPacketSyncCoinData message, Player player) { CoinAPI.getApi().HandleSyncPacket(message); }
    }


}
