package io.github.lightman314.lightmanscurrency.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.playertrading.ClientPlayerTrade;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CommonProxy {

	public boolean isClient() { return false; }

	public void init() {}

	public void setupClient() {}

	//Leave this one as it's used for notifications in chat
	public void receiveNotification(Notification notification) {}
	
	public void playCoinSound() {}
	
	public void createTeamResponse(long teamID) {}
	
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

	public boolean isSelf(@Nonnull Player player) { return false; }
	
}
