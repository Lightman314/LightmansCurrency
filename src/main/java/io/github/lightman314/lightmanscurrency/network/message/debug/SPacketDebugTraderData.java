package io.github.lightman314.lightmanscurrency.network.message.debug;

import com.google.gson.JsonElement;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.JsonHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SPacketDebugTraderData extends ServerToClientPacket {

    private static final Type<SPacketDebugTraderData> TYPE = sType("debug_trader_data");
    private static final StreamCodec<ByteBuf,SPacketDebugTraderData> STREAM_CODEC = ByteBufCodecs.LONG.map(SPacketDebugTraderData::new,p -> p.traderID);
    public static final Handler<SPacketDebugTraderData> HANDLER = new H();

    private final long traderID;
    public SPacketDebugTraderData(long traderID) { super(TYPE); this.traderID = traderID; }


    private static class H extends Handler<SPacketDebugTraderData>
    {
        protected H() { super(TYPE,STREAM_CODEC); }
        @Override
        protected void handle(SPacketDebugTraderData message, IPayloadContext context, Player player) {
            TraderData trader = LCApi.getTraderAPI().getTrader(ISidedContext.LOGICAL_CLIENT, message.traderID);
            if(trader != null)
            {
                DataContext<JsonElement> encoder = DataContext.createJson(player.registryAccess());
                LightmansCurrency.LogInfo("Client Copy of Trader #" + message.traderID + ":\n" + JsonHelper.PRETTY_GSON.toJson(encoder.write(trader,TraderData.CODEC)));
                player.sendSystemMessage(Component.literal("Client data for trader #" + message.traderID + " has been printed to the logs!"));
            }
            else
                player.sendSystemMessage(Component.literal("Trader #" + message.traderID + " does not exist on the server."));
        }
    }

}
