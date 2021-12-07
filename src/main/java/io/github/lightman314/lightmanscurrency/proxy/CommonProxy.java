package io.github.lightman314.lightmanscurrency.proxy;

import java.util.List;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class CommonProxy {
	
	public void setupClient() {}
	
	public void initializeTraders(CompoundTag compound) {}
	
	public void updateTrader(CompoundTag compound) {}
	
	public void removeTrader(UUID traderID) {}
	
	public void openTerminalScreen(Player player) {}
	
	public void openInventoryScreen(Player player) {}
	
	public long getTimeDesync() { return 0; }
	
	public void setTimeDesync(long currentTime) { }
	
	public void loadAdminPlayers(List<UUID> serverAdminList) { }
	
}
