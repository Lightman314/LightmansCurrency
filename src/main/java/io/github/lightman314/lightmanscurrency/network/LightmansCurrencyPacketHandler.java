package io.github.lightman314.lightmanscurrency.network;

import io.github.lightman314.lightmanscurrency.network.message.*;
import io.github.lightman314.lightmanscurrency.network.message.atm.*;
import io.github.lightman314.lightmanscurrency.network.message.cashregister.*;
import io.github.lightman314.lightmanscurrency.network.message.coinmint.*;
import io.github.lightman314.lightmanscurrency.network.message.command.MessageSyncAdminList;
import io.github.lightman314.lightmanscurrency.network.message.config.*;
import io.github.lightman314.lightmanscurrency.network.message.extendedinventory.*;
import io.github.lightman314.lightmanscurrency.network.message.item_trader.*;
import io.github.lightman314.lightmanscurrency.network.message.logger.*;
import io.github.lightman314.lightmanscurrency.network.message.paygate.*;
import io.github.lightman314.lightmanscurrency.network.message.trader.*;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.*;
import io.github.lightman314.lightmanscurrency.network.message.wallet.*;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.message.ticket_machine.*;
import io.github.lightman314.lightmanscurrency.network.message.time.MessageSyncClientTime;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
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
		register(MessageATM.class, new MessageATM());
		
		//Coinmint
		register(MessageMintCoin.class, new MessageMintCoin());
		
		//Trader
		register(MessageExecuteTrade.class, new MessageExecuteTrade());
		register(MessageCollectCoins.class, new MessageCollectCoins());
		register(MessageStoreCoins.class, new MessageStoreCoins());
		register(MessageOpenStorage.class, new MessageOpenStorage());
		register(MessageOpenTrades.class, new MessageOpenTrades());
		register(MessageSetCustomName.class, new MessageSetCustomName());
		register(MessageSyncTrades.class, new MessageSyncTrades());
		register(MessageToggleCreative.class, new MessageToggleCreative());
		register(MessageAddOrRemoveTrade.class, new MessageAddOrRemoveTrade());
		register(MessageSyncUsers.class, new MessageSyncUsers());
		register(MessageRequestSyncUsers.class, new MessageRequestSyncUsers());
		register(MessageAddOrRemoveAlly.class, new MessageAddOrRemoveAlly());
		
		//Item Trader
		register(MessageSetItemPrice.class, new MessageSetItemPrice());
		
		register(MessageItemEditSet.class, new MessageItemEditSet());
		register(MessageItemEditClose.class, new MessageItemEditClose());
		register(MessageOpenItemEdit.class, new MessageOpenItemEdit());
		
		//Cash Register
		register(MessageCRNextTrader.class, new MessageCRNextTrader());
		register(MessageCRSkipTo.class, new MessageCRSkipTo());
		
		//Wallet
		register(MessagePlayPickupSound.class, new MessagePlayPickupSound());
		register(MessageWalletConvertCoins.class, new MessageWalletConvertCoins());
		register(MessageWalletToggleAutoConvert.class, new MessageWalletToggleAutoConvert());
		register(MessageOpenWallet.class, new MessageOpenWallet());
		
		//Extended Inventory
		register(MessageUpdateWallet.class, new MessageUpdateWallet());
		
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
		register(MessageRequestTraders.class, new MessageRequestTraders());
		register(MessageUpdateTraders.class, new MessageUpdateTraders());
		register(MessageUpdateContainerData.class, new MessageUpdateContainerData());
		register(MessageSetCustomName2.class, new MessageSetCustomName2());
		register(MessageSetItemPrice2.class, new MessageSetItemPrice2());
		register(MessageAddOrRemoveAlly2.class, new MessageAddOrRemoveAlly2());
		
		//Logger
		register(MessageClearLogger.class, new MessageClearLogger());
		register(MessageClearUniversalLogger.class, new MessageClearUniversalLogger());
		
		//Trade Rules
		register(MessageSetTraderRules.class, new MessageSetTraderRules());
		register(MessageSetTraderRules2.class, new MessageSetTraderRules2());
		
		//Core
		register(MessageRequestNBT.class, new MessageRequestNBT());
		register(MessageSyncConfig.class, new MessageSyncConfig());
		register(MessageSyncClientTime.class, new MessageSyncClientTime());
		
		//Command/Admin
		register(MessageSyncAdminList.class, new MessageSyncAdminList());
		
	}

	private static <T> void register(Class<T> clazz, IMessage<T> message)
	{
		instance.registerMessage(nextId++, clazz, message::encode, message::decode, message::handle);
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
