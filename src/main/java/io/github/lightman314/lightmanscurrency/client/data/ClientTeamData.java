package io.github.lightman314.lightmanscurrency.client.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.util.LookupHelper;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

import javax.annotation.Nullable;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientTeamData {

	private static final Map<Long,Team> loadedTeams = new HashMap<>();
	
	public static List<Team> GetAllTeams() { return ImmutableList.copyOf(loadedTeams.values()); }

	@Nullable
	public static Team GetTeam(long teamID) { return loadedTeams.get(teamID); }
	
	public static void ClearTeams() { loadedTeams.clear(); }
	
	public static void UpdateTeam(CompoundTag compound)
	{
		Team updatedTeam = Team.load(compound, LookupHelper.getRegistryAccess());
		loadedTeams.put(updatedTeam.getID(), updatedTeam.flagAsClient());
	}
	
	public static void RemoveTeam(long teamID) { loadedTeams.remove(teamID); }
	
	@SubscribeEvent
	public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) { ClearTeams(); }
}
