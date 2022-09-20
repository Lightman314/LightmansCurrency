package io.github.lightman314.lightmanscurrency.proxy;

import java.util.List;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import net.minecraft.nbt.CompoundTag;

public class CommonProxy {
	
	public void setupClient() {}
	
	public void clearClientTraders() {}
	
	public void updateTrader(CompoundTag compound) {}
	
	public void removeTrader(long traderID) {}
	
	public void initializeTeams(CompoundTag compound) {}
	
	public void updateTeam(CompoundTag compound) {}
	
	public void removeTeam(long teamID) {}
	
	public void initializeBankAccounts(CompoundTag compound) {}
	
	public void updateBankAccount(CompoundTag compound) {}
	
	public void receiveEmergencyEjectionData(CompoundTag compound) {}
	
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
	
	public void processPlayerList(CompoundTag data) { }
	
}