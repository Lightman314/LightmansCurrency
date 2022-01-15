package io.github.lightman314.lightmanscurrency.trader.settings;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.SettingsLogger;
import io.github.lightman314.lightmanscurrency.client.gui.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.settings.core.*;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.Team.TeamReference;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.permissions.PermissionsList;
import io.github.lightman314.lightmanscurrency.trader.permissions.options.BooleanPermission;
import io.github.lightman314.lightmanscurrency.trader.permissions.options.PermissionOption;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

public class CoreTraderSettings extends Settings{

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID,"trader_core");
	
	private static final String UPDATE_CUSTOM_NAME = "customName";
	private static final String UPDATE_ADD_ALLY = "addAlly";
	private static final String UPDATE_REMOVE_ALLY = "removeAlly";
	//private static final String UPDATE_ADD_CUSTOM_PERM = "addCustomPerm";
	//private static final String UPDATE_REMOVE_CUSTOM_PERM = "removeCustomPerm";
	private static final String UPDATE_ALLY_PERMISSIONS = "allyPermissions";
	private static final String UPDATE_CUSTOM_PERMISSIONS = "customPermissions";
	private static final String UPDATE_CREATIVE = "creative";
	private static final String UPDATE_OWNERSHIP = "transferOwnership";
	private static final String UPDATE_TEAM = "changeTeam";
	
	public static PermissionsList getAllyDefaultPermissions(@Nonnull ITrader trader)
	{
		Map<String,Integer> defaultPermissions = Maps.newHashMap();
		defaultPermissions.put(Permissions.OPEN_STORAGE, 1);
		defaultPermissions.put(Permissions.EDIT_TRADES, 1);
		defaultPermissions.put(Permissions.EDIT_TRADE_RULES, 1);
		defaultPermissions.put(Permissions.EDIT_SETTINGS, 1);
		defaultPermissions.put(Permissions.CHANGE_NAME, 1);
		
		try {
		trader.getAllyDefaultPermissions().forEach((key,value) -> defaultPermissions.put(key, value));
		} catch(Exception e) { LightmansCurrency.LogError("Error getting additional default ally permissions for trader type " + trader.getClass().getName(), e); }
		
		return new PermissionsList(trader, UPDATE_ALLY_PERMISSIONS, defaultPermissions);
	}
	
	//Owner
	PlayerReference owner = null;
	//Team
	TeamReference team = null;
	public Team getTeam()
	{
		if(this.team != null)
			return this.team.getTeam(this.trader.isClient());
		return null;
	}
	//Ally Permissions
	List<PlayerReference> allies = Lists.newArrayList();
	PermissionsList allyPermissions = getAllyDefaultPermissions(this.trader);
	public PermissionsList getAllyPermissions() { return this.allyPermissions; }
	//Custom Permissions
	Map<PlayerReference,PermissionsList> customPermissions = Maps.newHashMap();

	//Custom Name
	String customName = "";
	public boolean hasCustomName() { return !this.customName.isEmpty(); }
	public String getCustomName() { return this.customName; }
	public CompoundNBT setCustomName(PlayerEntity requestor, String newName)
	{
		if(!this.hasPermission(requestor, Permissions.CHANGE_NAME))
		{
			PermissionWarning(requestor, "change the traders name", Permissions.CHANGE_NAME);
			return null;
		}
		if(!this.customName.contentEquals(newName))
		{
			String oldName = this.customName;
			this.customName = newName;
			this.logger.LogNameChange(requestor, oldName, this.customName);
			this.markDirty();
			CompoundNBT updateInfo = initUpdateInfo(UPDATE_CUSTOM_NAME);
			updateInfo.putString("NewName", this.customName);
			return updateInfo;
		}
		return null;
	}
	
	//Creative Mode
	boolean isCreative = false;
	
	SettingsLogger logger = new SettingsLogger();
	
	public CoreTraderSettings(ITrader trader, IMarkDirty marker, BiConsumer<ResourceLocation,CompoundNBT> sendToServer) { super(trader, marker, sendToServer, TYPE); }
	
	public PlayerReference getOwner() { return this.owner; }
	/**
	 * Returns the display name of the traders owner.
	 * If the trader is owned by a team (WIP), it will display the team name instead.
	 */
	public String getOwnerName()
	{
		if(this.getTeam() != null)
			return this.getTeam().getName();
		if(this.owner != null)
			return this.owner.lastKnownName();
		return "";
	}
	
	/**
	 * Initialized the traders owner.
	 * Does nothing if the owner is already defined.
	 * Does not flag it as dirty, as it is assumed to be run during a traders initialization.
	 */
	public void initializeOwner(PlayerReference owner)
	{
		if(this.owner == null)
			this.owner = owner;
		else
			LightmansCurrency.LogWarning("Attempted to initialize the owner for a trader that is already owned.");
	}
	
	public CompoundNBT setOwner(PlayerEntity requestor, String newOwnerName){
		if(!this.hasPermission(requestor, Permissions.TRANSFER_OWNERSHIP))
		{
			PermissionWarning(requestor, "transfer ownership", Permissions.TRANSFER_OWNERSHIP);
			return null;
		}
		else if(this.owner == null)
		{
			Team oldTeam = this.getTeam();
			if(oldTeam != null)
				this.team = null;
			this.owner = PlayerReference.of(newOwnerName);
			if(this.owner != null && oldTeam != null)
				this.logger.LogTeamChange(requestor, this.owner, oldTeam, null);
			
			CompoundNBT updateInfo = initUpdateInfo(UPDATE_OWNERSHIP);
			updateInfo.putString("newOwner", newOwnerName);
			return updateInfo;
		}
		else
		{
			PlayerReference newOwner = PlayerReference.of(newOwnerName);
			if(newOwner != null && (!this.owner.is(newOwner) || this.team != null))
			{
				//Reset the team, if setting the traders owner to a player
				Team oldTeam = this.getTeam();
				if(oldTeam != null)
					this.team = null;
				PlayerReference oldOwner = this.owner;
				this.owner = newOwner;
				if(oldTeam == null)
					this.logger.LogOwnerChange(requestor, oldOwner, this.owner);
				else
					this.logger.LogTeamChange(requestor, this.owner, oldTeam, null);
				
				CompoundNBT updateInfo = initUpdateInfo(UPDATE_OWNERSHIP);
				updateInfo.putString("newOwner", newOwnerName);
				return updateInfo;
			}
		}
		return null;
	}
	
	public CompoundNBT setTeam(PlayerEntity requestor, @Nullable UUID newTeamID)
	{
		if(!this.hasPermission(requestor, Permissions.TRANSFER_OWNERSHIP))
		{
			PermissionWarning(requestor, "transfer team ownership", Permissions.TRANSFER_OWNERSHIP);
			return null;
		}
		
		Team oldTeam = null;
		if(this.team != null)
		{
			//Abort if that team is already selected
			if(this.team.getID().equals(newTeamID))
				return null;
			oldTeam = this.getTeam();
		}
		
		this.team = Team.referenceOf(newTeamID);
		
		Team newTeam = this.getTeam();
		
		this.logger.LogTeamChange(requestor, this.owner, oldTeam, newTeam);
		
		CompoundNBT updateInfo = initUpdateInfo(UPDATE_TEAM);
		if(newTeamID != null)
			updateInfo.putUniqueId("Team", newTeamID);
		return updateInfo;
	}
	
	public List<PlayerReference> getAllies() { return this.allies; }
	public CompoundNBT addAlly(PlayerEntity requestor, String newAllyName)
	{
		if(!this.hasPermission(requestor, Permissions.ADD_REMOVE_ALLIES))
		{
			PermissionWarning(requestor, "add an ally", Permissions.ADD_REMOVE_ALLIES);
			return null;
		}
		if(!PlayerReference.listContains(this.allies, newAllyName))
		{
			PlayerReference newAlly = PlayerReference.of(newAllyName);
			if(newAlly != null)
			{
				this.allies.add(newAlly);
				this.logger.LogAllyChange(requestor, newAlly, true);
			}
			if(newAlly != null || requestor.world.isRemote)
			{
				CompoundNBT updateInfo = initUpdateInfo(UPDATE_ADD_ALLY);
				updateInfo.putString("AllyName", newAllyName);
				return updateInfo;
			}
		}
		return null;
	}
	public CompoundNBT removeAlly(PlayerEntity requestor, String removedAllyName)
	{
		if(!this.hasPermission(requestor, Permissions.ADD_REMOVE_ALLIES))
		{
			PermissionWarning(requestor, "remove an ally", Permissions.ADD_REMOVE_ALLIES);
			return null;
		}
		if(PlayerReference.listContains(this.allies, removedAllyName))
		{
			PlayerReference removedAlly = null;
			for(int i = 0; i < this.allies.size() && removedAlly == null; ++i)
			{
				if(this.allies.get(i).is(removedAllyName))
				{
					removedAlly = this.allies.get(i);
					this.allies.remove(i);
				}
			}
			if(removedAlly == null)
				return null;
			this.logger.LogAllyChange(requestor, removedAlly, false);
			CompoundNBT updateInfo = initUpdateInfo(UPDATE_REMOVE_ALLY);
			updateInfo.putString("AllyName", removedAllyName);
			return updateInfo;
		}
		return null;
	}
	
	/**
	 * Updates the last known name of all matching player references in the owner/ally/custom permissions list.
	 * @param player
	 */
	public void updateNames(PlayerEntity player)
	{
		if(this.owner != null)
			this.owner.tryUpdateName(player);
		for(PlayerReference ally : this.allies)
			ally.tryUpdateName(player);
		this.customPermissions.forEach((p,permission) ->{
			p.tryUpdateName(player);
		});
	}
	
	public PermissionsList addCustomPermissionEntry(PlayerEntity requestor, PlayerReference player)
	{
		if(!this.hasPermission(requestor, Permissions.EDIT_PERMISSIONS))
		{
			PermissionWarning(requestor, "add custom permission", Permissions.EDIT_PERMISSIONS);
			return null;
		}
		if(this.customPermissions.containsKey(player))
			return this.customPermissions.get(player);
		return this.customPermissions.put(player, new PermissionsList(this.trader, UPDATE_CUSTOM_PERMISSIONS));
	}
	
	public void removeCustomPermissionEntry(PlayerReference player)
	{
		this.customPermissions.remove(player);
	}
	
	public boolean hasPermission(PlayerEntity player, String permission)
	{
		if(player == null)
			return true;
		return getPermissionLevel(player, permission) > 0;
	}
	
	public int getPermissionLevel(PlayerEntity player, String permission)
	{
		if(player == null || (this.owner != null && this.owner.is(player)) || TradingOffice.isAdminPlayer(player))
			return Integer.MAX_VALUE;
		AtomicInteger level = new AtomicInteger(0);
		if(PlayerReference.listContains(this.allies, player.getUniqueID()))
			level.set(this.allyPermissions.getLevel(permission));
		this.customPermissions.forEach((playerRef,permissions) ->{
			if(playerRef.is(player))
			{
				int l = permissions.getLevel(permission);
				if(l > level.get())
					level.set(l);
			}
		});
		if(this.getTeam() != null)
		{
			if(this.getTeam().isAdmin(player))
				return Integer.MAX_VALUE;
			else if(this.getTeam().isMember(player))
			{
				int l = this.allyPermissions.getLevel(permission);
				if(l > level.get())
					level.set(l);
			}
		}
		return level.get();
	}
	
	public boolean isCreative() { return this.isCreative; }
	public CompoundNBT toggleCreative(PlayerEntity requestor)
	{
		if(!TradingOffice.isAdminPlayer(requestor))
		{
			PermissionWarning(requestor, "toggle creative mode", Permissions.ADMIN_MODE);
			return null;
		}
		this.isCreative = !this.isCreative;
		this.logger.LogCreativeToggle(requestor, this.isCreative);
		this.markDirty();
		CompoundNBT updateInfo = initUpdateInfo(UPDATE_CREATIVE);
		updateInfo.putBoolean("isCreative", this.isCreative);
		return updateInfo;
	}
	
	public SettingsLogger getLogger() { return this.logger; }
	
	@Override
	public void changeSetting(PlayerEntity requestor, CompoundNBT updateInfo) {
		if(this.isUpdateType(updateInfo, UPDATE_CUSTOM_NAME))
		{
			String newName = updateInfo.getString("NewName");
			CompoundNBT result = this.setCustomName(requestor, newName);
			if(result != null)
				this.markDirty();
			LightmansCurrency.LogInfo("Custom Name changed to '" + newName + "' on the server.");
		}
		else if(this.isUpdateType(updateInfo, UPDATE_ADD_ALLY))
		{
			String allyName = updateInfo.getString("AllyName");
			CompoundNBT result = this.addAlly(requestor, allyName);
			if(result != null)
				this.markDirty();
			LightmansCurrency.LogInfo("Attempted to add '" + allyName + "' as an ally.");
		}
		else if(this.isUpdateType(updateInfo, UPDATE_REMOVE_ALLY))
		{
			String allyName = updateInfo.getString("AllyName");
			CompoundNBT result = this.removeAlly(requestor, allyName);
			if(result != null)
				this.markDirty();
			LightmansCurrency.LogInfo("Attempted to remove '" + allyName + "' from the ally list.");
		}
		else if(this.isUpdateType(updateInfo, UPDATE_CREATIVE))
		{
			boolean nowCreative = updateInfo.getBoolean("isCreative");
			if(nowCreative != this.isCreative)
			{
				CompoundNBT result = this.toggleCreative(requestor);
				if(result != null)
					this.markDirty();
			}
		}
		else if(this.isUpdateType(updateInfo, UPDATE_ALLY_PERMISSIONS))
		{
			if(this.allyPermissions.changeLevel(requestor, updateInfo))
				this.markDirty();
		}
		else if(this.isUpdateType(updateInfo, UPDATE_OWNERSHIP))
		{
			String newOwnerName = updateInfo.getString("newOwner");
			CompoundNBT result = this.setOwner(requestor, newOwnerName);
			if(result != null)
				this.markDirty();
		}
		else if(this.isUpdateType(updateInfo, UPDATE_TEAM))
		{
			UUID newTeamID = null;
			if(updateInfo.contains("Team"))
				newTeamID = updateInfo.getUniqueId("Team");
			CompoundNBT result = this.setTeam(requestor, newTeamID);
			if(result != null)
				this.markDirty();
		}
	}
	
	public CompoundNBT save(CompoundNBT compound)
	{
		
		this.saveOwner(compound);
		this.saveTeam(compound);
		this.saveAllyList(compound);
		this.saveAllyPermissions(compound);
		this.saveCustomPermissions(compound);
		this.saveCustomName(compound);
		this.saveCreative(compound);
		this.saveLogger(compound);
		
		return compound;
	}
	
	public CompoundNBT saveOwner(CompoundNBT compound)
	{
		if(this.owner != null)
			compound.put("Owner", this.owner.save());
		return compound;
	}
	
	public CompoundNBT saveTeam(CompoundNBT compound)
	{
		if(this.team != null)
			compound.putUniqueId("Team", this.team.getID());
		else
			compound.putBoolean("Team", false);
		return compound;
	}
	
	public CompoundNBT saveAllyList(CompoundNBT compound)
	{
		PlayerReference.saveList(compound, this.allies, "Allies");
		return compound;
	}
	
	public CompoundNBT saveAllyPermissions(CompoundNBT compound)
	{
		this.allyPermissions.save(compound, "AllyPermissions");
		return compound;
	}
	
	public CompoundNBT saveCustomPermissions(CompoundNBT compound)
	{
		//Custom Permissions
		ListNBT customPermissionsList = new ListNBT();
		this.customPermissions.forEach((player,permissions)->{
			CompoundNBT thisCompound = new CompoundNBT();
			thisCompound.put("Player", player.save());
			permissions.save(compound, "Permissions");
			customPermissionsList.add(thisCompound);
		});
		compound.put("CustomPermissions", customPermissionsList);
		return compound;
	}
	
	public CompoundNBT saveCustomName(CompoundNBT compound)
	{
		compound.putString("CustomName", this.customName);
		return compound;
	}
	
	public CompoundNBT saveCreative(CompoundNBT compound)
	{
		compound.putBoolean("Creative", this.isCreative);
		return compound;
	}
	
	public CompoundNBT saveLogger(CompoundNBT compound)
	{
		this.logger.write(compound);
		return compound;
	}
	
	public void loadFromOldUniversalData(CompoundNBT compound)
	{
		LightmansCurrency.LogInfo("Loading Core Trader Settings from old UniversalData compound.");
		//Owner
		UUID ownerID = null;
		String ownerName = "";
		if(compound.contains("OwnerID"))
			ownerID = compound.getUniqueId("OwnerID");
		if(compound.contains("OwnerName", Constants.NBT.TAG_STRING))
			ownerName = compound.getString("OwnerName");
		if(ownerID != null)
			this.owner = PlayerReference.of(ownerID, ownerName);
		
		//Creative
		if(compound.contains("Creative"))
			this.isCreative = compound.getBoolean("Creative");
		
		//Read allies
		if(compound.contains("Allies",Constants.NBT.TAG_LIST))
		{
			this.allies.clear();
			ListNBT allyList = compound.getList("Allies", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < allyList.size(); i++)
			{
				CompoundNBT thisAlly = allyList.getCompound(i);
				if(thisAlly.contains("name", Constants.NBT.TAG_STRING))
				{
					this.addAlly(null, thisAlly.getString("name"));
				}
			}
		}
		
		//TraderName
		if(compound.contains("TraderName", Constants.NBT.TAG_STRING))
			this.customName = compound.getString("TraderName");
	}
	
	public void loadFromOldTraderData(CompoundNBT compound)
	{
		LightmansCurrency.LogInfo("Loading Core Trader Settings from old TileEntity compound.");
		//Owner
		UUID ownerID = null;
		String ownerName = "";
		if(compound.contains("OwnerID"))
			ownerID = compound.getUniqueId("OwnerID");
		if(compound.contains("OwnerName", Constants.NBT.TAG_STRING))
			ownerName = compound.getString("OwnerName");
		if(ownerID != null)
			this.owner = PlayerReference.of(ownerID, ownerName);
		
		//Creative
		if(compound.contains("Creative"))
			this.isCreative = compound.getBoolean("Creative");
		
		//Custom Name
		if(compound.contains("CustomName", Constants.NBT.TAG_STRING))
			this.customName = compound.getString("CustomName");
		
		//Read Allies
		if(compound.contains("Allies",Constants.NBT.TAG_LIST))
		{
			this.allies.clear();
			ListNBT allyList = compound.getList("Allies", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < allyList.size(); i++)
			{
				CompoundNBT thisAlly = allyList.getCompound(i);
				if(thisAlly.contains("name", Constants.NBT.TAG_STRING))
				{
					this.addAlly(null, thisAlly.getString("name"));
				}
			}
		}
		
	}
	
	public void load(CompoundNBT compound)
	{
		//Owner
		if(compound.contains("Owner", Constants.NBT.TAG_COMPOUND))
			this.owner = PlayerReference.load(compound.getCompound("Owner"));
		//Team
		if(compound.contains("Team", Constants.NBT.TAG_BYTE))
			this.team = null;
		else if(compound.contains("Team"))
			this.team = Team.referenceOf(compound.getUniqueId("Team"));
		//Ally List
		if(compound.contains("Allies", Constants.NBT.TAG_LIST))
			this.allies = PlayerReference.loadList(compound, "Allies");
		//Ally Permissions
		if(compound.contains("AllyPermissions", Constants.NBT.TAG_LIST))
			this.allyPermissions = PermissionsList.load(this.trader, UPDATE_ALLY_PERMISSIONS, compound, "AllyPermissions");
		//Custom Permissions
		if(compound.contains("CustomPermissions", Constants.NBT.TAG_LIST))
		{
			this.customPermissions.clear();
			ListNBT customPermissionsList = compound.getList("CustomPermissions", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < customPermissionsList.size(); ++i)
			{
				CompoundNBT thisCompound = customPermissionsList.getCompound(i);
				PlayerReference player = PlayerReference.load(thisCompound.getCompound("Player"));
				PermissionsList permissions = PermissionsList.load(this.trader, UPDATE_CUSTOM_PERMISSIONS, thisCompound, "Permissions");
				this.customPermissions.put(player, permissions);
			}
		}
		
		//Custom Name
		if(compound.contains("CustomName", Constants.NBT.TAG_STRING))
			this.customName = compound.getString("CustomName");
			
		//Creative
		if(compound.contains("Creative"))
			this.isCreative = compound.getBoolean("Creative");
		
		//Logger
		this.logger.read(compound);
		
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public List<SettingsTab> getSettingsTabs() {
		return Lists.newArrayList(MainTab.INSTANCE, AllyTab.INSTANCE, AllyPermissionsTab.INSTANCE, OwnershipTab.INSTANCE);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public List<SettingsTab> getBackEndSettingsTabs() {
		return Lists.newArrayList(LoggerTab.INSTANCE);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public List<PermissionOption> getPermissionOptions() {
		return Lists.newArrayList(
				BooleanPermission.of(Permissions.OPEN_STORAGE),
				BooleanPermission.of(Permissions.CHANGE_NAME),
				BooleanPermission.of(Permissions.EDIT_TRADES),
				BooleanPermission.of(Permissions.COLLECT_COINS),
				BooleanPermission.of(Permissions.STORE_COINS),
				BooleanPermission.of(Permissions.EDIT_TRADE_RULES),
				BooleanPermission.of(Permissions.EDIT_SETTINGS),
				BooleanPermission.of(Permissions.ADD_REMOVE_ALLIES),
				BooleanPermission.of(Permissions.EDIT_PERMISSIONS),
				BooleanPermission.of(Permissions.CLEAR_LOGS),
				BooleanPermission.of(Permissions.BREAK_TRADER),
				BooleanPermission.of(Permissions.TRANSFER_OWNERSHIP)
			);
	}
	
}
