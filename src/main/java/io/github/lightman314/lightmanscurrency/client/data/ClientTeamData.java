package io.github.lightman314.lightmanscurrency.client.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.lightman314.lightmanscurrency.common.teams.Team;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientTeamData {

	private static final Map<Long,Team> loadedTeams = new HashMap<>();
	
	public static List<Team> GetAllTeams()
	{
		return new ArrayList<>(loadedTeams.values());
	}
	
	public static Team GetTeam(long teamID)
	{
		if(loadedTeams.containsKey(teamID))
			return loadedTeams.get(teamID);
		return null;
	}
	
	public static void InitTeams(List<Team> teams)
	{
		loadedTeams.clear();
		teams.forEach(team -> loadedTeams.put(team.getID(), team));
	}
	
	public static void UpdateTeam(CompoundTag compound)
	{
		Team updatedTeam = Team.load(compound);
		loadedTeams.put(updatedTeam.getID(), updatedTeam);
	}
	
	public static void RemoveTeam(long teamID)
	{
		if(loadedTeams.containsKey(teamID))
			loadedTeams.remove(teamID);
	}
	
	@SubscribeEvent
	public static void onClientLogout(ClientPlayerNetworkEvent.LoggedOutEvent event) {
		loadedTeams.clear();
	}
}