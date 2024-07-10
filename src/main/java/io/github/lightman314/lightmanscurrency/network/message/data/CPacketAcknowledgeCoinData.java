package io.github.lightman314.lightmanscurrency.network.message.data;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketAcknowledgeCoinData extends ClientToServerPacket {

    public static final Type<CPacketAcknowledgeCoinData> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"c_acknowledge_master_coin_list"));
    private static final CPacketAcknowledgeCoinData INSTANCE = new CPacketAcknowledgeCoinData();
    public static final Handler<CPacketAcknowledgeCoinData> HANDLER = new H();


    public static void sendToServer() { INSTANCE.send(); }

    protected CPacketAcknowledgeCoinData() { super(TYPE); }

    private static class H extends SimpleHandler<CPacketAcknowledgeCoinData>
    {
        protected H() { super(TYPE, INSTANCE); }
        @Override
        protected void handle(@Nonnull CPacketAcknowledgeCoinData message, @Nonnull IPayloadContext context, @Nonnull Player player) {
            context.finishCurrentTask(SPacketSyncCoinData.CONFIG_TYPE);
        }
    }

}
