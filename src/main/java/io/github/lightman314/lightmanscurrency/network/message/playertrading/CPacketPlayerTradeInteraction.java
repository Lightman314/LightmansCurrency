package io.github.lightman314.lightmanscurrency.network.message.playertrading;

import io.github.lightman314.lightmanscurrency.common.playertrading.PlayerTrade;
import io.github.lightman314.lightmanscurrency.common.playertrading.PlayerTradeManager;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketPlayerTradeInteraction extends ClientToServerPacket {

    private static final Type<CPacketPlayerTradeInteraction> TYPE = new Type<>(VersionUtil.lcResource("c_player_trade_interaction"));
    public static final Handler<CPacketPlayerTradeInteraction> HANDLER = new H();

    private final int tradeID;
    private final CompoundTag message;

    public CPacketPlayerTradeInteraction(int tradeID, CompoundTag message) { super(TYPE); this.tradeID = tradeID; this.message = message; }

    private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull CPacketPlayerTradeInteraction message) {
        buffer.writeInt(message.tradeID);
        buffer.writeNbt(message.message);
    }
    private static CPacketPlayerTradeInteraction decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketPlayerTradeInteraction(buffer.readInt(),readNBT(buffer)); }

    private static class H extends Handler<CPacketPlayerTradeInteraction>
    {
        protected H() { super(TYPE, easyCodec(CPacketPlayerTradeInteraction::encode,CPacketPlayerTradeInteraction::decode)); }
        @Override
        protected void handle(@Nonnull CPacketPlayerTradeInteraction message, @Nonnull IPayloadContext context, @Nonnull Player player) {
            PlayerTrade trade = PlayerTradeManager.GetTrade(message.tradeID);
            if(trade != null)
                trade.handleInteraction(player, message.message);
        }
    }

}
