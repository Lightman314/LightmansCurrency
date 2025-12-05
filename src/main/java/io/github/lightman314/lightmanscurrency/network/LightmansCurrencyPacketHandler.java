package io.github.lightman314.lightmanscurrency.network;

import io.github.lightman314.lightmanscurrency.network.message.*;
import io.github.lightman314.lightmanscurrency.network.message.auction.*;
import io.github.lightman314.lightmanscurrency.network.message.bank.*;
import io.github.lightman314.lightmanscurrency.network.message.cap.*;
import io.github.lightman314.lightmanscurrency.network.message.command.*;
import io.github.lightman314.lightmanscurrency.network.message.config.*;
import io.github.lightman314.lightmanscurrency.network.message.data.*;
import io.github.lightman314.lightmanscurrency.network.message.emergencyejection.*;
import io.github.lightman314.lightmanscurrency.network.message.enchantments.*;
import io.github.lightman314.lightmanscurrency.network.message.event.*;
import io.github.lightman314.lightmanscurrency.network.message.interfacebe.*;
import io.github.lightman314.lightmanscurrency.network.message.menu.*;
import io.github.lightman314.lightmanscurrency.network.message.notifications.*;
import io.github.lightman314.lightmanscurrency.network.message.paygate.*;
import io.github.lightman314.lightmanscurrency.network.message.persistentdata.*;
import io.github.lightman314.lightmanscurrency.network.message.player.*;
import io.github.lightman314.lightmanscurrency.network.message.playertrading.*;
import io.github.lightman314.lightmanscurrency.network.message.teams.*;
import io.github.lightman314.lightmanscurrency.network.message.trader.*;
import io.github.lightman314.lightmanscurrency.network.message.wallet.*;
import io.github.lightman314.lightmanscurrency.network.message.walletslot.*;
import io.github.lightman314.lightmanscurrency.network.message.time.*;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.network.packet.CustomPacket;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.PacketTarget;
import net.minecraftforge.network.simple.SimpleChannel;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class LightmansCurrencyPacketHandler {
	
	public static final String PROTOCOL_VERSION = "1";
	
	public static SimpleChannel instance;
	private static int nextId = 0;

	public static void init()
	{
		
		instance = NetworkRegistry.ChannelBuilder
				.named(VersionUtil.lcResource("network"))
				.networkProtocolVersion(() -> PROTOCOL_VERSION)
				.clientAcceptedVersions(PROTOCOL_VERSION::equals)
				.serverAcceptedVersions(PROTOCOL_VERSION::equals)
				.simpleChannel();
		
		//ATM & Bank
		registerC2S(CPacketOpenATM.class, CPacketOpenATM.HANDLER);
		registerC2S(CPacketSelectBankAccount.class, CPacketSelectBankAccount.HANDLER);
		registerC2S(CPacketBankInteraction.class, CPacketBankInteraction.HANDLER);
		registerC2S(CPacketBankTransferAccount.class, CPacketBankTransferAccount.HANDLER);
		registerC2S(CPacketBankTransferPlayer.class, CPacketBankTransferPlayer.HANDLER);
		registerS2C(SPacketBankTransferResponse.class, SPacketBankTransferResponse.HANDLER);
		registerC2S(CPacketATMSetPlayerAccount.class, CPacketATMSetPlayerAccount.HANDLER);
		registerS2C(SPacketATMPlayerAccountResponse.class, SPacketATMPlayerAccountResponse.HANDLER);
		
		//Trader
		registerC2S(CPacketExecuteTrade.class, CPacketExecuteTrade.HANDLER);
		registerC2S(CPacketCollectCoins.class, CPacketCollectCoins.HANDLER);
		registerC2S(CPacketOpenStorage.class, CPacketOpenStorage.HANDLER);
		registerC2S(CPacketOpenTrades.class, CPacketOpenTrades.HANDLER);
		registerC2S(CPacketOpenNetworkTerminal.class, CPacketOpenNetworkTerminal.HANDLER);
		registerS2C(SPacketSyncUsers.class, SPacketSyncUsers.HANDLER);
		registerC2S(CPacketAddOrRemoveTrade.class, CPacketAddOrRemoveTrade.HANDLER);
		registerS2C(SPacketTaxInfo.class, SPacketTaxInfo.HANDLER);

		//Paygate
		registerC2S(CPacketCollectTicketStubs.class, CPacketCollectTicketStubs.HANDLER);

		//Wallet
		registerS2C(SPacketPlayCoinSound.class, SPacketPlayCoinSound.HANDLER);
		registerC2S(CPacketWalletExchangeCoins.class, CPacketWalletExchangeCoins.HANDLER);
		registerC2S(CPacketWalletToggleAutoExchange.class, CPacketWalletToggleAutoExchange.HANDLER);
		registerC2S(CPacketOpenWallet.class, CPacketOpenWallet.HANDLER);
		registerC2S(CPacketOpenWalletBank.class, CPacketOpenWalletBank.HANDLER);
		registerC2S(CPacketWalletQuickCollect.class, CPacketWalletQuickCollect.HANDLER);
		registerC2S(CPacketChestQuickCollect.class, CPacketChestQuickCollect.HANDLER);

		//Wallet Inventory Slot
		registerS2C(SPacketSyncWallet.class, SPacketSyncWallet.HANDLER);
		registerC2S(CPacketSetVisible.class, CPacketSetVisible.HANDLER);
		registerC2S(CPacketCreativeWalletEdit.class, CPacketCreativeWalletEdit.HANDLER);
		
		//Auction House
		registerS2C(SPacketStartBid.class, SPacketStartBid.HANDLER);
		registerC2S(CPacketSubmitBid.class, CPacketSubmitBid.HANDLER);
		registerS2C(SPacketSyncAuctionStandDisplay.class, SPacketSyncAuctionStandDisplay.HANDLER);
		
		//Trader Interfaces
		registerC2S(CPacketInterfaceHandlerMessage.class, CPacketInterfaceHandlerMessage.HANDLER);
		
		//Teams
		registerC2S(CPacketOpenTeamManager.class, CPacketOpenTeamManager.HANDLER);

		//Lazy Menu Interaction
		registerS2C(SPacketLazyMenu.class, SPacketLazyMenu.HANDLER);
		registerC2S(CPacketLazyMenu.class, CPacketLazyMenu.HANDLER);

		//Notifications
		registerS2C(SPacketChatNotification.class, SPacketChatNotification.HANDLER);
		registerC2S(CPacketOpenNotifications.class, CPacketOpenNotifications.HANDLER);

		//Core
		registerC2S(CPacketRequestNBT.class, CPacketRequestNBT.HANDLER);
		registerS2C(SPacketSyncTime.class, SPacketSyncTime.HANDLER);
		
		//Command/Admin
		registerS2C(SPacketSyncAdminList.class, SPacketSyncAdminList.HANDLER);
		registerS2C(SPacketDebugTrader.class, SPacketDebugTrader.HANDLER);
		
		//Coin Data
		registerS2C(SPacketSyncCoinData.class, SPacketSyncCoinData.HANDLER);
		
		//Enchantments
		registerS2C(SPacketMoneyMendingClink.class, SPacketMoneyMendingClink.HANDLER);
		
		//Persistent Data
		registerC2S(CPacketCreatePersistentTrader.class, CPacketCreatePersistentTrader.HANDLER);
		registerC2S(CPacketCreatePersistentAuction.class, CPacketCreatePersistentAuction.HANDLER);
		
		//Ejection data
		registerC2S(CPacketOpenEjectionMenu.class, CPacketOpenEjectionMenu.HANDLER);

		//Player Trading
		registerS2C(SPacketSyncPlayerTrade.class, SPacketSyncPlayerTrade.HANDLER);
		registerC2S(CPacketPlayerTradeInteraction.class, CPacketPlayerTradeInteraction.HANDLER);

		//Event Tracker Syncing
		registerS2C(SPacketSyncEventUnlocks.class, SPacketSyncEventUnlocks.HANDLER);

		//Config System
        registerC2S(CPacketEditConfig.class,CPacketEditConfig.HANDLER);
        registerC2S(CPacketTrackServerFile.class,CPacketTrackServerFile.HANDLER);
		registerS2C(SPacketSyncConfig.class, SPacketSyncConfig.HANDLER);
		registerS2C(SPacketReloadConfig.class, SPacketReloadConfig.HANDLER);
		registerS2C(SPacketEditConfig.class, SPacketEditConfig.HANDLER);
		registerS2C(SPacketEditListConfig.class, SPacketEditListConfig.HANDLER);
		registerS2C(SPacketEditMapConfig.class, SPacketEditMapConfig.HANDLER);
		registerS2C(SPacketResetConfig.class, SPacketResetConfig.HANDLER);
		registerS2C(SPacketViewConfig.class, SPacketViewConfig.HANDLER);

		registerC2S(CPacketRequestName.class, CPacketRequestName.HANDLER);
		registerC2S(CPacketRequestID.class,CPacketRequestID.HANDLER);
		registerS2C(SPacketUpdatePlayerCache.class,SPacketUpdatePlayerCache.HANDLER);

		//Custom Data Syncing
		registerS2C(SPacketSyncCustomData.class,SPacketSyncCustomData.HANDLER);

        //Variant Capability Syncing
        registerS2C(SPacketSyncVariantBECap.class,SPacketSyncVariantBECap.HANDLER);
        registerS2C(SPacketSyncVariantChunkCap.class,SPacketSyncVariantChunkCap.HANDLER);

	}

    private static <T extends ServerToClientPacket> void registerS2C(Class<T> clazz, CustomPacket.Handler<T> handler)
    {
        instance.registerMessage(nextId++, clazz,CustomPacket::encode,handler::decode,handler::handlePacket, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    private static <T extends ClientToServerPacket> void registerC2S(Class<T> clazz, CustomPacket.Handler<T> handler)
    {
        instance.registerMessage(nextId++,clazz,CustomPacket::encode,handler::decode,handler::handlePacket,Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }
	
	public static PacketTarget getTarget(Player player)
	{
		if(player instanceof ServerPlayer sp)
			return getTarget(sp);
		return null;
	}
	
	public static PacketTarget getTarget(ServerPlayer player) { return PacketDistributor.PLAYER.with(() -> player); }
	
}
