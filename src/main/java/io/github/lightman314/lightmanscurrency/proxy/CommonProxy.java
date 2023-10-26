package io.github.lightman314.lightmanscurrency.proxy;

import java.util.List;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.common.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.common.playertrading.ClientPlayerTrade;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CommonProxy {

	public void setupClient() {}

	public void clearClientTraders() {}

	public void updateTrader(CompoundTag compound) {}

	public void removeTrader(long traderID) {}

	public void clearTeams() {}

	public void updateTeam(CompoundTag compound) {}

	public void removeTeam(long teamID) {}

	public void initializeBankAccounts(CompoundTag compound) {}

	public void updateBankAccount(CompoundTag compound) {}

	public void receiveEmergencyEjectionData(CompoundTag compound) {}

	public void updateNotifications(NotificationData data) {}

	public void receiveNotification(Notification notification) {}

	public void receiveSelectedBankAccount(BankReference selectedAccount) {}

	public void updateTaxEntries(CompoundTag compound) {}

	public void removeTaxEntry(long id) {}

	public void openNotificationScreen() {}

	public void openTeamManager() {}

	public void playCoinSound() {}

	public void createTeamResponse(long teamID) {}

	public long getTimeDesync() { return 0; }

	public void setTimeDesync(long currentTime) { }

	public void loadAdminPlayers(List<UUID> serverAdminList) { }

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

}