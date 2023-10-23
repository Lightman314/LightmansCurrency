package io.github.lightman314.lightmanscurrency.common.teams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.data.ClientTeamData;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.data.SPacketRemoveClientTeam;
import io.github.lightman314.lightmanscurrency.network.message.data.SPacketUpdateClientTeam;
import io.github.lightman314.lightmanscurrency.network.message.teams.SPacketClearClientTeams;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor.PacketTarget;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID)
public class TeamSaveData extends SavedData {

	private long nextID = 0;
	private long getNextID() {
		long id = this.nextID;
		this.nextID++;
		this.setDirty();
		return id;
	}
	private final Map<Long, Team> teams = new HashMap<>();


	private TeamSaveData() {}
	private TeamSaveData(CompoundTag compound) {

		this.nextID = compound.getLong("NextID");

		ListTag teamList = compound.getList("Teams", Tag.TAG_COMPOUND);
		for(int i = 0; i < teamList.size(); ++i)
		{
			Team team = Team.load(teamList.getCompound(i));
			if(team != null)
				this.teams.put(team.getID(), team);
		}

	}

	@Nonnull
	public CompoundTag save(CompoundTag compound) {

		compound.putLong("NextID", this.nextID);

		ListTag teamList = new ListTag();
		this.teams.forEach((teamID, team) ->{
			if(team != null)
				teamList.add(team.save());
		});
		compound.put("Teams", teamList);

		return compound;
	}

	private static TeamSaveData get() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			ServerLevel level = server.getLevel(Level.OVERWORLD);
			if(level != null)
				return level.getDataStorage().computeIfAbsent(TeamSaveData::new, TeamSaveData::new, "lightmanscurrency_team_data");
		}
		return null;
	}

	public static List<Team> GetAllTeams(boolean isClient)
	{
		if(isClient)
		{
			return ClientTeamData.GetAllTeams();
		}
		else
		{
			TeamSaveData tsd = get();
			if(tsd != null)
				return new ArrayList<>(tsd.teams.values());
			return new ArrayList<>();
		}
	}

	public static Team GetTeam(boolean isClient, long teamID)
	{
		if(isClient)
		{
			return ClientTeamData.GetTeam(teamID);
		}
		else
		{
			TeamSaveData tsd = get();
			if(tsd != null)
			{
				if(tsd.teams.containsKey(teamID))
					return tsd.teams.get(teamID);
			}
			return null;
		}
	}

	public static void MarkTeamDirty(long teamID)
	{
		TeamSaveData tsd = get();
		if(tsd != null)
		{
			tsd.setDirty();
			//Send update packet to all connected clients
			Team team = GetTeam(false, teamID);
			if(team != null)
			{
				CompoundTag compound = team.save();
				new SPacketUpdateClientTeam(compound).sendToAll();
			}
		}
	}

	public static Team RegisterTeam(Player owner, String teamName)
	{
		TeamSaveData tsd = get();
		if(tsd != null)
		{
			long teamID = tsd.getNextID();
			Team newTeam = Team.of(teamID, PlayerReference.of(owner), teamName);
			tsd.teams.put(teamID, newTeam);

			MarkTeamDirty(teamID);

			return newTeam;
		}
		return null;
	}

	public static void RemoveTeam(long teamID)
	{
		TeamSaveData tsd = get();
		if(tsd != null)
		{
			if(tsd.teams.containsKey(teamID))
			{
				tsd.teams.remove(teamID);
				tsd.setDirty();

				//Send update packet to the connected clients
				new SPacketRemoveClientTeam(teamID).sendToAll();
			}
		}
	}

	@SubscribeEvent
	public static void OnPlayerLogin(PlayerLoggedInEvent event)
	{

		PacketTarget target = LightmansCurrencyPacketHandler.getTarget(event.getEntity());
		TeamSaveData tsd = get();

		SPacketClearClientTeams.INSTANCE.sendToTarget(target);

		tsd.teams.forEach((id, team) -> new SPacketUpdateClientTeam(team.save()).sendToTarget(target));

	}

}