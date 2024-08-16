package io.github.lightman314.lightmanscurrency.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.common.playertrading.ClientPlayerTrade;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CommonProxy {

	public boolean isClient() { return false; }

	public void init(@Nonnull ModContainer modContainer) {}

	public void setupClient() {}
	
	public void clearClientTraders() {}
	
	public void updateTrader(CompoundTag compound) {}
	
	public void removeTrader(long traderID) {}
	
	public void clearTeams() {}
	
	public void updateTeam(CompoundTag compound) {}
	
	public void removeTeam(long teamID) {}
	
	public void clearBankAccounts() {}
	
	public void updateBankAccount(UUID player, CompoundTag compound) {}

	public void removeBankAccount(UUID player) {}

	public void receiveEmergencyEjectionData(CompoundTag compound) {}
	
	public void updateNotifications(NotificationData data) {}
	
	public void receiveNotification(Notification notification) {}
	
	public void receiveSelectedBankAccount(BankReference selectedAccount) {}

	public void updateTaxEntries(CompoundTag compound) {}

	public void removeTaxEntry(long id) {}

	public void openNotificationScreen() {}
	
	public void playCoinSound() {}
	
	public long getTimeDesync() { return 0; }
	
	public void setTimeDesync(long currentTime) { }
	
	public void loadAdminPlayers(List<UUID> serverAdminList) { }

	@Nullable
	public Level getDimension(boolean isClient, @Nonnull ResourceKey<Level> type)
	{
		if(!isClient)
		{
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if(server != null)
				return server.getLevel(type);
		}
		return null;
	}

	@Nonnull
	public Level safeGetDummyLevel() throws Exception{
		Level level = this.getDummyLevelFromServer();
		if(level != null)
			return level;
		throw new Exception("Could not get dummy level from server, as there is no active server!");
	}

	@Nullable
	protected final Level getDummyLevelFromServer() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
			return server.overworld();
		return null;
	}

	public void loadPlayerTrade(ClientPlayerTrade trade) { }

	public void syncEventUnlocks(@Nonnull List<String> unlocks) {}

	public void sendClientMessage(@Nonnull Component message) {}

	public List<GameProfile> getPlayerList(boolean logicalClient)
	{
		if(logicalClient)
			return new ArrayList<>();
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
			return server.getPlayerList().getPlayers().stream().map(ServerPlayer::getGameProfile).toList();
		return new ArrayList<>();
	}

	public RegistryAccess getClientRegistryHolder() { return null; }
	
}
