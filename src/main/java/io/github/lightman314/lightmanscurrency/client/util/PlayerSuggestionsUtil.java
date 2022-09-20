package io.github.lightman314.lightmanscurrency.client.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.player.CPacketRequestPlayerList;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedInEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class PlayerSuggestionsUtil {
	
	private static Timer timer = new Timer();
	public static final long PLAYER_LIST_REQUEST_DELAY = TimeUtil.DURATION_MINUTE * 5;
	
	public static List<String> getSuggestions(String search) { return getSuggestions(search, new ArrayList<>()); }
	
	public static List<String> getSuggestions(String search, List<PlayerReference> ignoreEntries) { return getSuggestions(search, PlayerReference.getKnownPlayers(), ignoreEntries); }
	
	public static List<String> getSuggestions(String search, List<PlayerReference> options, List<PlayerReference> ignoreEntries) {
		Objects.requireNonNull(search);
		Objects.requireNonNull(options);
		Objects.requireNonNull(ignoreEntries);
		List<String> suggestions = new ArrayList<>();
		if(search.isBlank())
			return suggestions;
		for(PlayerReference player : options)
		{
			if(player.lastKnownName().toLowerCase().startsWith(search.toLowerCase()))
			{
				if(ignoreEntries.stream().noneMatch(ignored -> player.is(ignored)))
					suggestions.add(player.lastKnownName());
			}
		}
		return suggestions;
	}
	
	public static void loadFromServerData(CompoundTag serverData) {
		
		if(serverData.contains("KnownPlayers", Tag.TAG_LIST))
		{
			
			//Clear the player cache
			PlayerReference.clearPlayerCache();
			
			ListTag knownPlayerList = serverData.getList("KnownPlayers", Tag.TAG_COMPOUND);
			for(int i = 0; i < knownPlayerList.size(); ++i)
			{
				//Load the player (will automatically add it to the PlayerReference cache)
				PlayerReference.load(knownPlayerList.getCompound(i));
			}
		}
		
	}
	
	@SubscribeEvent
	public static void onLogin(LoggedInEvent event) {
		try {
			timer = new Timer();
			TimerTask task = new RequestPlayerListTask();
			timer.scheduleAtFixedRate(task, PLAYER_LIST_REQUEST_DELAY, PLAYER_LIST_REQUEST_DELAY);
			task.run();
		} catch(Throwable t) {}
	}
	
	@SubscribeEvent
	public static void onLogout(LoggedOutEvent event) {
		try {
			timer.cancel();
			timer = null;
		} catch(Throwable t) {}
	}
	
	private static class RequestPlayerListTask extends TimerTask
	{
		@Override
		public void run() { LightmansCurrencyPacketHandler.instance.sendToServer(new CPacketRequestPlayerList()); }
	}
	
}
