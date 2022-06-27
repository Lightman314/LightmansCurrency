package io.github.lightman314.lightmanscurrency.network;

import io.github.lightman314.lightmanscurrency.network.message.*;
import io.github.lightman314.lightmanscurrency.network.message.armor_display.*;
import io.github.lightman314.lightmanscurrency.network.message.auction.*;
import io.github.lightman314.lightmanscurrency.network.message.bank.*;
import io.github.lightman314.lightmanscurrency.network.message.coinmint.*;
import io.github.lightman314.lightmanscurrency.network.message.command.*;
import io.github.lightman314.lightmanscurrency.network.message.enchantments.*;
import io.github.lightman314.lightmanscurrency.network.message.interfacebe.*;
import io.github.lightman314.lightmanscurrency.network.message.logger.*;
import io.github.lightman314.lightmanscurrency.network.message.notifications.*;
import io.github.lightman314.lightmanscurrency.network.message.player.*;
import io.github.lightman314.lightmanscurrency.network.message.teams.*;
import io.github.lightman314.lightmanscurrency.network.message.trader.*;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.*;
import io.github.lightman314.lightmanscurrency.network.message.wallet.*;
import io.github.lightman314.lightmanscurrency.network.message.walletslot.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.PacketTarget;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.atm.ATMData;
import io.github.lightman314.lightmanscurrency.money.MoneyData;
import io.github.lightman314.lightmanscurrency.network.message.ticket_machine.*;
import io.github.lightman314.lightmanscurrency.network.message.time.MessageSyncClientTime;

public class LightmansCurrencyPacketHandler {
	
	public static final String PROTOCOL_VERSION = "1";
	
	public static SimpleChannel instance;
	private static int nextId = 0;
	
	public static void init()
	{
		
		instance = NetworkRegistry.ChannelBuilder
				.named(new ResourceLocation(LightmansCurrency.MODID,"network"))
				.networkProtocolVersion(() -> PROTOCOL_VERSION)
				.clientAcceptedVersions(PROTOCOL_VERSION::equals)
				.serverAcceptedVersions(PROTOCOL_VERSION::equals)
				.simpleChannel();
		
		//ATM & Bank
		register(MessageATMConversion.class, MessageATMConversion::encode, MessageATMConversion::decode, MessageATMConversion::handle);
		register(MessageATMSetAccount.class, MessageATMSetAccount::encode, MessageATMSetAccount::decode, MessageATMSetAccount::handle);
		register(MessageBankInteraction.class, MessageBankInteraction::encode, MessageBankInteraction::decode, MessageBankInteraction::handle);
		register(MessageInitializeClientBank.class, MessageInitializeClientBank::encode, MessageInitializeClientBank::decode, MessageInitializeClientBank::handle);
		register(MessageUpdateClientBank.class, MessageUpdateClientBank::encode, MessageUpdateClientBank::decode, MessageUpdateClientBank::handle);
		register(MessageBankTransferTeam.class, MessageBankTransferTeam::encode, MessageBankTransferTeam::decode, MessageBankTransferTeam::handle);
		register(MessageBankTransferPlayer.class, MessageBankTransferPlayer::encode, MessageBankTransferPlayer::decode, MessageBankTransferPlayer::handle);
		register(MessageBankTransferResponse.class, MessageBankTransferResponse::encode, MessageBankTransferResponse::decode, MessageBankTransferResponse::handle);
		register(MessageATMSetPlayerAccount.class, MessageATMSetPlayerAccount::encode, MessageATMSetPlayerAccount::decode, MessageATMSetPlayerAccount::handle);
		register(MessageATMPlayerAccountResponse.class, MessageATMPlayerAccountResponse::encode, MessageATMPlayerAccountResponse::decode, MessageATMPlayerAccountResponse::handle);
		register(MessageSetBankNotificationLevel.class, MessageSetBankNotificationLevel::encode, MessageSetBankNotificationLevel::decode, MessageSetBankNotificationLevel::handle);
		
		//Coinmint
		register(MessageMintCoin.class, MessageMintCoin::encode, MessageMintCoin::decode, MessageMintCoin::handle);
		
		//Trader
		register(MessageExecuteTrade.class, MessageExecuteTrade::encode, MessageExecuteTrade::decode, MessageExecuteTrade::handle);
		register(MessageCollectCoins.class, MessageCollectCoins::encode, MessageCollectCoins::decode, MessageCollectCoins::handle);
		register(MessageStoreCoins.class, MessageStoreCoins::encode, MessageStoreCoins::decode, MessageStoreCoins::handle);
		register(MessageOpenStorage.class, MessageOpenStorage::encode, MessageOpenStorage::decode, MessageOpenStorage::handle);
		register(MessageOpenTrades.class, MessageOpenTrades::encode, MessageOpenTrades::decode, MessageOpenTrades::handle);
		register(MessageSyncUsers.class, MessageSyncUsers::encode, MessageSyncUsers::decode, MessageSyncUsers::handle);
		register(MessageRequestSyncUsers.class, MessageRequestSyncUsers::encode, MessageRequestSyncUsers::decode, MessageRequestSyncUsers::handle);
		register(MessageAddOrRemoveTrade.class, MessageAddOrRemoveTrade::encode, MessageAddOrRemoveTrade::decode, MessageAddOrRemoveTrade::handle);
		register(MessageChangeSettings.class, MessageChangeSettings::encode, MessageChangeSettings::decode, MessageChangeSettings::handle);
		register(MessageStorageInteraction.class, MessageStorageInteraction::encode, MessageStorageInteraction::decode, MessageStorageInteraction::handle);
		register(MessageStorageInteractionC.class, MessageStorageInteractionC::encode, MessageStorageInteractionC::decode, MessageStorageInteractionC::handle);
		
		//Armor Display Trader
		register(MessageRequestArmorStandID.class, MessageRequestArmorStandID::encode, MessageRequestArmorStandID::decode, MessageRequestArmorStandID::handle);
		register(MessageSendArmorStandID.class, MessageSendArmorStandID::encode, MessageSendArmorStandID::decode, MessageSendArmorStandID::handle);
		
		//Wallet
		register(MessagePlayPickupSound.class, new MessagePlayPickupSound());
		register(MessageWalletConvertCoins.class, MessageWalletConvertCoins::encode, MessageWalletConvertCoins::decode, MessageWalletConvertCoins::handle);
		register(MessageWalletToggleAutoConvert.class, MessageWalletToggleAutoConvert::encode, MessageWalletToggleAutoConvert::decode, MessageWalletToggleAutoConvert::handle);
		register(MessageOpenWallet.class, MessageOpenWallet::encode, MessageOpenWallet::decode, MessageOpenWallet::handle);
		
		//Wallet Inventory Slot
		register(SPacketSyncWallet.class, SPacketSyncWallet::encode, SPacketSyncWallet::decode, SPacketSyncWallet::handle);
		register(CPacketSetVisible.class, CPacketSetVisible::encode, CPacketSetVisible::decode, CPacketSetVisible::handle);
		register(CPacketWalletInteraction.class, CPacketWalletInteraction::encode, CPacketWalletInteraction::decode, CPacketWalletInteraction::handle);
		
		//Ticket Machine
		register(MessageCraftTicket.class, MessageCraftTicket::encode, MessageCraftTicket::decode, MessageCraftTicket::handle);
		
		//Universal Traders
		register(MessageOpenTrades2.class, MessageOpenTrades2::encode, MessageOpenTrades2::decode, MessageOpenTrades2::handle);
		register(MessageOpenStorage2.class, MessageOpenStorage2::encode, MessageOpenStorage2::decode, MessageOpenStorage2::handle);
		register(MessageSyncStorage.class, MessageSyncStorage::encode, MessageSyncStorage::decode, MessageSyncStorage::handle);
		register(MessageClearClientTraders.class, MessageClearClientTraders::encode, MessageClearClientTraders::decode, MessageClearClientTraders::handle);
		register(MessageUpdateClientData.class, MessageUpdateClientData::encode, MessageUpdateClientData::decode, MessageUpdateClientData::handle);
		register(MessageRemoveClientTrader.class, MessageRemoveClientTrader::encode, MessageRemoveClientTrader::decode, MessageRemoveClientTrader::handle);
		register(MessageAddOrRemoveTrade2.class, MessageAddOrRemoveTrade2::encode, MessageAddOrRemoveTrade2::decode, MessageAddOrRemoveTrade2::handle);
		register(MessageChangeSettings2.class, MessageChangeSettings2::encode, MessageChangeSettings2::decode, MessageChangeSettings2::handle);
		
		//Auction House
		register(MessageStartBid.class, MessageStartBid::encode, MessageStartBid::decode, MessageStartBid::handle);
		register(MessageSubmitBid.class, MessageSubmitBid::encode, MessageSubmitBid::decode, MessageSubmitBid::handle);
		
		//Trader Interfaces
		register(MessageHandlerMessage.class, MessageHandlerMessage::encode, MessageHandlerMessage::decode, MessageHandlerMessage::handle);
		register(MessageInterfaceInteraction.class, MessageInterfaceInteraction::encode, MessageInterfaceInteraction::decode, MessageInterfaceInteraction::handle);
		
		//Teams
		register(MessageInitializeClientTeams.class, MessageInitializeClientTeams::encode, MessageInitializeClientTeams::decode, MessageInitializeClientTeams::handle);
		register(MessageRemoveClientTeam.class, MessageRemoveClientTeam::encode, MessageRemoveClientTeam::decode, MessageRemoveClientTeam::handle);
		register(MessageUpdateClientTeam.class, MessageUpdateClientTeam::encode, MessageUpdateClientTeam::decode, MessageUpdateClientTeam::handle);
		register(MessageEditTeam.class, MessageEditTeam::encode, MessageEditTeam::decode, MessageEditTeam::handle);
		register(MessageRenameTeam.class, MessageRenameTeam::encode, MessageRenameTeam::decode, MessageRenameTeam::handle);
		register(MessageDisbandTeam.class, MessageDisbandTeam::encode, MessageDisbandTeam::decode, MessageDisbandTeam::handle);
		register(MessageOpenTeamManager.class, MessageOpenTeamManager::encode, MessageOpenTeamManager::decode, MessageOpenTeamManager::handle);
		register(MessageCreateTeam.class, MessageCreateTeam::encode, MessageCreateTeam::decode, MessageCreateTeam::handle);
		register(MessageCreateTeamResponse.class, MessageCreateTeamResponse::encode, MessageCreateTeamResponse::decode, MessageCreateTeamResponse::handle);
		register(MessageCreateTeamBankAccount.class, MessageCreateTeamBankAccount::encode, MessageCreateTeamBankAccount::decode, MessageCreateTeamBankAccount::handle);
		register(MessageSetTeamBankLimit.class, MessageSetTeamBankLimit::encode, MessageSetTeamBankLimit::decode, MessageSetTeamBankLimit::handle);
		
		//Logger
		register(MessageClearLogger.class, MessageClearLogger::encode, MessageClearLogger::decode, MessageClearLogger::handle);
		register(MessageClearUniversalLogger.class, MessageClearUniversalLogger::encode, MessageClearUniversalLogger::decode, MessageClearUniversalLogger::handle);
		
		//Notifications
		register(MessageUpdateClientNotifications.class, MessageUpdateClientNotifications::encode, MessageUpdateClientNotifications::decode, MessageUpdateClientNotifications::handle);
		register(MessageFlagNotificationsSeen.class, MessageFlagNotificationsSeen::encode, MessageFlagNotificationsSeen::decode, MessageFlagNotificationsSeen::handle);
		register(MessageClientNotification.class, MessageClientNotification::encode, MessageClientNotification::decode, MessageClientNotification::handle);
		
		//Trade Rules
		register(MessageUpdateTradeRule.class, MessageUpdateTradeRule::encode, MessageUpdateTradeRule::decode, MessageUpdateTradeRule::handle);
		register(MessageUpdateTradeRule2.class, MessageUpdateTradeRule2::encode, MessageUpdateTradeRule2::decode, MessageUpdateTradeRule2::handle);
		
		//Core
		register(MessageRequestNBT.class, MessageRequestNBT::encode, MessageRequestNBT::decode, MessageRequestNBT::handle);
		register(MessageSyncClientTime.class, MessageSyncClientTime::encode, MessageSyncClientTime::decode, MessageSyncClientTime::handle);
		
		//Command/Admin
		register(MessageSyncAdminList.class, MessageSyncAdminList::encode, MessageSyncAdminList::decode, MessageSyncAdminList::handle);
		
		//Money Data
		register(MoneyData.class, MoneyData::encode, MoneyData::decode, MoneyData::handle);
		
		//ATM Data
		register(ATMData.class, ATMData::encode, ATMData::decode, ATMData::handle);
		
		//Enchantments
		register(SPacketMoneyMendingClink.class, LazyEncoders::emptyEncode, LazyEncoders.emptyDecode(SPacketMoneyMendingClink::new), SPacketMoneyMendingClink::handle);
		
		//Player List
		register(CPacketRequestPlayerList.class, LazyEncoders::emptyEncode, LazyEncoders.emptyDecode(CPacketRequestPlayerList::new), CPacketRequestPlayerList::handle);
		register(SPacketSendPlayerList.class, SPacketSendPlayerList::encode, SPacketSendPlayerList::decode, SPacketSendPlayerList::handle);
		
	}

	private static <T> void register(Class<T> clazz, IMessage<T> message)
	{
		instance.registerMessage(nextId++, clazz, message::encode, message::decode, message::handle);
	}
	
	private static <T> void register(Class<T> clazz, BiConsumer<T,FriendlyByteBuf> encoder, Function<FriendlyByteBuf,T> decoder, BiConsumer<T,Supplier<Context>> handler)
	{
		instance.registerMessage(nextId++, clazz, encoder, decoder, handler);
	}
	
	public static PacketTarget getTarget(Player player)
	{
		if(player instanceof ServerPlayer)
			return getTarget((ServerPlayer)player);
		return null;
	}
	
	public static PacketTarget getTarget(ServerPlayer player)
	{
		return PacketDistributor.PLAYER.with(() -> player);
	}
	
	private static class LazyEncoders
	{

		public static <T> void emptyEncode(T message, FriendlyByteBuf buffer) {}
		public static <T> Function<FriendlyByteBuf,T> emptyDecode(NonNullSupplier<T> get) { return (buffer) -> get.get(); }

	}
	
}
