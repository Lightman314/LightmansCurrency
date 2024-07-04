package io.github.lightman314.lightmanscurrency.network;

import io.github.lightman314.lightmanscurrency.network.message.*;
import io.github.lightman314.lightmanscurrency.network.message.auction.*;
import io.github.lightman314.lightmanscurrency.network.message.bank.*;
import io.github.lightman314.lightmanscurrency.network.message.command.*;
import io.github.lightman314.lightmanscurrency.network.message.config.*;
import io.github.lightman314.lightmanscurrency.network.message.data.*;
import io.github.lightman314.lightmanscurrency.network.message.data.bank.*;
import io.github.lightman314.lightmanscurrency.network.message.data.team.*;
import io.github.lightman314.lightmanscurrency.network.message.data.trader.*;
import io.github.lightman314.lightmanscurrency.network.message.emergencyejection.*;
import io.github.lightman314.lightmanscurrency.network.message.event.*;
import io.github.lightman314.lightmanscurrency.network.message.interfacebe.*;
import io.github.lightman314.lightmanscurrency.network.message.menu.*;
import io.github.lightman314.lightmanscurrency.network.message.notifications.*;
import io.github.lightman314.lightmanscurrency.network.message.paygate.*;
import io.github.lightman314.lightmanscurrency.network.message.persistentdata.*;
import io.github.lightman314.lightmanscurrency.network.message.playertrading.*;
import io.github.lightman314.lightmanscurrency.network.message.tax.*;
import io.github.lightman314.lightmanscurrency.network.message.teams.*;
import io.github.lightman314.lightmanscurrency.network.message.trader.*;
import io.github.lightman314.lightmanscurrency.network.message.wallet.*;
import io.github.lightman314.lightmanscurrency.network.message.walletslot.*;
import io.github.lightman314.lightmanscurrency.network.message.time.*;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.network.packet.CustomPacket;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD,modid = LightmansCurrency.MODID)
public class LightmansCurrencyPacketHandler {
	
	public static final String PROTOCOL_VERSION = "1";

	private static PayloadRegistrar registrar = null;

	@SubscribeEvent
	public static void onPayloadRegister(RegisterPayloadHandlersEvent event) {

		registrar = event.registrar(PROTOCOL_VERSION);

		//ATM & Bank
		registerC2S(CPacketOpenATM.HANDLER);
		registerC2S(CPacketSelectBankAccount.HANDLER);
		registerC2S(CPacketBankInteraction.HANDLER);
		registerS2C(SPacketClearClientBank.HANDLER);
		registerS2C(SPacketUpdateClientBank.HANDLER);
		registerC2S(CPacketBankTransferTeam.HANDLER);
		registerC2S(CPacketBankTransferPlayer.HANDLER);
		registerS2C(SPacketBankTransferResponse.HANDLER);
		registerC2S(CPacketATMSetPlayerAccount.HANDLER);
		registerS2C(SPacketATMPlayerAccountResponse.HANDLER);
		registerS2C(SPacketSyncSelectedBankAccount.HANDLER);
		
		//Trader
		registerC2S(CPacketExecuteTrade.HANDLER);
		registerC2S(CPacketCollectCoins.HANDLER);
		registerC2S(CPacketStoreCoins.HANDLER);
		registerC2S(CPacketOpenStorage.HANDLER);
		registerC2S(CPacketOpenTrades.HANDLER);
		registerC2S(CPacketOpenNetworkTerminal.HANDLER);
		registerS2C(SPacketSyncUsers.HANDLER);
		registerC2S(CPacketAddOrRemoveTrade.HANDLER);

		//Paygate
		registerC2S(CPacketCollectTicketStubs.HANDLER);

		//Wallet
		registerS2C(SPacketPlayCoinSound.HANDLER);
		registerC2S(CPacketWalletExchangeCoins.HANDLER);
		registerC2S(CPacketWalletToggleAutoExchange.HANDLER);
		registerC2S(CPacketOpenWallet.HANDLER);
		registerC2S(CPacketOpenWalletBank.HANDLER);
		registerC2S(CPacketWalletQuickCollect.HANDLER);
		registerC2S(CPacketChestQuickCollect.HANDLER);

		//Wallet Inventory Slot
		registerS2C(SPacketSyncWallet.HANDLER);
		registerC2S(CPacketSetVisible.HANDLER);
		registerC2S(CPacketCreativeWalletEdit.HANDLER);

		//Trader Data Sync
		registerS2C(SPacketClearClientTraders.HANDLER);
		registerS2C(SPacketUpdateClientTrader.HANDLER);
		registerS2C(SPacketMessageRemoveClientTrader.HANDLER);
		
		//Auction House
		registerS2C(SPacketStartBid.HANDLER);
		registerC2S(CPacketSubmitBid.HANDLER);
		registerS2C(SPacketSyncAuctionStandDisplay.HANDLER);
		
		//Trader Interfaces
		registerC2S(CPacketInterfaceHandlerMessage.HANDLER);
		
		//Teams
		registerS2C(SPacketClearClientTeams.HANDLER);
		registerS2C(SPacketRemoveClientTeam.HANDLER);
		registerS2C(SPacketUpdateClientTeam.HANDLER);
		registerC2S(CPacketEditTeam.HANDLER);
		registerC2S(CPacketCreateTeam.HANDLER);
		registerS2C(SPacketCreateTeamResponse.HANDLER);

		//Lazy Menu Interaction
		registerS2C(SPacketLazyMenu.HANDLER);
		registerC2S(CPacketLazyMenu.HANDLER);

		//Notifications
		registerS2C(SPacketSyncNotifications.HANDLER);
		registerC2S(CPacketFlagNotificationsSeen.HANDLER);
		registerS2C(SPacketChatNotification.HANDLER);

		//Taxes
		registerS2C(SPacketUpdateClientTax.HANDLER);
		registerS2C(SPacketRemoveTax.HANDLER);

		//Core
		registerC2S(CPacketRequestNBT.HANDLER);
		registerS2C(SPacketSyncTime.HANDLER);
		
		//Command/Admin
		registerS2C(SPacketSyncAdminList.HANDLER);
		registerS2C(SPacketDebugTrader.HANDLER);
		
		//Coin Data
		registerConfigS2C(SPacketSyncCoinData.HANDLER);
		registerS2C(SPacketSyncCoinData.HANDLER);
		
		//Persistent Data
		registerC2S(CPacketCreatePersistentTrader.HANDLER);
		registerC2S(CPacketCreatePersistentAuction.HANDLER);
		
		//Ejection data
		registerS2C(SPacketSyncEjectionData.HANDLER);
		registerC2S(CPacketOpenEjectionMenu.HANDLER);

		//Player Trading
		registerS2C(SPacketSyncPlayerTrade.HANDLER);
		registerC2S(CPacketPlayerTradeInteraction.HANDLER);

		//Event Tracker Syncing
		registerS2C(SPacketSyncEventUnlocks.HANDLER);

		//Config System
		registerS2C(SPacketSyncConfig.HANDLER);
		registerS2C(SPacketReloadConfig.HANDLER);
		registerS2C(SPacketEditConfig.HANDLER);
		registerS2C(SPacketEditListConfig.HANDLER);
		registerS2C(SPacketResetConfig.HANDLER);
		registerS2C(SPacketViewConfig.HANDLER);

		registrar = null;

	}

	private static <T extends ServerToClientPacket> void registerS2C(CustomPacket.AbstractHandler<T> handler)
	{
		registrar.playToClient(handler.type, handler.codec, handler);
	}

	private static <T extends ClientToServerPacket> void registerC2S(CustomPacket.AbstractHandler<T> handler)
	{
		registrar.playToServer(handler.type, handler.codec, handler);
	}

	private static <T extends ServerToClientPacket> void registerConfigS2C(CustomPacket.ConfigHandler<T> handler)
	{
		registrar.configurationToClient(handler.type,handler.configCodec,handler);
	}
	
}
