package io.github.lightman314.lightmanscurrency.network.message.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public final class SPacketSyncCoinData extends ServerToClientPacket {

    public static final Handler<SPacketSyncCoinData> HANDLER = new H();

    private static final Gson GSON = new GsonBuilder().create();

    private final JsonObject json;
    public JsonObject getJson() { return this.json; }
    public SPacketSyncCoinData(@Nonnull JsonObject json) { this.json = json; }

    @Override
    public void encode(@Nonnull FriendlyByteBuf buffer) {
        String jsonString = GSON.toJson(this.json);
        buffer.writeInt(jsonString.length());
        buffer.writeUtf(jsonString, jsonString.length());
    }

    private static class H extends Handler<SPacketSyncCoinData>
    {
        @Nonnull
        @Override
        public SPacketSyncCoinData decode(@Nonnull FriendlyByteBuf buffer) {
            int size = buffer.readInt();
            String string = buffer.readUtf(size);
            return new SPacketSyncCoinData(GsonHelper.parse(string).getAsJsonObject());
        }
        @Override
        protected void handle(@Nonnull SPacketSyncCoinData message, @Nullable ServerPlayer sender) { CoinAPI.API.HandleSyncPacket(message); }
    }


}
