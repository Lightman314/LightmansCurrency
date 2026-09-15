package io.github.lightman314.lightmanscurrency.network.message.debug;

import com.google.gson.JsonElement;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.JsonHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.data.CodecInteractionHelper;
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

    public static final Type<SPacketDebugTraderData> TYPE = sType("debug_trader_data");
    public static final StreamCodec<ByteBuf,SPacketDebugTraderData> STREAM_CODEC = ByteBufCodecs.LONG.map(SPacketDebugTraderData::new,p -> p.traderID);

    private final long traderID;
    public SPacketDebugTraderData(long traderID) { super(TYPE); this.traderID = traderID; }

    @Override
    protected void handle(IPayloadContext context, Player player) {
        TraderData trader = LCApi.getTraderAPI().getTrader(ISidedContext.LOGICAL_CLIENT, this.traderID);
        if(trader != null)
        {
            CodecInteractionHelper<JsonElement> encoder = CodecInteractionHelper.createJson(player.registryAccess());
            LightmansCurrency.LogInfo("Client Copy of Trader #" + this.traderID + ":\n" + JsonHelper.PRETTY_GSON.toJson(encoder.write(trader,TraderData.CODEC)));
            player.sendSystemMessage(Component.literal("Client data for trader #" + this.traderID + " has been printed to the logs!"));
        }
        else
            player.sendSystemMessage(Component.literal("Trader #" + this.traderID + " does not exist on the server."));
    }

}
