package io.github.lightman314.lightmanscurrency.common.teams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.data.ClientTeamData;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.data.MessageRemoveClientTeam;
import io.github.lightman314.lightmanscurrency.network.message.data.MessageUpdateClientTeam;
import io.github.lightman314.lightmanscurrency.network.message.teams.MessageInitializeClientTeams;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID)
public class TeamSaveData extends WorldSavedData {

	private long nextID = 0;
	private long getNextID() {
		long id = this.nextID;
		this.nextID++;
		this.setDirty();
		return id;
	}
	private final Map<Long, Team> teams = new HashMap<>();
	
	
	private TeamSaveData() { super("lightmanscurrency_team_data"); }
	public void load(CompoundNBT compound) {
		
		this.nextID = compound.getLong("NextID");
		
		ListNBT teamList = compound.getList("Teams", Constants.NBT.TAG_COMPOUND);
		for(int i = 0; i < teamList.size(); ++i)
		{
			Team team = Team.load(teamList.getCompound(i));
			if(team != null)
				this.teams.put(team.getID(), team);
		}
		
	}
	
	@Nonnull
	public CompoundNBT save(CompoundNBT compound) {
		
		compound.putLong("NextID", this.nextID);

		ListNBT teamList = new ListNBT();
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
			ServerWorld level = server.overworld();
			if(level != null)
				return level.getDataStorage().computeIfAbsent(TeamSaveData::new, "lightmanscurrency_team_data");
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
				CompoundNBT compound = team.save();
				LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageUpdateClientTeam(compound));
			}
		}
	}


	/** @deprecated Only use to copy team over from former Trading Office. */
	@Deprecated
	public static Team RegisterOldTeam(Team team) {
		TeamSaveData tsd = get();
		if(tsd != null)
		{
			long teamID = tsd.getNextID();
			team.overrideID(teamID);
			tsd.teams.put(teamID, team);
			
			MarkTeamDirty(teamID);
			return team;
		}
		return null;
	}
	
	public static Team RegisterTeam(PlayerEntity owner, String teamName)
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
				LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageRemoveClientTeam(teamID));
			}
		}
	}
	
	@SubscribeEvent
	public static void OnPlayerLogin(PlayerLoggedInEvent event)
	{
		
		PacketDistributor.PacketTarget target = LightmansCurrencyPacketHandler.getTarget(event.getPlayer());
		TeamSaveData tsd = get();
		
		CompoundNBT compound = new CompoundNBT();
		ListNBT teamList = new ListNBT();
		tsd.teams.forEach((id, team) -> teamList.add(team.save()));
		compound.put("Teams", teamList);
		LightmansCurrencyPacketHandler.instance.send(target, new MessageInitializeClientTeams(compound));
		
	}
	
	
}