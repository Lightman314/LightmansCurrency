package io.github.lightman314.lightmanscurrency.network;

import io.github.lightman314.lightmanscurrency.network.message.data.*;
import io.github.lightman314.lightmanscurrency.network.message.debug.SPacketDebugTraderData;
import io.github.lightman314.lightmanscurrency.network.message.wallet.CPacketCreativeWalletUpdate;
import io.github.lightman314.lightmanscurrency.network.message.wallet.CPacketSetWalletVisibility;
import io.github.lightman314.lightmanscurrency.network.packet.BiDirectionalPacket;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.network.packet.CustomPacket;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class LCPacketHandler {

    public static final String PROTOCOL_VERSION = "2";

    private static PayloadRegistrar registrar = null;

    @SubscribeEvent
    private static void onPayloadRegister(RegisterPayloadHandlersEvent event)
    {
        registrar = event.registrar(PROTOCOL_VERSION);

        //Coin Data
        registerS2C(SPacketSyncCoinData.HANDLER);

        //Wallet
        registerC2S(CPacketSetWalletVisibility.HANDLER);
        registerC2S(CPacketCreativeWalletUpdate.HANDLER);

        //Menus
        registerBi(BPacketMenuMessage.HANDLER);

        //Fancy Data
        registerS2C(SPacketSyncFancyData.HANDLER);

        //Debug Packets
        registerS2C(SPacketDebugTraderData.HANDLER);

    }

    private static <T extends ServerToClientPacket> void registerS2C(CustomPacket.AbstractHandler<T> handler)
    {
        registrar.playToClient(handler.type,handler.codec,handler);
    }

    private static <T extends ClientToServerPacket> void registerC2S(CustomPacket.AbstractHandler<T> handler)
    {
        registrar.playToServer(handler.type,handler.codec,handler);
    }

    private static <T extends BiDirectionalPacket> void registerBi(CustomPacket.AbstractHandler<T> handler)
    {
        registrar.playBidirectional(handler.type,handler.codec,handler,handler);
    }

    private static <T extends ServerToClientPacket> void registerConfigS2C(CustomPacket.ConfigHandler<T> handler)
    {
        registrar.configurationToClient(handler.type,handler.configCodec,handler);
    }

}
