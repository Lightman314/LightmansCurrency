package io.github.lightman314.lightmanscurrency.network;

import io.github.lightman314.lightmanscurrency.network.message.*;
import io.github.lightman314.lightmanscurrency.network.message.auction.*;
import io.github.lightman314.lightmanscurrency.network.message.bank.*;
import io.github.lightman314.lightmanscurrency.network.message.command.*;
import io.github.lightman314.lightmanscurrency.network.message.data.*;
import io.github.lightman314.lightmanscurrency.network.message.emergencyejection.*;
import io.github.lightman314.lightmanscurrency.network.message.enchantments.*;
import io.github.lightman314.lightmanscurrency.network.message.interfacebe.*;
import io.github.lightman314.lightmanscurrency.network.message.menu.*;
import io.github.lightman314.lightmanscurrency.network.message.notifications.*;
import io.github.lightman314.lightmanscurrency.network.message.paygate.CPacketCollectTicketStubs;
import io.github.lightman314.lightmanscurrency.network.message.persistentdata.*;
import io.github.lightman314.lightmanscurrency.network.message.playertrading.*;
import io.github.lightman314.lightmanscurrency.network.message.tax.*;
import io.github.lightman314.lightmanscurrency.network.message.teams.*;
import io.github.lightman314.lightmanscurrency.network.message.trader.*;
import io.github.lightman314.lightmanscurrency.network.message.wallet.*;
import io.github.lightman314.lightmanscurrency.network.message.walletslot.*;
import io.github.lightman314.lightmanscurrency.network.packet.CustomPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.*;
import net.minecraftforge.network.PacketDistributor.PacketTarget;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.atm.ATMData;
import io.github.lightman314.lightmanscurrency.common.money.MoneyData;
import io.github.lightman314.lightmanscurrency.network.message.time.SPacketSyncTime;

import javax.annotation.Nonnull;

public class LightmansCurrencyPacketHandler {
	
	public static final int PROTOCOL_VERSION = 1;
	
	public static SimpleChannel instance;

	public static void init()
	{
		
		instance = ChannelBuilder
				.named(new ResourceLocation(LightmansCurrency.MODID,"network"))
				.networkProtocolVersion(PROTOCOL_VERSION)
				.clientAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
				.serverAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
				.simpleChannel();
		
		//ATM & Bank
		register(CPacketOpenATM.class, CPacketOpenATM.HANDLER);
		register(CPacketSelectBankAccount.class, CPacketSelectBankAccount.HANDLER);
		register(CPacketBankInteraction.class, CPacketBankInteraction.HANDLER);
		register(SPacketInitializeClientBank.class, SPacketInitializeClientBank.HANDLER);
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
		
		//Money Data
		register(MoneyData.class, MoneyData.PACKET_HANDLER);
		
		//ATM Data
		register(ATMData.class, ATMData.PACKET_HANDLER);
		
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


	}

	private static <T extends CustomPacket> void register(@Nonnull Class<T> clazz, @Nonnull CustomPacket.Handler<T> handler)
	{
		instance.messageBuilder(clazz)
				.encoder(CustomPacket::encode)
				.decoder(handler::decode)
				.consumerMainThread(handler::handlePacket)
				.add();
	}
	
	public static PacketTarget getTarget(Player player)
	{
		if(player instanceof ServerPlayer)
			return getTarget((ServerPlayer)player);
		return null;
	}
	
	public static PacketTarget getTarget(ServerPlayer player) { return PacketDistributor.PLAYER.with(player); }
	
}
