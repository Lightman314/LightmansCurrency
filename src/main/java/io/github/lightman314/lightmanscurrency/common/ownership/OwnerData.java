package io.github.lightman314.lightmanscurrency.common.ownership;

import java.util.function.Consumer;

import io.github.lightman314.lightmanscurrency.common.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;

public class OwnerData {

	private IFormattableTextComponent customOwner = null;
	private PlayerReference playerOwner = null;
	private long teamOwner = -1;
	
	private final IClientTracker parent;
	private final Consumer<OwnerData> onChanged;
	
	public OwnerData(IClientTracker parent, Consumer<OwnerData> onChanged) { this.parent = parent; this.onChanged = onChanged; }
	
	public boolean hasOwner() { return this.playerOwner != null || this.getTeam() != null || this.customOwner != null; }
	
	public CompoundNBT save()
	{
		CompoundNBT compound = new CompoundNBT();
		if(this.customOwner != null)
			compound.putString("Custom", ITextComponent.Serializer.toJson(this.customOwner));
		if(this.playerOwner != null)
			compound.put("Player", this.playerOwner.save());
		if(this.teamOwner >= 0)
			compound.putLong("Team", this.teamOwner);
		return compound;
	}
	
	public void load(CompoundNBT compound)
	{
		if(compound.contains("Custom"))
			this.customOwner = ITextComponent.Serializer.fromJson(compound.getString("Custom"));
		else
			this.customOwner = null;
		
		if(compound.contains("Player"))
			this.playerOwner = PlayerReference.load(compound.getCompound("Player"));
		else
			this.playerOwner = null;
		
		if(compound.contains("Team"))
			this.teamOwner = compound.getLong("Team");
		else
			this.teamOwner = -1;
	}
	
	public void copyFrom(OwnerData owner) {
		this.customOwner = owner.customOwner;
		this.playerOwner = owner.playerOwner;
		this.teamOwner = owner.teamOwner;
	}
	
	public boolean hasPlayer() { return this.playerOwner != null; }
	public PlayerReference getPlayer() { return this.playerOwner; }
	
	public boolean hasTeam() { return this.getTeam() != null; }
	public Team getTeam()
	{
		if(this.teamOwner < 0)
			return null;
		return TeamSaveData.GetTeam(this.parent.isClient(), this.teamOwner);
	}
	public PlayerReference getPlayerForContext() {
		Team team = this.getTeam();
		if(team != null)
			return team.getOwner().copyWithName(team.getName());
		return this.playerOwner;
	}
	
	public boolean isAdmin(PlayerEntity player) { return CommandLCAdmin.isAdminPlayer(player) || this.isAdmin(PlayerReference.of(player)); }
	
	public boolean isAdmin(PlayerReference player)
	{
		if(player == null)
			return false;
		Team team = this.getTeam();
		if(team != null)
			return team.isAdmin(player.id);
		return player.is(this.playerOwner);
	}
	
	public boolean isMember(PlayerEntity player) { return CommandLCAdmin.isAdminPlayer(player) || this.isMember(PlayerReference.of(player));}
	
	public boolean isMember(PlayerReference player) {
		if(player == null)
			return false;
		Team team = this.getTeam();
		if(team != null)
			return team.isMember(player.id);
		return player.is(this.playerOwner);
	}
	
	public String getOwnerName(boolean isClient)
	{
		if(this.customOwner != null)
			return this.customOwner.getString();
		Team team = this.getTeam();
		if(team != null)
			return team.getName();
		if(this.playerOwner != null)
			return this.playerOwner.getName(isClient);
		return "NULL";
	}
	
	public void SetCustomOwner(String customOwner) { this.customOwner = EasyText.literal(customOwner); }
	public void SetCustomOwner(IFormattableTextComponent customOwner) { this.customOwner = customOwner; }
	
	public void SetOwner(PlayerReference player) {
		this.playerOwner = player;
		this.teamOwner = -1;
		this.onChanged.accept(this); 
	}
	
	public void SetOwner(Team team) {
		if(team == null)
			return;
		this.teamOwner = team.getID();
		this.onChanged.accept(this); 
	}
	
}