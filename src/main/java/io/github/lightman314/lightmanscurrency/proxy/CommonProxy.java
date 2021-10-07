package io.github.lightman314.lightmanscurrency.proxy;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public class CommonProxy {
	
	public void setupClient() {}
	
	public void updateTraders(CompoundNBT compound) {}
	
	public void openTerminalScreen(PlayerEntity player) {}
	
	public long getTimeDesync() { return 0; }
	
	public void setTimeDesync(long currentTime) { }
	
	public void loadAdminPlayers(List<UUID> serverAdminList) { }
	
}
