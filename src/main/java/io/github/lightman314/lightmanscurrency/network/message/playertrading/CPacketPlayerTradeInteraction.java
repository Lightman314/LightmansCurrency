package io.github.lightman314.lightmanscurrency.network.message.playertrading;

import io.github.lightman314.lightmanscurrency.common.playertrading.PlayerTrade;
import io.github.lightman314.lightmanscurrency.common.playertrading.PlayerTradeManager;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketPlayerTradeInteraction extends ClientToServerPacket {

    public static final Handler<CPacketPlayerTradeInteraction> HANDLER = new H();

    private final int tradeID;
    private final CompoundTag message;

    public CPacketPlayerTradeInteraction(int tradeID, CompoundTag message) { this.tradeID = tradeID; this.message = message; }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.tradeID);
        buffer.writeNbt(this.message);
    }

    private static class H extends Handler<CPacketPlayerTradeInteraction>
    {
        @Override
        public CPacketPlayerTradeInteraction decode(FriendlyByteBuf buffer) { return new CPacketPlayerTradeInteraction(buffer.readInt(), buffer.readAnySizeNbt()); }
        @Override
        protected void handle(CPacketPlayerTradeInteraction message, Player player) {
            PlayerTrade trade = PlayerTradeManager.GetTrade(message.tradeID);
            if(trade != null)
                trade.handleInteraction(player, message.message);
        }
    }

}
