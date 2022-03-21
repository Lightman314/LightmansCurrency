package io.github.lightman314.lightmanscurrency.network;

import io.github.lightman314.lightmanscurrency.network.message.*;
import io.github.lightman314.lightmanscurrency.network.message.bank.*;
import io.github.lightman314.lightmanscurrency.network.message.cashregister.*;
import io.github.lightman314.lightmanscurrency.network.message.coinmint.*;
import io.github.lightman314.lightmanscurrency.network.message.command.*;
import io.github.lightman314.lightmanscurrency.network.message.item_trader.*;
import io.github.lightman314.lightmanscurrency.network.message.logger.*;
import io.github.lightman314.lightmanscurrency.network.message.paygate.*;
import io.github.lightman314.lightmanscurrency.network.message.teams.*;
import io.github.lightman314.lightmanscurrency.network.message.trader.*;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.*;
import io.github.lightman314.lightmanscurrency.network.message.wallet.*;
import io.github.lightman314.lightmanscurrency.network.message.walletslot.*;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.money.MoneyData;
import io.github.lightman314.lightmanscurrency.network.message.ticket_machine.*;
import io.github.lightman314.lightmanscurrency.network.message.time.MessageSyncClientTime;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;
import net.minecraftforge.fml.network.simple.SimpleChannel;

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
		//ATM
		register(MessageATMConversion.class, new MessageATMConversion());
		register(MessageATMSetAccount.class, MessageATMSetAccount::encode, MessageATMSetAccount::decode, MessageATMSetAccount::handle);
		register(MessageBankInteraction.class, MessageBankInteraction::encode, MessageBankInteraction::decode, MessageBankInteraction::handle);
		register(MessageInitializeClientBank.class, MessageInitializeClientBank::encode, MessageInitializeClientBank::decode, MessageInitializeClientBank::handle);
		register(MessageUpdateClientBank.class, MessageUpdateClientBank::encode, MessageUpdateClientBank::decode, MessageUpdateClientBank::handle);
		register(MessageBankTransferTeam.class, MessageBankTransferTeam::encode, MessageBankTransferTeam::decode, MessageBankTransferTeam::handle);
		register(MessageBankTransferPlayer.class, MessageBankTransferPlayer::encode, MessageBankTransferPlayer::decode, MessageBankTransferPlayer::handle);
		register(MessageBankTransferResponse.class, MessageBankTransferResponse::encode, MessageBankTransferResponse::decode, MessageBankTransferResponse::handle);
		
		//Coinmint
		register(MessageMintCoin.class, new MessageMintCoin());
		
		//Trader
		register(MessageExecuteTrade.class, new MessageExecuteTrade());
		register(MessageCollectCoins.class, new MessageCollectCoins());
		register(MessageStoreCoins.class, new MessageStoreCoins());
		register(MessageOpenStorage.class, new MessageOpenStorage());
		register(MessageOpenTrades.class, new MessageOpenTrades());
		register(MessageAddOrRemoveTrade.class, new MessageAddOrRemoveTrade());
		register(MessageSyncUsers.class, new MessageSyncUsers());
		register(MessageRequestSyncUsers.class, new MessageRequestSyncUsers());
		register(MessageChangeSettings.class, new MessageChangeSettings());
		
		//Item Trader
		register(MessageSetItemPrice.class, new MessageSetItemPrice());
		register(MessageItemEditSet.class, new MessageItemEditSet());
		register(MessageItemEditClose.class, new MessageItemEditClose());
		register(MessageOpenItemEdit.class, new MessageOpenItemEdit());
		register(MessageSetTradeItem.class, new MessageSetTradeItem());
		
		//Cash Register
		register(MessageCRNextTrader.class, new MessageCRNextTrader());
		register(MessageCRSkipTo.class, new MessageCRSkipTo());
		
		//Wallet
		register(MessagePlayPickupSound.class, new MessagePlayPickupSound());
		register(MessageWalletConvertCoins.class, new MessageWalletConvertCoins());
		register(MessageWalletToggleAutoConvert.class, new MessageWalletToggleAutoConvert());
		register(MessageOpenWallet.class, new MessageOpenWallet());
		
		//Wallet Inventory Slot
		register(SPacketSyncWallet.class, new SPacketSyncWallet());
		
		//Paygate
		register(MessageActivatePaygate.class, new MessageActivatePaygate());
		register(MessageUpdatePaygateData.class, new MessageUpdatePaygateData());
		register(MessageSetPaygateTicket.class, new MessageSetPaygateTicket());
		
		//Ticket Machine
		register(MessageCraftTicket.class, new MessageCraftTicket());
		
		//Universal Traders
		register(MessageOpenTrades2.class, new MessageOpenTrades2());
		register(MessageOpenStorage2.class, new MessageOpenStorage2());
		register(MessageSyncStorage.class, new MessageSyncStorage());
		register(MessageClearClientTraders.class, new MessageClearClientTraders());
		register(MessageUpdateClientData.class, new MessageUpdateClientData());
		register(MessageSetItemPrice2.class, new MessageSetItemPrice2());
		register(MessageSetTradeItem2.class, new MessageSetTradeItem2());
		register(MessageRemoveClientTrader.class, new MessageRemoveClientTrader());
		register(MessageChangeSettings2.class, new MessageChangeSettings2());
		register(MessageAddOrRemoveTrade2.class, new MessageAddOrRemoveTrade2());
		
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
		register(MessageClearLogger.class, new MessageClearLogger());
		register(MessageClearUniversalLogger.class, new MessageClearUniversalLogger());
		
		//Trade Rules
		register(MessageUpdateTradeRule.class, MessageUpdateTradeRule::encode, MessageUpdateTradeRule::decode, MessageUpdateTradeRule::handle);
		register(MessageUpdateTradeRule2.class, MessageUpdateTradeRule2::encode, MessageUpdateTradeRule2::decode, MessageUpdateTradeRule2::handle);
		
		//Core
		register(MessageRequestNBT.class, new MessageRequestNBT());
		register(MessageSyncClientTime.class, new MessageSyncClientTime());
		
		//Command/Admin
		register(MessageSyncAdminList.class, new MessageSyncAdminList());
		
		//Money Data
		register(MoneyData.class, MoneyData::encode, MoneyData::decode, MoneyData::handle);
		
	}

	private static <T> void register(Class<T> clazz, IMessage<T> message)
	{
		instance.registerMessage(nextId++, clazz, message::encode, message::decode, message::handle);
	}
	
	private static <T> void register(Class<T> clazz, BiConsumer<T,PacketBuffer> encode, Function<PacketBuffer,T> decode, BiConsumer<T,Supplier<Context>> handle)
	{
		instance.registerMessage(nextId++, clazz, encode, decode, handle);
	}
	
	public static PacketTarget getTarget(PlayerEntity player)
	{
		return getTarget((ServerPlayerEntity)player);
	}
	
	public static PacketTarget getTarget(ServerPlayerEntity player)
	{
		return PacketDistributor.PLAYER.with(() -> player);
	}
	
}
