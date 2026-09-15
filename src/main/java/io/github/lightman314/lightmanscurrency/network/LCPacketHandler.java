package io.github.lightman314.lightmanscurrency.network;

import io.github.lightman314.lightmanscurrency.network.message.config.*;
import io.github.lightman314.lightmanscurrency.network.message.data.*;
import io.github.lightman314.lightmanscurrency.network.message.debug.*;
import io.github.lightman314.lightmanscurrency.network.message.player.CPacketRequestPlayerID;
import io.github.lightman314.lightmanscurrency.network.message.player.CPacketRequestPlayerName;
import io.github.lightman314.lightmanscurrency.network.message.player.SPacketPlayerCacheResult;
import io.github.lightman314.lightmanscurrency.network.message.system.*;
import io.github.lightman314.lightmanscurrency.network.message.wallet.*;
import io.github.lightman314.lightmanscurrency.network.packet.BiDirectionalPacket;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
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
        registerS2C(SPacketSyncCoinData.TYPE,SPacketSyncCoinData.STREAM_CODEC);

        //Wallet
        registerC2S(CPacketSetWalletVisibility.TYPE,CPacketSetWalletVisibility.STREAM_CODEC);
        registerC2S(CPacketCreativeWalletUpdate.TYPE,CPacketCreativeWalletUpdate.STREAM_CODEC);
        registerC2S(CPacketOpenWalletMenu.TYPE,StreamCodec.unit(CPacketOpenWalletMenu.INSTANCE));

        //Menus
        registerBi(BPacketMenuMessage.TYPE,BPacketMenuMessage.STREAM_CODEC);

        //Fancy Data
        registerS2C(SPacketSyncFancyData.TYPE,SPacketSyncFancyData.STREAM_CODEC);

        //System
        registerS2C(SPacketSyncTime.TYPE,SPacketSyncTime.STREAM_CODEC);
        registerS2C(SPacketSyncAdminStatus.TYPE,SPacketSyncAdminStatus.STREAM_CODEC);

        //Config
        registerS2C(SPacketSyncConfig.TYPE,SPacketSyncConfig.STREAM_CODEC);
        registerS2C(SPacketReloadConfig.TYPE,SPacketReloadConfig.STREAM_CODEC);

        //Player Name Cache
        registerC2S(CPacketRequestPlayerName.TYPE,CPacketRequestPlayerName.STREAM_CODEC);
        registerC2S(CPacketRequestPlayerID.TYPE,CPacketRequestPlayerID.STREAM_CODEC);
        registerS2C(SPacketPlayerCacheResult.TYPE,SPacketPlayerCacheResult.STREAM_CODEC);

        //Debug Packets
        registerS2C(SPacketDebugTraderData.TYPE,SPacketDebugTraderData.STREAM_CODEC);

    }

    private static <T extends ServerToClientPacket> void registerS2C(Type<T> type,StreamCodec<? super RegistryFriendlyByteBuf,T> codec)
    {
        registrar.playToClient(type,codec,T::handle);
    }

    private static <T extends ClientToServerPacket> void registerC2S(Type<T> type,StreamCodec<? super RegistryFriendlyByteBuf,T> codec)
    {
        registrar.playToServer(type,codec,T::handle);
    }

    private static <T extends BiDirectionalPacket> void registerBi(Type<T> type,StreamCodec<? super RegistryFriendlyByteBuf,T> codec)
    {
        registrar.playBidirectional(type,codec,T::handle,T::handle);
    }

    private static <T extends ServerToClientPacket> void registerConfigS2C(Type<T> type,StreamCodec<? super FriendlyByteBuf,T> codec)
    {
        registrar.configurationToClient(type,codec,T::handle);
    }

}
