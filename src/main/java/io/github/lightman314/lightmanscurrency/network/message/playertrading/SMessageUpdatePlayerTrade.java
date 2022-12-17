package io.github.lightman314.lightmanscurrency.network.message.playertrading;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.playertrading.ClientPlayerTrade;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SMessageUpdatePlayerTrade {

    private final ClientPlayerTrade data;

    public SMessageUpdatePlayerTrade(ClientPlayerTrade data) { this.data = data; }

    public static void encode(SMessageUpdatePlayerTrade message, FriendlyByteBuf buffer) { message.data.encode(buffer); }

    public static SMessageUpdatePlayerTrade decode(FriendlyByteBuf buffer) { return new SMessageUpdatePlayerTrade(ClientPlayerTrade.decode(buffer)); }

    public static void handle(SMessageUpdatePlayerTrade message, Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.loadPlayerTrade(message.data));
        supplier.get().setPacketHandled(true);
    }

}
