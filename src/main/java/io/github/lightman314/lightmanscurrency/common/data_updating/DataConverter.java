package io.github.lightman314.lightmanscurrency.common.data_updating;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.bank.BankSaveData;
import io.github.lightman314.lightmanscurrency.common.data_updating.events.ConvertUniversalTraderEvent;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionSaveData;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationSaveData;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

//Replaces the old TradingOffice class, and is used to convert & distribute the old TradingOffice data into it's new locations & types.
@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID)
@SuppressWarnings("deprecation")
public class DataConverter extends SavedData {

	private static final String DATA_NAME = "lightmanscurrency_trading_office";
	
	private static boolean loading = false;
	
	private static final Map<UUID,Long> newTraderIDs = new HashMap<>();
	private static final Map<UUID,Long> newTeamIDs = new HashMap<>();
	
	private DataConverter() { newTraderIDs.clear(); newTeamIDs.clear(); }
	private DataConverter(CompoundTag compound) {
		this();
		loading = true;
		//OLD DATA CONVERSION
		if(compound.contains("Teams", Tag.TAG_LIST))
		{
			//Set as dirty so that it will forcibly save and won't try to duplicate the teams
			this.setDirty();
			ListTag teamList = compound.getList("Teams", Tag.TAG_COMPOUND);
			for(int i = 0; i < teamList.size(); ++i)
			{
				CompoundTag tag = teamList.getCompound(i);
				Team team = Team.load(tag);
				if(team != null)
				{
					team = TeamSaveData.RegisterOldTeam(team);
					if(team != null)
					{
						if(tag.contains("id"))
						{
							UUID oldID = tag.getUUID("id");
							long newID = team.getID();
							newTeamIDs.put(oldID, newID);
							LightmansCurrency.LogInfo("Successfully transferred team to the new team data.\nOldID: " + oldID + "\nNew ID: " + newID);
						}
						else
							LightmansCurrency.LogInfo("Successfully transferred team to the new team data.\nNew ID: " + team.getID());
					}
				}
			}
		}
		
		//Do traders after teams so that the traders owned by teams can get their new team ids
		if(compound.contains("UniversalTraders", Tag.TAG_LIST))
		{
			//Set as dirty so that it will forcibly save and won't try to duplicate the traders
			this.setDirty();
			ListTag universalTraderDataList = compound.getList("UniversalTraders", Tag.TAG_COMPOUND);
			for(int i = 0; i < universalTraderDataList.size(); ++i)
			{
				CompoundTag traderTag = universalTraderDataList.getCompound(i);
				TraderData trader = this.convertTraderData(traderTag);
				if(trader != null)
				{
					trader.setAlwaysShowOnTerminal();
					TraderSaveData.RegisterOldTrader(trader);
					if(traderTag.contains("ID"))
					{
						UUID oldID = traderTag.getUUID("ID");
						long newID = trader.getID();
						newTraderIDs.put(oldID, newID);
						LightmansCurrency.LogInfo("Successfully converted universal trader tag into TraderData.\nOld ID: " + oldID.toString() + "\nNew ID: " + newID + "\nTrader Type: " + trader.type);
					}
					else
					{
						LightmansCurrency.LogWarning("Successfully converted universal trader tag into TraderData, but I could not extract the traders old UUID from the tag.");
					}
				}
				else
				{
					LightmansCurrency.LogError("Could not convert universal trader tag into TraderData.\n" + traderTag.getAsString());
				}
			}
		}
		
		if(compound.contains("BankAccounts", Tag.TAG_LIST))
		{
			ListTag bankAccountList = compound.getList("BankAccounts", Tag.TAG_COMPOUND);
			for(int i = 0; i < bankAccountList.size(); ++i)
			{
				CompoundTag accountCompound = bankAccountList.getCompound(i);
				try {
					UUID owner = accountCompound.getUUID("Player");
					BankAccount bankAccount = new BankAccount(() -> BankSaveData.MarkBankAccountDirty(owner), accountCompound);
					if(owner != null && bankAccount != null)
					{
						//Generate notification consumer
						bankAccount.setNotificationConsumer(BankAccount.generateNotificationAcceptor(owner));
						BankSaveData.GiveOldBankAccount(owner, bankAccount);
						LightmansCurrency.LogInfo("Successfully transferred " + bankAccount.getOwnersName() + "'s bank account to the new bank data.");
					}
				} catch(Exception e) { e.printStackTrace(); }
			}
		}
		
		if(compound.contains("PersistentTraderIDs", Tag.TAG_LIST))
		{
			ListTag persistentIDs = compound.getList("PersistentTraderIDs", Tag.TAG_COMPOUND);
			for(int i = 0; i < persistentIDs.size(); ++i)
			{
				try {
					CompoundTag idData = persistentIDs.getCompound(i);
					UUID uuid = idData.getUUID("UUID");
					String traderID = idData.getString("TraderID");
					if(uuid != null && traderID != null)
					{
						long newID = TraderSaveData.CheckOldPersistentID(traderID);
						if(newID >= 0)
							newTraderIDs.put(uuid, newID);
					}
				} catch(Exception e) { e.printStackTrace(); }
			}
		}
		
		//Run this after the persistent trader ids so that we don't need to worry about overlapping id, etc.
		if(compound.contains("PersistentTraderData", Tag.TAG_LIST))
		{
			ListTag persistentData = compound.getList("PersistentTraderData", Tag.TAG_COMPOUND);
			for(int i = 0; i < persistentData.size(); ++i)
			{
				CompoundTag thisData = persistentData.getCompound(i);
				String traderID = thisData.getString("traderID");
				TraderSaveData.GiveOldPersistentTag(traderID, thisData);
				LightmansCurrency.LogInfo("Successfully transferred Persistent Trader '" + traderID + "' persistent data to the new trader data.");
			}
		}
		
		if(compound.contains("PlayerNotifications", Tag.TAG_LIST))
		{
			ListTag notificationData = compound.getList("PlayerNotifications", Tag.TAG_COMPOUND);
			for(int i = 0; i < notificationData.size(); ++i)
			{
				CompoundTag notData = notificationData.getCompound(i);
				if(notData.contains("Player"))
				{
					UUID playerID = notData.getUUID("Player");
					NotificationData data = NotificationData.loadFrom(notData);
					if(playerID != null && data != null)
					{
						NotificationSaveData.GiveOldNotificationData(playerID, data);
						LightmansCurrency.LogInfo("Successfully transferred the notifications for Player with ID '" + playerID + "' to the new notification data.");
					}
				}
			}
		}
		
		if(compound.contains("LastSelectedBankAccounts", Tag.TAG_LIST))
		{
			ListTag selectedAccounts = compound.getList("LastSelectedBankAccounts", Tag.TAG_COMPOUND);
			for(int i = 0; i < selectedAccounts.size(); ++i)
			{
				CompoundTag accountData = selectedAccounts.getCompound(i);
				if(accountData.contains("Player"))
				{
					UUID playerID = accountData.getUUID("Player");
					AccountReference account = BankAccount.LoadReference(false, accountData);
					if(playerID != null && account != null)
					{
						BankSaveData.GiveOldSelectedBankAccount(playerID, account);
					}
				}
			}
		}
		
		if(compound.contains("EmergencyEjectionData", Tag.TAG_LIST))
		{
			ListTag ejectionData = compound.getList("EmergencyEjectionData", Tag.TAG_COMPOUND);
			int successCount = 0;
			for(int i = 0; i < ejectionData.size(); ++i)
			{
				try {
					EjectionData data = EjectionData.loadData(ejectionData.getCompound(i));
					if(data != null)
					{
						EjectionSaveData.GiveOldEjectionData(data);
						successCount++;
					}
						
				} catch(Throwable t) {}
			}
			if(ejectionData.size() > 0)
			{
				if(successCount < ejectionData.size())
					LightmansCurrency.LogInfo("Only succeeded in transferring " + successCount + "/" + ejectionData.size() + " ejection data entries to the new ejection data.");
				else
					LightmansCurrency.LogInfo("Successfully transferring all " + successCount + " ejection data entries to the new ejection data.");
			}
			else
				LightmansCurrency.LogInfo("No ejectionData needed to be transferred.");
		}
		//END OF OLD DATA
		
		
		//NEW DATA LOADING
		if(compound.contains("NewTraderIDs", Tag.TAG_LIST))
		{
			ListTag traderIDList = compound.getList("NewTraderIDs", Tag.TAG_COMPOUND);
			for(int i = 0; i < traderIDList.size(); ++i)
			{
				CompoundTag entry = traderIDList.getCompound(i);
				UUID uuid = entry.getUUID("UUID");
				long id = entry.getLong("ID");
				if(uuid != null)
					newTraderIDs.put(uuid, id);
			}
		}
		
		if(compound.contains("NewTeamIDs", Tag.TAG_LIST))
		{
			ListTag teamIDList = compound.getList("NewTeamIDs", Tag.TAG_COMPOUND);
			for(int i = 0; i < teamIDList.size(); ++i)
			{
				CompoundTag entry = teamIDList.getCompound(i);
				UUID uuid = entry.getUUID("UUID");
				long id = entry.getLong("ID");
				if(uuid != null)
					newTeamIDs.put(uuid, id);
			}
		}
		
		loading = false;
		
	}
	
	@Override
	public CompoundTag save(CompoundTag compound) {
		
		ListTag traderIDList = new ListTag();
		newTraderIDs.forEach((uuid,id) -> {
			CompoundTag entry = new CompoundTag();
			entry.putUUID("UUID", uuid);
			entry.putLong("ID", id);
			traderIDList.add(entry);
		});
		compound.put("NewTraderIDs", traderIDList);
		
		ListTag teamIDList = new ListTag();
		newTeamIDs.forEach((uuid,id) -> {
			CompoundTag entry = new CompoundTag();
			entry.putUUID("UUID", uuid);
			entry.putLong("ID", id);
			teamIDList.add(entry);
		});
		compound.put("NewTeamIDs", teamIDList);
		
		return compound;
	}
	
	private static DataConverter get() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			ServerLevel level = server.getLevel(Level.OVERWORLD);
			if(level != null)
				return level.getDataStorage().computeIfAbsent(DataConverter::new, DataConverter::new, DATA_NAME);
		}
		return null;
	}
	
	private TraderData convertTraderData(CompoundTag oldTraderData) {
		try {
			ConvertUniversalTraderEvent event = new ConvertUniversalTraderEvent(oldTraderData);
			MinecraftForge.EVENT_BUS.post(event);
			return event.getTrader();
		} catch(Throwable t) { LightmansCurrency.LogError("Error converting trader data.", t); return null; }
	}
	
	public static long getNewTraderID(UUID traderID) {
		if(!loading) //Force it to be loaded if it's not already in the middle of loading
			get();
		return newTraderIDs.getOrDefault(traderID, -1l);
	}
	
	public static long getNewTeamID(UUID teamID) {
		if(!loading) //Force it to be loaded if it's not already in the middle of loading
			get();
		return newTeamIDs.getOrDefault(teamID, -1l);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		//Forcibly load the data converter so that it'll forcibly send data to the other save data if relevant
		if(!loading)
			get();
	}
	
}
