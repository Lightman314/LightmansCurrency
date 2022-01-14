package io.github.lightman314.lightmanscurrency.proxy;

import java.util.List;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;

public class CommonProxy {
	
	public void setupClient() {}
	
	public void initializeTraders(CompoundTag compound) {}
	
	public void updateTrader(CompoundTag compound) {}
	
	public void removeTrader(UUID traderID) {}
	
	public void initializeTeams(CompoundTag compound) {}
	
	public void updateTeam(CompoundTag compound) {}
	
	public void removeTeam(UUID teamID) {}
	
	public void openTerminalScreen() {}
	
	public void openTeamManager() {}
	
	public void createTeamResponse(UUID teamID) {}
	
	public long getTimeDesync() { return 0; }
	
	public void setTimeDesync(long currentTime) { }
	
	public void loadAdminPlayers(List<UUID> serverAdminList) { }
	
}
