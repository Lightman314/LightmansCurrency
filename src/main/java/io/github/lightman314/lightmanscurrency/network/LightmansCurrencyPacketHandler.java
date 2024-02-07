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
import io.github.lightman314.lightmanscurrency.network.message.enchantments.*;
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
import io.github.lightman314.lightmanscurrency.network.packet.CustomPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.PacketTarget;
import net.minecraftforge.network.simple.SimpleChannel;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;


import javax.annotation.Nonnull;

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
		register(CPacketOpenATM.class, CPacketOpenATM.HANDLER);
		register(CPacketSelectBankAccount.class, CPacketSelectBankAccount.HANDLER);
		register(CPacketBankInteraction.class, CPacketBankInteraction.HANDLER);
		register(SPacketClearClientBank.class, SPacketClearClientBank.HANDLER);
		register(SPacketUpdateClientBank.class, SPacketUpdateClientBank.HANDLER);
		register(CPacketBankTransferTeam.class, CPacketBankTransferTeam.HANDLER);
		register(CPacketBankTransferPlayer.class, CPacketBankTransferPlayer.HANDLER);
		register(SPacketBankTransferResponse.class, SPacketBankTransferResponse.HANDLER);
		register(CPacketATMSetPlayerAccount.class, CPacketATMSetPlayerAccount.HANDLER);
		register(SPacketATMPlayerAccountResponse.class, SPacketATMPlayerAccountResponse.HANDLER);
		register(SPacketSyncSelectedBankAccount.class, SPacketSyncSelectedBankAccount.HANDLER);
		
		//Trader
		register(CPacketExecuteTrade.class, CPacketExecuteTrade.HANDLER);
		register(CPacketCollectCoins.class, CPacketCollectCoins.HANDLER);
		register(CPacketStoreCoins.class, CPacketStoreCoins.HANDLER);
		register(CPacketOpenStorage.class, CPacketOpenStorage.HANDLER);
		register(CPacketOpenTrades.class, CPacketOpenTrades.HANDLER);
		register(CPacketOpenNetworkTerminal.class, CPacketOpenNetworkTerminal.HANDLER);
		register(SPacketSyncUsers.class, SPacketSyncUsers.HANDLER);
		register(CPacketAddOrRemoveTrade.class, CPacketAddOrRemoveTrade.HANDLER);

		//Paygate
		register(CPacketCollectTicketStubs.class, CPacketCollectTicketStubs.HANDLER);

		//Wallet
		register(SPacketPlayPickupSound.class, SPacketPlayPickupSound.HANDLER);
		register(CPacketWalletExchangeCoins.class, CPacketWalletExchangeCoins.HANDLER);
		register(CPacketWalletToggleAutoExchange.class, CPacketWalletToggleAutoExchange.HANDLER);
		register(CPacketOpenWallet.class, CPacketOpenWallet.HANDLER);
		register(CPacketOpenWalletBank.class, CPacketOpenWalletBank.HANDLER);
		register(CPacketWalletQuickCollect.class, CPacketWalletQuickCollect.HANDLER);
		register(CPacketChestQuickCollect.class, CPacketChestQuickCollect.HANDLER);

		//Wallet Inventory Slot
		register(SPacketSyncWallet.class, SPacketSyncWallet.HANDLER);
		register(CPacketSetVisible.class, CPacketSetVisible.HANDLER);
		register(CPacketWalletInteraction.class, CPacketWalletInteraction.HANDLER);
		
		//Trader Data Sync
		register(SPacketClearClientTraders.class, SPacketClearClientTraders.HANDLER);
		register(SPacketUpdateClientTrader.class, SPacketUpdateClientTrader.HANDLER);
		register(SPacketMessageRemoveClientTrader.class, SPacketMessageRemoveClientTrader.HANDLER);
		
		//Auction House
		register(SPacketStartBid.class, SPacketStartBid.HANDLER);
		register(CPacketSubmitBid.class, CPacketSubmitBid.HANDLER);
		register(SPacketSyncAuctionStandDisplay.class, SPacketSyncAuctionStandDisplay.HANDLER);
		
		//Trader Interfaces
		register(CPacketInterfaceHandlerMessage.class, CPacketInterfaceHandlerMessage.HANDLER);
		register(CPacketInterfaceInteraction.class, CPacketInterfaceInteraction.HANDLER);
		
		//Teams
		register(SPacketClearClientTeams.class, SPacketClearClientTeams.HANDLER);
		register(SPacketRemoveClientTeam.class, SPacketRemoveClientTeam.HANDLER);
		register(SPacketUpdateClientTeam.class, SPacketUpdateClientTeam.HANDLER);
		register(CPacketEditTeam.class, CPacketEditTeam.HANDLER);
		register(CPacketCreateTeam.class, CPacketCreateTeam.HANDLER);
		register(SPacketCreateTeamResponse.class, SPacketCreateTeamResponse.HANDLER);

		//Lazy Menu Interaction
		register(SPacketLazyMenu.class, SPacketLazyMenu.HANDLER);
		register(CPacketLazyMenu.class, CPacketLazyMenu.HANDLER);

		//Notifications
		register(SPacketSyncNotifications.class, SPacketSyncNotifications.HANDLER);
		register(CPacketFlagNotificationsSeen.class, CPacketFlagNotificationsSeen.HANDLER);
		register(SPacketChatNotification.class, SPacketChatNotification.HANDLER);

		//Taxes
		register(SPacketSyncClientTax.class, SPacketSyncClientTax.HANDLER);
		register(SPacketRemoveTax.class, SPacketRemoveTax.HANDLER);

		//Core
		register(CPacketRequestNBT.class, CPacketRequestNBT.HANDLER);
		register(SPacketSyncTime.class, SPacketSyncTime.HANDLER);
		
		//Command/Admin
		register(SPacketSyncAdminList.class, SPacketSyncAdminList.HANDLER);
		register(SPacketDebugTrader.class, SPacketDebugTrader.HANDLER);
		
		//Coin Data
		register(SPacketSyncCoinData.class, SPacketSyncCoinData.HANDLER);
		
		//Enchantments
		register(SPacketMoneyMendingClink.class, SPacketMoneyMendingClink.HANDLER);
		
		//Persistent Data
		register(CPacketCreatePersistentTrader.class, CPacketCreatePersistentTrader.HANDLER);
		register(CPacketCreatePersistentAuction.class, CPacketCreatePersistentAuction.HANDLER);
		
		//Ejection data
		register(SPacketSyncEjectionData.class, SPacketSyncEjectionData.HANDLER);
		register(CPacketOpenEjectionMenu.class, CPacketOpenEjectionMenu.HANDLER);

		//Player Trading
		register(SPacketSyncPlayerTrade.class, SPacketSyncPlayerTrade.HANDLER);
		register(CPacketPlayerTradeInteraction.class, CPacketPlayerTradeInteraction.HANDLER);

		//Event Tracker Syncing
		register(SPacketSyncEventUnlocks.class, SPacketSyncEventUnlocks.HANDLER);

		//Config System
		register(SPacketSyncConfig.class, SPacketSyncConfig.HANDLER);
		register(SPacketReloadConfig.class, SPacketReloadConfig.HANDLER);
		register(SPacketEditConfig.class, SPacketEditConfig.HANDLER);
		register(SPacketEditListConfig.class, SPacketEditListConfig.HANDLER);
		register(SPacketResetConfig.class, SPacketResetConfig.HANDLER);
		register(SPacketViewConfig.class, SPacketViewConfig.HANDLER);


	}

	private static <T extends CustomPacket> void register(@Nonnull Class<T> clazz, @Nonnull CustomPacket.Handler<T> handler)
	{
		instance.registerMessage(nextId++, clazz, CustomPacket::encode, handler::decode, handler::handlePacket);
	}
	
	public static PacketTarget getTarget(Player player)
	{
		if(player instanceof ServerPlayer)
			return getTarget((ServerPlayer)player);
		return null;
	}
	
	public static PacketTarget getTarget(ServerPlayer player) { return PacketDistributor.PLAYER.with(() -> player); }
	
}
