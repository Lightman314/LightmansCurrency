package io.github.lightman314.lightmanscurrency.network;

import io.github.lightman314.lightmanscurrency.network.message.*;
import io.github.lightman314.lightmanscurrency.network.message.atm.*;
import io.github.lightman314.lightmanscurrency.network.message.cashregister.*;
import io.github.lightman314.lightmanscurrency.network.message.coinmint.*;
import io.github.lightman314.lightmanscurrency.network.message.command.MessageSyncAdminList;
import io.github.lightman314.lightmanscurrency.network.message.config.*;
import io.github.lightman314.lightmanscurrency.network.message.item_trader.*;
import io.github.lightman314.lightmanscurrency.network.message.logger.*;
import io.github.lightman314.lightmanscurrency.network.message.paygate.*;
import io.github.lightman314.lightmanscurrency.network.message.trader.*;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.*;
import io.github.lightman314.lightmanscurrency.network.message.wallet.*;
import io.github.lightman314.lightmanscurrency.network.message.walletslot.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.PacketTarget;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
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
		//ATM
		register(MessageATM.class, MessageATM::encode, MessageATM::decode, MessageATM::handle);
		
		//Coinmint
		register(MessageMintCoin.class, MessageMintCoin::encode, MessageMintCoin::decode, MessageMintCoin::handle);
		
		//Trader
		register(MessageExecuteTrade.class, MessageExecuteTrade::encode, MessageExecuteTrade::decode, MessageExecuteTrade::handle);
		register(MessageCollectCoins.class, MessageCollectCoins::encode, MessageCollectCoins::decode, MessageCollectCoins::handle);
		register(MessageStoreCoins.class, MessageStoreCoins::encode, MessageStoreCoins::decode, MessageStoreCoins::handle);
		register(MessageOpenStorage.class, MessageOpenStorage::encode, MessageOpenStorage::decode, MessageOpenStorage::handle);
		register(MessageOpenTrades.class, MessageOpenTrades::encode, MessageOpenTrades::decode, MessageOpenTrades::handle);
		register(MessageSetCustomName.class, MessageSetCustomName::encode, MessageSetCustomName::decode, MessageSetCustomName::handle);
		register(MessageToggleCreative.class, MessageToggleCreative::encode, MessageToggleCreative::decode, MessageToggleCreative::handle);
		register(MessageAddOrRemoveTrade.class, MessageAddOrRemoveTrade::encode, MessageAddOrRemoveTrade::decode, MessageAddOrRemoveTrade::handle);
		register(MessageSyncUsers.class, MessageSyncUsers::encode, MessageSyncUsers::decode, MessageSyncUsers::handle);
		register(MessageRequestSyncUsers.class, MessageRequestSyncUsers::encode, MessageRequestSyncUsers::decode, MessageRequestSyncUsers::handle);
		register(MessageAddOrRemoveAlly.class, MessageAddOrRemoveAlly::encode, MessageAddOrRemoveAlly::decode, MessageAddOrRemoveAlly::handle);
		
		//Item Trader
		register(MessageSetItemPrice.class, MessageSetItemPrice::encode, MessageSetItemPrice::decode, MessageSetItemPrice::handle);
		register(MessageItemEditSet.class, MessageItemEditSet::encode, MessageItemEditSet::decode, MessageItemEditSet::handle);
		register(MessageItemEditClose.class, MessageItemEditClose::encode, MessageItemEditClose::decode, MessageItemEditClose::handle);
		register(MessageOpenItemEdit.class, MessageOpenItemEdit::encode, MessageOpenItemEdit::decode, MessageOpenItemEdit::handle);
		register(MessageSetTradeItem.class, MessageSetTradeItem::encode, MessageSetTradeItem::decode, MessageSetTradeItem::handle);
		
		//Cash Register
		register(MessageCRNextTrader.class, MessageCRNextTrader::encode, MessageCRNextTrader::decode, MessageCRNextTrader::handle);
		register(MessageCRSkipTo.class, MessageCRSkipTo::encode, MessageCRSkipTo::decode, MessageCRSkipTo::handle);
		
		//Wallet
		register(MessagePlayPickupSound.class, MessagePlayPickupSound::encode, MessagePlayPickupSound::decode, MessagePlayPickupSound::handle);
		register(MessageWalletConvertCoins.class, MessageWalletConvertCoins::encode, MessageWalletConvertCoins::decode, MessageWalletConvertCoins::handle);
		register(MessageWalletToggleAutoConvert.class, MessageWalletToggleAutoConvert::encode, MessageWalletToggleAutoConvert::decode, MessageWalletToggleAutoConvert::handle);
		register(MessageOpenWallet.class, MessageOpenWallet::encode, MessageOpenWallet::decode, MessageOpenWallet::handle);
		
		//Wallet Inventory Slot
		register(SPacketSyncWallet.class, SPacketSyncWallet::encode, SPacketSyncWallet::decode, SPacketSyncWallet::handle);
		register(CPacketOpenVanilla.class, CPacketOpenVanilla::encode, CPacketOpenVanilla::decode, CPacketOpenVanilla::handle);
		register(SPacketOpenVanillaResponse.class, SPacketOpenVanillaResponse::encode, SPacketOpenVanillaResponse::decode, SPacketOpenVanillaResponse::handle);
		register(CPacketOpenWallet.class, CPacketOpenWallet::encode, CPacketOpenWallet::decode, CPacketOpenWallet::handle);
		register(SPacketGrabbedItem.class, SPacketGrabbedItem::encode, SPacketGrabbedItem::decode, SPacketGrabbedItem::handle);
		
		//Paygate
		register(MessageActivatePaygate.class, MessageActivatePaygate::encode, MessageActivatePaygate::decode, MessageActivatePaygate::handle);
		register(MessageUpdatePaygateData.class, MessageUpdatePaygateData::encode, MessageUpdatePaygateData::decode, MessageUpdatePaygateData::handle);
		register(MessageSetPaygateTicket.class, MessageSetPaygateTicket::encode, MessageSetPaygateTicket::decode, MessageSetPaygateTicket::handle);
		
		//Ticket Machine
		register(MessageCraftTicket.class, MessageCraftTicket::encode, MessageCraftTicket::decode, MessageCraftTicket::handle);
		
		//Universal Traders
		register(MessageOpenTrades2.class, MessageOpenTrades2::encode, MessageOpenTrades2::decode, MessageOpenTrades2::handle);
		register(MessageOpenStorage2.class, MessageOpenStorage2::encode, MessageOpenStorage2::decode, MessageOpenStorage2::handle);
		register(MessageSyncStorage.class, MessageSyncStorage::encode, MessageSyncStorage::decode, MessageSyncStorage::handle);
		register(MessageInitializeClientTraders.class, MessageInitializeClientTraders::encode, MessageInitializeClientTraders::decode, MessageInitializeClientTraders::handle);
		register(MessageUpdateClientData.class, MessageUpdateClientData::encode, MessageUpdateClientData::decode, MessageUpdateClientData::handle);
		register(MessageSetCustomName2.class, MessageSetCustomName2::encode, MessageSetCustomName2::decode, MessageSetCustomName2::handle);
		register(MessageSetItemPrice2.class, MessageSetItemPrice2::encode, MessageSetItemPrice2::decode, MessageSetItemPrice2::handle);
		register(MessageAddOrRemoveAlly2.class, MessageAddOrRemoveAlly2::encode, MessageAddOrRemoveAlly2::decode, MessageAddOrRemoveAlly2::handle);
		register(MessageSetTradeItem2.class, MessageSetTradeItem2::encode, MessageSetTradeItem2::decode, MessageSetTradeItem2::handle);
		register(MessageRemoveClientTrader.class, MessageRemoveClientTrader::encode, MessageRemoveClientTrader::decode, MessageRemoveClientTrader::handle);
		
		//Logger
		register(MessageClearLogger.class, MessageClearLogger::encode, MessageClearLogger::decode, MessageClearLogger::handle);
		register(MessageClearUniversalLogger.class, MessageClearUniversalLogger::encode, MessageClearUniversalLogger::decode, MessageClearUniversalLogger::handle);
		
		//Trade Rules
		register(MessageSetTraderRules.class, MessageSetTraderRules::encode, MessageSetTraderRules::decode, MessageSetTraderRules::handle);
		register(MessageSetTraderRules2.class, MessageSetTraderRules2::encode, MessageSetTraderRules2::decode, MessageSetTraderRules2::handle);
		
		//Core
		register(MessageRequestNBT.class, MessageRequestNBT::encode, MessageRequestNBT::decode, MessageRequestNBT::handle);
		register(MessageSyncConfig.class, MessageSyncConfig::encode, MessageSyncConfig::decode, MessageSyncConfig::handle);
		register(MessageSyncClientTime.class, MessageSyncClientTime::encode, MessageSyncClientTime::decode, MessageSyncClientTime::handle);
		
		//Command/Admin
		register(MessageSyncAdminList.class, MessageSyncAdminList::encode, MessageSyncAdminList::decode, MessageSyncAdminList::handle);
		
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
	
}
