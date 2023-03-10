package io.github.lightman314.lightmanscurrency.network.message.playertrading;

import io.github.lightman314.lightmanscurrency.common.playertrading.PlayerTrade;
import io.github.lightman314.lightmanscurrency.common.playertrading.PlayerTradeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class CMessagePlayerTradeInteraction {

    private final int tradeID;
    private final CompoundNBT message;

    public CMessagePlayerTradeInteraction(int tradeID, CompoundNBT message) { this.tradeID = tradeID; this.message = message; }

    public static void encode(CMessagePlayerTradeInteraction message, PacketBuffer buffer) {
        buffer.writeInt(message.tradeID);
        buffer.writeNbt(message.message);
    }

    public static CMessagePlayerTradeInteraction decode(PacketBuffer buffer) {
        return new CMessagePlayerTradeInteraction(buffer.readInt(), buffer.readAnySizeNbt());
    }

    public static void handle(CMessagePlayerTradeInteraction message, Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() ->{
            PlayerTrade trade = PlayerTradeManager.GetTrade(message.tradeID);
            if(trade != null)
                trade.handleInteraction(supplier.get().getSender(), message.message);
        });
        supplier.get().setPacketHandled(true);
    }

}