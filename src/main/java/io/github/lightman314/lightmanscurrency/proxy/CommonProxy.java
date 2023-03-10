package io.github.lightman314.lightmanscurrency.proxy;

import java.util.List;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.common.playertrading.ClientPlayerTrade;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CommonProxy {
	
	public void setupClient() {}
	
	public void clearClientTraders() {}
	
	public void updateTrader(CompoundNBT compound) {}
	
	public void removeTrader(long traderID) {}
	
	public void initializeTeams(CompoundNBT compound) {}
	
	public void updateTeam(CompoundNBT compound) {}
	
	public void removeTeam(long teamID) {}
	
	public void initializeBankAccounts(CompoundNBT compound) {}
	
	public void updateBankAccount(CompoundNBT compound) {}
	
	public void receiveEmergencyEjectionData(CompoundNBT compound) {}
	
	public void updateNotifications(NotificationData data) {}
	
	public void receiveNotification(Notification notification) {}
	
	public void receiveSelectedBankAccount(AccountReference selectedAccount) {}
	
	public void openTerminalScreen() {}
	
	public void openNotificationScreen() {}
	
	public void openTeamManager() {}
	
	public void playCoinSound() {}
	
	public void createTeamResponse(long teamID) {}
	
	public long getTimeDesync() { return 0; }
	
	public void setTimeDesync(long currentTime) { }
	
	public void loadAdminPlayers(List<UUID> serverAdminList) { }

	@Nonnull
	public World safeGetDummyLevel() throws Exception{
		World level = this.getDummyLevelFromServer();
		if(level != null)
			return level;
		throw new Exception("Could not get dummy level from server, as there is no active server!");
	}

	@Nullable
	protected final World getDummyLevelFromServer() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
			return server.overworld();
		return null;
	}

	public void loadPlayerTrade(ClientPlayerTrade trade) { }

}