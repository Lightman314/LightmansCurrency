package io.github.lightman314.lightmanscurrency.common.traders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.ITraderBlock;
import io.github.lightman314.lightmanscurrency.client.gui.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.settings.core.AllyTab;
import io.github.lightman314.lightmanscurrency.client.gui.settings.core.MainTab;
import io.github.lightman314.lightmanscurrency.client.gui.settings.core.NotificationTab;
import io.github.lightman314.lightmanscurrency.client.gui.settings.core.OwnershipTab;
import io.github.lightman314.lightmanscurrency.client.gui.settings.core.PermissionsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.bank.BankSaveData;
import io.github.lightman314.lightmanscurrency.common.data_updating.DataConverter;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.IDumpable;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationSaveData;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.AddRemoveAllyNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeAllyPermissionNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeCreativeNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeNameNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeOwnerNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeSettingNotification;
import io.github.lightman314.lightmanscurrency.common.ownership.OwnerData;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext.TradeResult;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.options.BooleanPermission;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.options.PermissionOption;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue.CoinValuePair;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageSyncUsers;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageTraderMessage;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType.IUpgradeable;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class TraderData implements IClientTracker, IDumpable, IUpgradeable, ITraderSource {
	
	public static final int GLOBAL_TRADE_LIMIT = 32;
	
	private boolean canMarkDirty = false;
	public final TraderData allowMarkingDirty() { this.canMarkDirty = true; return this; }
	
	public final ResourceLocation type;
	private long id = -1;
	public long getID() { return this.id; }
	public void setID(long id) { this.id = id; }
	
	private boolean alwaysShowOnTerminal = false;
	public void setAlwaysShowOnTerminal() { this.alwaysShowOnTerminal = true; this.markDirty(this::saveShowOnTerminal); }
	public boolean shouldAlwaysShowOnTerminal() { return this.alwaysShowOnTerminal; }
	public boolean canShowOnTerminal() { return true; }
	public boolean showOnTerminal() {
		if(this.alwaysShowOnTerminal)
			return true;
		else
			return this.hasNetworkUpgrade();
	}
	
	protected final boolean hasNetworkUpgrade() { return UpgradeType.hasUpgrade(UpgradeType.NETWORK, this.upgrades); }
	
	private boolean creative = false;
	public void setCreative(Player player, boolean creative) {
		if(this.hasPermission(player, Permissions.ADMIN_MODE) && this.creative != creative)
		{
			this.creative = creative;
			this.markDirty(this::saveCreative);
			
			if(player != null)
				this.pushLocalNotification(new ChangeCreativeNotification(PlayerReference.of(player), this.creative));
		}
	}
	public boolean isCreative() { return this.creative; }
	
	private boolean isClient = false;
	public void flagAsClient() { this.isClient = true; }
	public boolean isClient() { return this.isClient; }
	
	private final OwnerData owner = new OwnerData(this, o -> this.markDirty(this::saveOwner));
	public final OwnerData getOwner() { return this.owner; }
	
	private final List<PlayerReference> allies = new ArrayList<>();
	public final List<PlayerReference> getAllies() { return new ArrayList<>(this.allies); }
	
	private final Map<String,Integer> allyPermissions = this.getDefaultAllyPermissions();
	
	private Map<String,Integer> getDefaultAllyPermissions() {
		Map<String,Integer> defaultValues = new HashMap<>();
		defaultValues.put(Permissions.OPEN_STORAGE, 1);
		defaultValues.put(Permissions.EDIT_TRADES, 1);
		defaultValues.put(Permissions.EDIT_TRADE_RULES, 1);
		defaultValues.put(Permissions.EDIT_SETTINGS, 1);
		defaultValues.put(Permissions.CHANGE_NAME, 1);
		defaultValues.put(Permissions.VIEW_LOGS, 1);
		defaultValues.put(Permissions.NOTIFICATION, 1);
		
		this.modifyDefaultAllyPermissions(defaultValues);
		return defaultValues;
	}
	
	protected void modifyDefaultAllyPermissions(Map<String,Integer> defaultValues) {}
	
	public boolean hasPermission(Player player, String permission) { return this.getPermissionLevel(player, permission) > 0; }
	public boolean hasPermission(PlayerReference player, String permission) { return this.getPermissionLevel(player, permission) > 0; }
	
	public int getPermissionLevel(Player player, String permission) {
		if(this.isPersistent() && player != null && this.persistentTraderBlockedPermissions().contains(permission))
			return 0;

		if(this.isAdmin(player))
			return Integer.MAX_VALUE;
		
		if(this.isAlly(PlayerReference.of(player)))
			return this.getAllyPermissionLevel(permission);
		
		return 0;
	}
	public int getPermissionLevel(PlayerReference player, String permission) {
		if(this.isPersistent() && player != null && this.persistentTraderBlockedPermissions().contains(permission))
			return 0;

		if(this.isAdmin(player))
			return Integer.MAX_VALUE;
		
		if(this.isAlly(player))
			return this.getAllyPermissionLevel(permission);
		return 0;
		
	}

	private ImmutableList<String> persistentTraderBlockedPermissions() {
		List<String> blockedPermissions = Lists.newArrayList(Permissions.EDIT_TRADES, Permissions.EDIT_SETTINGS, Permissions.INTERACTION_LINK, Permissions.TRANSFER_OWNERSHIP, Permissions.COLLECT_COINS, Permissions.STORE_COINS);
		this.blockPermissionsForPersistentTrader(blockedPermissions);
		return ImmutableList.copyOf(blockedPermissions);
	}

	protected void blockPermissionsForPersistentTrader(List<String> list) {}
	
	public int getAllyPermissionLevel(String permission) { return this.allyPermissions.getOrDefault(permission, 0); }
	public void setAllyPermissionLevel(Player player, String permission, int level) {
		if(this.hasPermission(player, Permissions.EDIT_PERMISSIONS) && this.getAllyPermissionLevel(permission) != level)
		{
			int oldLevel = this.getAllyPermissionLevel(permission);
			this.allyPermissions.put(permission, level);
			this.markDirty(this::saveAllyPermissions);
			//Push local notification
			if(player != null)
				this.pushLocalNotification(new ChangeAllyPermissionNotification(PlayerReference.of(player), permission, level, oldLevel));
		}
	}
	
	private boolean isAdmin(Player player) { return player == null || this.owner.isAdmin(player); }
	private boolean isAdmin(PlayerReference player) { return player == null || this.owner.isAdmin(player); }
	
	private boolean isAlly(PlayerReference player) {
		if(this.owner.isMember(player))
			return true;
		for(PlayerReference ally : this.allies)
		{
			if(ally.is(player))
				return true;
		}
		return false;
	}
	
	private final NotificationData logger = new NotificationData();
	public final List<Notification> getNotifications() { return this.logger.getNotifications(); }
	
	private String customName = "";
	public boolean hasCustomName() { return this.customName.length() > 0; }
	public String getCustomName() { return this.customName; }
	public void setCustomName(Player player, String name) {
		if(this.hasPermission(player, Permissions.CHANGE_NAME) && !this.customName.equals(name))
		{
			String oldName = this.customName;
			
			this.customName = name;
			this.markDirty(this::saveName);
			
			if(player != null)
				this.pushLocalNotification(new ChangeNameNotification(PlayerReference.of(player), this.customName, oldName));
			
		}
	}
	
	public abstract IconData getIcon();
	
	public MutableComponent getName() {
		if(this.hasCustomName())
			return new TextComponent(this.customName);
		return this.getDefaultName();
	}
	
	public final MutableComponent getTitle() {
		if(this.creative)
			return this.getName();
		return new TranslatableComponent("gui.lightmanscurrency.trading.title", this.getName(), this.owner.getOwnerName(this.isClient));
	}
	
	private Item traderBlock;
	protected MutableComponent getDefaultName() {
		if(this.traderBlock != null)
			return new TextComponent("").append(new ItemStack(this.traderBlock).getHoverName());
		return new TranslatableComponent("gui.lightmanscurrency.universaltrader.default");
	}
	
	private CoinValue storedMoney = new CoinValue();
	public CoinValue getStoredMoney()
	{
		BankAccount ba = this.getBankAccount();
		if(ba != null)
			return ba.getCoinStorage();
		return this.storedMoney.copy();
	}
	public CoinValue getInternalStoredMoney() { return this.storedMoney.copy(); }
	public void addStoredMoney(CoinValue amount) {
		BankAccount ba = this.getBankAccount();
		if(ba != null)
		{
			ba.depositCoins(amount);
			ba.LogInteraction(this, amount, true);
			return;
		}
		this.storedMoney.addValue(amount);
		this.markDirty(this::saveStoredMoney);
	}
	public void removeStoredMoney(CoinValue amount) {
		BankAccount ba = this.getBankAccount();
		if(ba != null)
		{
			ba.withdrawCoins(amount);
			ba.LogInteraction(this, amount, false);
			return;
		}	
		this.storedMoney.removeValue(amount);
		this.markDirty(this::saveStoredMoney);
	}
	public void clearStoredMoney() {
		this.storedMoney = new CoinValue();
		this.markDirty(this::saveStoredMoney);
	}
	
	private boolean linkedToBank = false;
	public boolean getLinkedToBank() { return this.linkedToBank; }
	public boolean canLinkBankAccount()
	{
		if(this.owner.hasTeam())
			return this.owner.getTeam().hasBankAccount();
		return true;
	}
	public void setLinkedToBank(Player player, boolean linkedToBank) {
		if(this.hasPermission(player, Permissions.BANK_LINK) && linkedToBank != this.linkedToBank)
		{
			this.linkedToBank = linkedToBank;
			if(this.linkedToBank)
			{
				BankAccount account = this.getBankAccount();
				if(account != null)
				{
					account.depositCoins(this.storedMoney);
					this.storedMoney = new CoinValue();
					this.markDirty(this::saveStoredMoney);
				}
				else
					this.linkedToBank = false;
			}
			this.markDirty(this::saveLinkedBankAccount);
			
			if(player != null)
				this.pushLocalNotification(new ChangeSettingNotification.Simple(PlayerReference.of(player), "BankLink", String.valueOf(this.linkedToBank)));
		}
	}
	
	public boolean hasBankAccount() { return this.getBankAccount() != null; }
	public BankAccount getBankAccount() {
		
		if(this.linkedToBank)
		{
			if(this.owner.hasTeam())
			{
				Team team = this.owner.getTeam();
				if(team != null)
				{
					if(team.hasBankAccount())
					{
						return team.getBankAccount();
					}
				}
			}
			if(this.owner.hasPlayer())
			{
				PlayerReference player = this.owner.getPlayer();
				if(player != null)
				{
					//Get player bank account
					return BankSaveData.GetBankAccount(this.isClient, player.id);
				}
			}
		}
		return null;
	}
	
	private SimpleContainer upgrades;
	public Container getUpgrades() { return this.upgrades; }
	public final boolean allowUpgrade(UpgradeType type) {
		if(!this.showOnTerminal() && this.canShowOnTerminal() && type == UpgradeType.NETWORK)
			return true;
		return this.allowAdditionalUpgradeType(type);
	}
	protected abstract boolean allowAdditionalUpgradeType(UpgradeType type);
	
	private List<TradeRule> rules = new ArrayList<>();
	public List<TradeRule> getRules() { return Lists.newArrayList(this.rules); }
	
	private boolean notificationsEnabled = false;
	public boolean notificationsEnabled() { return this.notificationsEnabled; }
	
	private boolean notificationsToChat = true;
	public boolean notificationsToChat() { return this.notificationsToChat; }
	
	private int teamNotificationLevel = 0;
	public int teamNotificationLevel() { return this.teamNotificationLevel; }
	
	public abstract int getTradeCount();
	public boolean canEditTradeCount() { return false; }
	public int getMaxTradeCount() { return 1; }
	public abstract int getTradeStock(int tradeIndex);
	public abstract boolean hasValidTrade();
	
	
	private ResourceKey<Level> level = Level.OVERWORLD;
	public ResourceKey<Level> getLevel() { return this.level; }
	private BlockPos pos = new BlockPos(0,0,0);
	public BlockPos getPos() { return this.pos; }
	
	public void move(Level level, BlockPos pos)
	{
		if(level != null)
			this.level = level.dimension();
		if(pos != null)
			this.pos = pos;
		if(this.id >= 0)
			this.markDirty(this::saveLevelData);
	}
	
	protected TraderData(ResourceLocation type) {
		this.type = type;
		this.upgrades = new SimpleContainer(5);
		this.upgrades.addListener(c -> this.markDirty(this::saveUpgrades));
		TradeRule.ValidateTradeRuleList(this.rules, this::allowTradeRule);
	}
	
	protected TraderData(ResourceLocation type, Level level, BlockPos pos) {
		this(type);
		this.level = level == null ? Level.OVERWORLD : level.dimension();
		this.pos = pos == null ? new BlockPos(0,0,0) : pos;
		this.traderBlock = level == null ? ModItems.TRADING_CORE.get() : level.getBlockState(this.pos).getBlock().asItem();
	}
	
	private String persistentID = "";
	public boolean isPersistent() { return this.persistentID.length() > 0; }
	public String getPersistentID() { return this.persistentID; }
	public void makePersistent(long id, String persistentID) {
		this.id = id;
		this.persistentID = persistentID;
		this.creative = true;
		this.alwaysShowOnTerminal = true;
	}
	
	protected final void markDirty(CompoundTag updateData) {
		if(this.isClient || !this.canMarkDirty)
			return;
		updateData.putLong("ID", this.id);
		TraderSaveData.MarkTraderDirty(updateData);
	}
	
	@SafeVarargs
	protected final void markDirty(Consumer<CompoundTag>... updateWriters) {
		if(this.isClient || !this.canMarkDirty)
			return;
		CompoundTag updateData = new CompoundTag();
		updateData.putLong("ID", this.id);
		for(Consumer<CompoundTag> u : updateWriters) u.accept(updateData);
		this.markDirty(updateData);
	}
	
	public final CompoundTag save() {
		
		CompoundTag compound = new CompoundTag();
		
		compound.putString("Type", this.type.toString());
		compound.putLong("ID", this.id);
		
		this.saveLevelData(compound);
		this.saveTraderItem(compound);
		this.saveOwner(compound);
		this.saveAllies(compound);
		this.saveAllyPermissions(compound);
		this.saveName(compound);
		this.saveCreative(compound);
		this.saveShowOnTerminal(compound);
		this.saveRules(compound);
		this.saveUpgrades(compound);
		this.saveStoredMoney(compound);
		this.saveLinkedBankAccount(compound);
		this.saveLogger(compound);
		this.saveNotificationData(compound);
		
		//Save persistent trader id
		if(this.persistentID.length() > 0)
			compound.putString("PersistentTraderID", this.persistentID);
		
		//Save trader-specific data
		this.saveAdditional(compound);
		
		return compound;
		
	}
	
	public final void saveLevelData(CompoundTag compound) {
		if(this.pos != null)
		{
			CompoundTag posTag = new CompoundTag();
			posTag.putInt("x", this.pos.getX());
			posTag.putInt("y", this.pos.getY());
			posTag.putInt("z", this.pos.getZ());
			compound.put("WorldPos", posTag);
		}
		if(this.level != null)
			compound.putString("Level", this.level.location().toString());
	}
	
	private void saveTraderItem(CompoundTag compound) { if(this.traderBlock != null) compound.putString("TraderBlock", ForgeRegistries.ITEMS.getKey(this.traderBlock).toString()); }
	
	protected final void saveOwner(CompoundTag compound) { compound.put("OwnerData", this.owner.save()); }
	
	protected final void saveAllies(CompoundTag compound) {
		ListTag allyData = new ListTag();
		for(PlayerReference ally : this.allies)
			allyData.add(ally.save());
		compound.put("Allies", allyData);
	}
	
	protected final void saveAllyPermissions(CompoundTag compound) {
		ListTag allyPermList = new ListTag();
		this.allyPermissions.forEach((perm,level) -> {
			CompoundTag tag = new CompoundTag();
			if(level != 0)
			{
				tag.putString("Permission", perm);
				tag.putInt("Level", level);
				allyPermList.add(tag);
			}
		});
		compound.put("AllyPermissions", allyPermList);
	}
	
	protected final void saveName(CompoundTag compound) { compound.putString("Name", this.customName); }
	
	protected final void saveCreative(CompoundTag compound) { compound.putBoolean("Creative", this.creative); }
	
	protected final void saveShowOnTerminal(CompoundTag compound) { compound.putBoolean("AlwaysShowOnTerminal", this.alwaysShowOnTerminal); }
	
	protected final void saveRules(CompoundTag compound) { TradeRule.saveRules(compound, this.rules, "RuleData"); }
	
	protected final void saveUpgrades(CompoundTag compound) { InventoryUtil.saveAllItems("Upgrades", compound, this.upgrades); }
	
	protected final void saveStoredMoney(CompoundTag compound) { this.storedMoney.save(compound, "StoredMoney"); }
	
	protected final void saveLinkedBankAccount(CompoundTag compound) { compound.putBoolean("LinkedToBank", this.linkedToBank); }
	
	protected final void saveLogger(CompoundTag compound) { compound.put("Logger", this.logger.save()); }
	
	protected final void saveNotificationData(CompoundTag compound) {
		compound.putBoolean("NotificationsEnabled", this.notificationsEnabled);
		compound.putBoolean("ChatNotifications", this.notificationsToChat);
		compound.putInt("TeamNotifications", this.teamNotificationLevel);
	}
	
	protected abstract void saveTrades(CompoundTag compound);
	
	protected abstract void saveAdditional(CompoundTag compound);
	
	public void markTradesDirty() { this.markDirty(this::saveTrades); }
	
	public void markRulesDirty() { this.markDirty(this::saveRules); }
	
	public final JsonObject saveToJson() throws Exception
	{
		if(!this.canMakePersistent())
			throw new Exception("Trader of type '" + this.type.toString() + "' cannot be saved to JSON!");
		
		JsonObject json = new JsonObject();
		
		json.addProperty("Type", this.type.toString());
		
		json.addProperty("Name", this.hasCustomName() ? this.customName : "Trader");
		
		JsonArray ruleData = TradeRule.saveRulesToJson(this.rules);
		if(ruleData.size() > 0)
			json.add("Rules", ruleData);
		
		this.saveAdditionalToJson(json);
		return json;
	}
	
	protected abstract void saveAdditionalToJson(JsonObject json);
	
	public final void load(CompoundTag compound) {
		
		if(compound.contains("ID", Tag.TAG_LONG))
			this.setID(compound.getLong("ID"));
		
		//Load persistent trader id
		if(compound.contains("PersistentTraderID"))
			this.persistentID = compound.getString("PersistentTraderID");
		
		//Position
		if(compound.contains("WorldPos"))
		{
			CompoundTag posTag = compound.getCompound("WorldPos");
			this.pos = new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
		}
		if(compound.contains("Level"))
			this.level = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(compound.getString("Level")));
		
		if(compound.contains("TraderBlock"))
		{
			try {
				this.traderBlock = ForgeRegistries.ITEMS.getValue(new ResourceLocation(compound.getString("TraderBlock")));
			}catch (Throwable ignored) {}
		}
		
		if(compound.contains("OwnerData", Tag.TAG_COMPOUND))
			this.owner.load(compound.getCompound("OwnerData"));
		
		if(compound.contains("Allies"))
		{
			this.allies.clear();
			ListTag allyList = compound.getList("Allies", Tag.TAG_COMPOUND);
			for(int i = 0; i < allyList.size(); ++i)
			{
				PlayerReference ally = PlayerReference.load(allyList.getCompound(i));
				if(ally != null)
					this.allies.add(ally);
			}
		}
		
		if(compound.contains("AllyPermissions"))
		{
			this.allyPermissions.clear();
			ListTag allyPermList = compound.getList("AllyPermissions", Tag.TAG_COMPOUND);
			for(int i = 0; i < allyPermList.size(); ++i)
			{
				CompoundTag tag = allyPermList.getCompound(i);
				String perm = tag.getString("Permission");
				int level = tag.getInt("Level");
				this.allyPermissions.put(perm, level);
			}
		}
		
		if(compound.contains("Name"))
			this.customName = compound.getString("Name");
		
		if(compound.contains("Creative"))
			this.creative = compound.getBoolean("Creative");
		
		if(compound.contains("AlwaysShowOnTerminal"))
			this.alwaysShowOnTerminal = compound.getBoolean("AlwaysShowOnTerminal");
		
		if(compound.contains("RuleData"))
		{
			this.rules = TradeRule.loadRules(compound, "RuleData");
			if(!this.isPersistent())
				TradeRule.ValidateTradeRuleList(this.rules, this::allowTradeRule);
		}
		
		if(compound.contains("Upgrades"))
		{
			this.upgrades = InventoryUtil.loadAllItems("Upgrades", compound, 5);
			this.upgrades.addListener(c -> this.markDirty(this::saveUpgrades));
		}
		
		if(compound.contains("StoredMoney"))
			this.storedMoney.load(compound, "StoredMoney");
		
		if(compound.contains("LinkedToBank"))
			this.linkedToBank = compound.getBoolean("LinkedToBank");
		
		if(compound.contains("Logger"))
			this.logger.load(compound.getCompound("Logger"));
		
		if(compound.contains("NotificationsEnabled"))
			this.notificationsEnabled = compound.getBoolean("NotificationsEnabled");
		if(compound.contains("ChatNotifications"))
			this.notificationsToChat = compound.getBoolean("ChatNotifications");
		if(compound.contains("TeamNotifications"))
			this.teamNotificationLevel = compound.getInt("TeamNotifications");
		
		//Load trader-specific data
		this.loadAdditional(compound);
		
	}
	
	protected abstract void loadAdditional(CompoundTag compound);
	
	public final void loadFromJson(JsonObject json) throws Exception {
		
		if(json.has("OwnerName"))
			this.owner.SetCustomOwner(json.get("OwnerName").getAsString());
		else
			this.owner.SetCustomOwner("Server");
		
		if(json.has("Name"))
			this.customName = json.get("Name").getAsString();
		
		if(json.has("Rules"))
			this.rules = TradeRule.Parse(json.getAsJsonArray("Rules"));
		
		this.loadAdditionalFromJson(json);
	}
	
	protected abstract void loadAdditionalFromJson(JsonObject json) throws Exception;
	
	public final CompoundTag savePersistentData() {
		CompoundTag compound = new CompoundTag();
		//Save persistent trade rule data
		TradeRule.savePersistentData(compound, this.rules, "RuleData");
		//Save additional persistent data
		this.saveAdditionalPersistentData(compound);
		return compound;
	}
	
	protected abstract void saveAdditionalPersistentData(CompoundTag compound);
	
	public final void loadPersistentData(CompoundTag compound) {
		//Load persistent trade rule data
		TradeRule.loadPersistentData(compound, this.rules, "RuleData");
		//Load additional persistent data
		this.loadAdditionalPersistentData(compound);
	}
	
	protected abstract void loadAdditionalPersistentData(CompoundTag compound);
	
	public void openTraderMenu(Player player) {
		if(player instanceof ServerPlayer sp)
			NetworkHooks.openGui(sp, this.getTraderMenuProvider(), this.getMenuDataWriter());
	}
	
	protected MenuProvider getTraderMenuProvider() { return new TraderMenuProvider(this.id); }

	private record TraderMenuProvider(long traderID) implements MenuProvider {

		@Override
		public AbstractContainerMenu createMenu(int windowID, @NotNull Inventory inventory, @NotNull Player player) {
			return new TraderMenu(windowID, inventory, this.traderID);
		}

		@Override
		public @NotNull Component getDisplayName() { return EasyText.empty(); }
	}
	
	public void openStorageMenu(Player player) {
		if(!this.hasPermission(player, Permissions.OPEN_STORAGE))
			return;
		if(player instanceof ServerPlayer sp)
			NetworkHooks.openGui(sp, this.getTraderStorageMenuProvider(), this.getMenuDataWriter());
	}
	
	protected MenuProvider getTraderStorageMenuProvider()  { return new TraderStorageMenuProvider(this.id); }

	private record TraderStorageMenuProvider(long traderID) implements MenuProvider {

		@Override
		public AbstractContainerMenu createMenu(int windowID, @NotNull Inventory inventory, @NotNull Player player) {
			return new TraderStorageMenu(windowID, inventory, this.traderID);
		}

		@Override
		public @NotNull Component getDisplayName() { return EasyText.empty(); }

	}
	
	public Consumer<FriendlyByteBuf> getMenuDataWriter() { return b -> b.writeLong(this.id); }
	
	public PreTradeEvent runPreTradeEvent(PlayerReference player, TradeData trade)
	{
		PreTradeEvent event = new PreTradeEvent(player, trade, this);
		
		//Trader trade rules
		for(TradeRule rule : this.rules)
		{
			if(rule.isActive())
				rule.beforeTrade(event);
		}	
		
		//Trades trade rules
		trade.beforeTrade(event);
		
		//Public posting
		MinecraftForge.EVENT_BUS.post(event);
		
		return event;
	}
	
	public TradeCostEvent runTradeCostEvent(PlayerReference player, TradeData trade)
	{
		TradeCostEvent event = new TradeCostEvent(player, trade, this);
		
		//Trader trade rules
		for(TradeRule rule : this.rules)
		{
			if(rule.isActive())
				rule.tradeCost(event);
		}
		
		//Trades trade rules
		trade.tradeCost(event);
		
		//Public posting
		MinecraftForge.EVENT_BUS.post(event);
		
		return event;
	}
	
	public PostTradeEvent runPostTradeEvent(PlayerReference player, TradeData trade, CoinValue cost)
	{
		PostTradeEvent event = new PostTradeEvent(player, trade, this, cost);
		
		//Trader trade rules
		for(TradeRule rule : this.rules)
		{
			if(rule.isActive())
				rule.afterTrade(event);
		}
		if(event.isDirty())
			this.markRulesDirty();
		event.clean();
		
		//Trades trade rules
		trade.afterTrade(event);
		if(event.isDirty())
			this.markTradesDirty();
		event.clean();
		
		//Public posting
		MinecraftForge.EVENT_BUS.post(event);
		
		return event;
	}
	
	@NotNull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction relativeSide) { return LazyOptional.empty(); }
	
	//Content drops
	public final List<ItemStack> getContents(Level level, BlockPos pos, BlockState state, boolean dropBlock) {
		List<ItemStack> results = new ArrayList<>();
		if(dropBlock)
		{
			Block block = state != null ? state.getBlock() : null;
			ItemStack blockStack = block != null ? new ItemStack(block.asItem()) : ItemStack.EMPTY;
			if(block instanceof ITraderBlock b)
			{
				blockStack = b.getDropBlockItem(level, pos, state);
			}
			if(!blockStack.isEmpty())
				results.add(blockStack);
			else
				LightmansCurrency.LogWarning("Block drop for trader is empty!");
		}
		
		//Add upgrade items
		for(int i = 0; i < this.upgrades.getContainerSize(); ++i)
		{
			ItemStack stack = this.upgrades.getItem(i);
			if(!stack.isEmpty())
				results.add(stack);
		}
		
		//Add stored money
		for(CoinValuePair entry : this.storedMoney.getEntries())
		{
			ItemStack stack = new ItemStack(entry.coin, entry.amount);
			while(stack.getCount() > stack.getMaxStackSize())
				results.add(stack.split(stack.getMaxStackSize()));
			if(!stack.isEmpty())
				results.add(stack);
		}
		
		//Add trader-specific drops
		this.getAdditionalContents(results);
		
		return results;
	}
	
	protected abstract void getAdditionalContents(List<ItemStack> results);
	
	//Static deserializer/registration
	private static final Map<String,NonNullSupplier<TraderData>> deserializers = new HashMap<>();
	
	public static void register(ResourceLocation type, @Nonnull NonNullSupplier<TraderData> source)
	{
		String t = type.toString();
		if(deserializers.containsKey(t))
		{
			LightmansCurrency.LogWarning("Attempted to register duplicate TraderData type '" + t + "'!");
			return;
		}
		deserializers.put(t, source);
	}
	
	public static TraderData Deserialize(boolean isClient, CompoundTag compound)
	{
		if(compound.contains("Type"))
		{
			String type = compound.getString("Type");
			if(deserializers.containsKey(type))
			{
				TraderData data = deserializers.get(type).get();
				if(isClient)
					data.flagAsClient();
				data.load(compound);
				return data;
			}
			else
			{
				LightmansCurrency.LogWarning("Could not deserialize TraderData of type '" + type + "' as no deserializer for that type has been registered!");
				return null;
			}
		}
		else
		{
			LightmansCurrency.LogError("Could not deserialize TraderData as no 'Type' entry was given!");
			return null;
		}
	}
	
	public static TraderData Deserialize(JsonObject json) throws Exception
	{
		if(!json.has("Type") || !json.get("Type").isJsonPrimitive() || !json.get("Type").getAsJsonPrimitive().isString())
			throw new Exception("No string 'Type' entry for this trader.");
		String thisType = json.get("Type").getAsString();
		if(deserializers.containsKey(thisType))
		{
			TraderData data = deserializers.get(thisType).get();
			data.loadFromJson(json);
			return data;
		}
		throw new Exception("Trader type '" + thisType + "' is undefined.");
	}
	
	//Network stuff
	public boolean shouldRemove(MinecraftServer server) {
		if(this.level == null || this.pos == null)
			return false;
		ServerLevel level = server.getLevel(this.level);
		if(level != null && level.isLoaded(this.pos))
		{
			BlockEntity be = level.getBlockEntity(this.pos);
			if(be instanceof TraderBlockEntity<?> tbe)
			{
				return tbe.getTraderID() != this.id;
			}
			return true;
		}
		return false;
	}

	//User data
	private int userCount = 0;
	private final List<Player> currentUsers = new ArrayList<>();
	public List<Player> getUsers() { return new ArrayList<>(this.currentUsers); }
	public int getUserCount() { return this.userCount; }
	
	public void userOpen(Player player) { this.currentUsers.add(player); this.updateUserCount(); }
	public void userClose(Player player) { this.currentUsers.remove(player); this.updateUserCount(); }
	
	private void updateUserCount() {
		if(this.isServer())
		{
			this.userCount = this.currentUsers.size();
			LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageSyncUsers(this.id, this.userCount));
		}
	}
	public void updateUserCount(int userCount)
	{
		if(this.isClient)
			this.userCount = userCount;
	}

	public abstract List<? extends TradeData> getTradeData();
	
	public int indexOfTrade(TradeData trade) { return this.getTradeData().indexOf(trade); }
	
	public abstract void addTrade(Player requestor);
	
	public abstract void removeTrade(Player requestor);
	
	public boolean allowTradeRule(TradeRule rule) { return true; }
	
	public abstract TradeResult ExecuteTrade(TradeContext context, int tradeIndex);
	
	public abstract void addInteractionSlots(List<InteractionSlotData> interactionSlots);
	
	public abstract boolean canMakePersistent();
	
	public Function<TradeData,Boolean> getStorageDisplayFilter(TraderStorageMenu menu) { return TradeButtonArea.FILTER_ANY; }
	
	public abstract void initStorageTabs(TraderStorageMenu menu);
	
	public final void sendTradeRuleMessage(int tradeIndex, ResourceLocation type, CompoundTag updateInfo)
	{
		if(this.isClient)
		{
			CompoundTag message = new CompoundTag();
			message.putString("TradeRuleEdit", type.toString());
			message.putInt("TradeIndex", tradeIndex);
			message.put("TradeRuleData", updateInfo);
			this.sendNetworkMessage(message);
		}
	}
	
	public final void sendNetworkMessage(CompoundTag message)
	{
		if(this.isClient && message != null)
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageTraderMessage(this.id, message));
	}
	
	public void receiveNetworkMessage(Player player, CompoundTag message)
	{
		if(message.contains("ChangePlayerOwner"))
		{
			if(this.hasPermission(player, Permissions.TRANSFER_OWNERSHIP))
			{
				PlayerReference newOwner = PlayerReference.of(this.isClient, message.getString("ChangePlayerOwner"));
				if(newOwner != null && (this.owner.hasTeam() || !newOwner.is(this.owner.getPlayer())))
				{
					Team oldTeam = this.owner.getTeam();
					PlayerReference oldPlayer = this.owner.getPlayer();
					
					this.owner.SetOwner(newOwner);
					if(this.linkedToBank)
					{
						this.linkedToBank = false;
						this.markDirty(this::saveLinkedBankAccount);
					}
					
					if(player != null)
					{
						if(oldTeam != null)
							this.pushLocalNotification(new ChangeOwnerNotification(PlayerReference.of(player), newOwner, oldTeam));
						else if(oldPlayer != null)
							this.pushLocalNotification(new ChangeOwnerNotification(PlayerReference.of(player), newOwner, oldPlayer));
					}
				}
			}
		}
		if(message.contains("ChangeTeamOwner"))
		{
			if(this.hasPermission(player, Permissions.TRANSFER_OWNERSHIP))
			{
				Team team = TeamSaveData.GetTeam(this.isClient, message.getLong("ChangeTeamOwner"));
				if(team != null && team.isMember(player) && team != this.owner.getTeam())
				{
					Team oldTeam = this.owner.getTeam();
					PlayerReference oldPlayer = this.owner.getPlayer();
					
					this.owner.SetOwner(team);
					if(this.linkedToBank)
					{
						this.linkedToBank = false;
						this.markDirty(this::saveLinkedBankAccount);
					}
					
					if(player != null)
					{
						if(oldTeam != null)
							this.pushLocalNotification(new ChangeOwnerNotification(PlayerReference.of(player), team, oldTeam));
						else if(oldPlayer != null)
							this.pushLocalNotification(new ChangeOwnerNotification(PlayerReference.of(player), team, oldPlayer));
					}
				}
			}
		}
		if(message.contains("AddAlly"))
		{
			if(this.hasPermission(player, Permissions.ADD_REMOVE_ALLIES))
			{
				PlayerReference newAlly = PlayerReference.of(this.isClient, message.getString("AddAlly"));
				if(newAlly != null && !PlayerReference.listContains(this.allies, newAlly.id))
				{
					this.allies.add(newAlly);
					this.markDirty(this::saveAllies);
					
					if(player != null)
						this.pushLocalNotification(new AddRemoveAllyNotification(PlayerReference.of(player), true, newAlly));
				}
			}
		}
		if(message.contains("RemoveAlly"))
		{
			if(this.hasPermission(player, Permissions.ADD_REMOVE_ALLIES))
			{
				PlayerReference oldAlly = PlayerReference.of(this.isClient, message.getString("RemoveAlly"));
				if(oldAlly != null && PlayerReference.removeFromList(this.allies, oldAlly.id))
				{
					this.markDirty(this::saveAllies);
					
					if(player != null)
						this.pushLocalNotification(new AddRemoveAllyNotification(PlayerReference.of(player), false, oldAlly));
				}
			}
		}
		if(message.contains("ChangeAllyPermissions"))
		{
			if(this.hasPermission(player, Permissions.EDIT_PERMISSIONS))
			{
				String permission = message.getString("ChangeAllyPermissions");
				int newLevel = message.getInt("NewLevel");
				this.setAllyPermissionLevel(player, permission, newLevel);
			}
		}
		if(message.contains("ChangeName"))
		{
			//LightmansCurrency.LogInfo("Received change name message of value: " + message.getString("ChangeName"));
			this.setCustomName(player, message.getString("ChangeName"));
		}
		if(message.contains("MakeCreative"))
		{
			this.setCreative(player, message.getBoolean("MakeCreative"));
		}
		if(message.contains("LinkToBankAccount"))
		{
			this.setLinkedToBank(player, message.getBoolean("LinkToBankAccount"));
		}
		if(message.contains("Notifications"))
		{
			if(this.hasPermission(player, Permissions.NOTIFICATION))
			{
				boolean enable = message.getBoolean("Notifications");
				if(this.notificationsEnabled != enable)
				{
					this.notificationsEnabled = enable;
					this.markDirty(this::saveNotificationData);
					
					if(player != null)
						this.pushLocalNotification(new ChangeSettingNotification.Simple(PlayerReference.of(player), "Notifications", String.valueOf(this.notificationsEnabled)));
				}
			}
		}
		if(message.contains("NotificationsToChat"))
		{
			if(this.hasPermission(player, Permissions.NOTIFICATION))
			{
				boolean enable = message.getBoolean("NotificationsToChat");
				if(this.notificationsToChat != enable)
				{
					this.notificationsToChat = enable;
					this.markDirty(this::saveNotificationData);
					
					if(player != null)
						this.pushLocalNotification(new ChangeSettingNotification.Simple(PlayerReference.of(player), "NotificationsToChat", String.valueOf(this.notificationsToChat)));
				}
			}
		}
		if(message.contains("TeamNotificationLevel"))
		{
			if(this.hasPermission(player, Permissions.NOTIFICATION))
			{
				int level = message.getInt("TeamNotificationLevel");
				if(this.teamNotificationLevel != level)
				{
					this.teamNotificationLevel = level;
					this.markDirty(this::saveNotificationData);
					
					if(player != null)
						this.pushLocalNotification(new ChangeSettingNotification.Simple(PlayerReference.of(player), "TeamNotificationLevel", String.valueOf(this.teamNotificationLevel)));
				}
			}
		}
		if(message.contains("TradeRuleEdit"))
		{
			if(this.hasPermission(player, Permissions.EDIT_TRADE_RULES))
			{
				ResourceLocation type = new ResourceLocation(message.getString("TradeRuleEdit"));
				int tradeIndex = message.getInt("TradeIndex");
				CompoundTag updateData = message.getCompound("TradeRuleData");
				if(tradeIndex >= 0)
				{
					try {
						TradeData trade = this.getTradeData().get(tradeIndex);
						if(trade != null)
						{
							TradeRule rule = TradeRule.getRule(type, trade.getRules());
							if(rule != null)
							{
								rule.receiveUpdateMessage(updateData);
								this.markTradesDirty();
							}
						}
					} catch(Throwable t) { t.printStackTrace(); }
				}
				else
				{
					TradeRule rule = TradeRule.getRule(type, this.rules);
					if(rule != null)
					{
						rule.receiveUpdateMessage(updateData);
						this.markDirty(this::saveRules);
					}
				}
			}
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public final List<SettingsTab> getSettingsTabs() {
		List<SettingsTab> tabs = Lists.newArrayList(MainTab.INSTANCE, AllyTab.INSTANCE, PermissionsTab.INSTANCE, NotificationTab.INSTANCE);
		this.addSettingsTabs(tabs);
		tabs.add(OwnershipTab.INSTANCE);
		return tabs;
	}
	
	@OnlyIn(Dist.CLIENT)
	protected abstract void addSettingsTabs(List<SettingsTab> tabs);
	
	@OnlyIn(Dist.CLIENT)
	public final List<PermissionOption> getPermissionOptions(){
		List<PermissionOption> options = Lists.newArrayList(
				BooleanPermission.of(Permissions.OPEN_STORAGE),
				BooleanPermission.of(Permissions.CHANGE_NAME),
				BooleanPermission.of(Permissions.EDIT_TRADES),
				BooleanPermission.of(Permissions.COLLECT_COINS),
				BooleanPermission.of(Permissions.STORE_COINS),
				BooleanPermission.of(Permissions.EDIT_TRADE_RULES),
				BooleanPermission.of(Permissions.EDIT_SETTINGS),
				BooleanPermission.of(Permissions.ADD_REMOVE_ALLIES),
				BooleanPermission.of(Permissions.EDIT_PERMISSIONS),
				BooleanPermission.of(Permissions.VIEW_LOGS),
				BooleanPermission.of(Permissions.NOTIFICATION),
				BooleanPermission.of(Permissions.BANK_LINK),
				BooleanPermission.of(Permissions.BREAK_TRADER),
				BooleanPermission.of(Permissions.TRANSFER_OWNERSHIP)
			);
		if(this.showOnTerminal())
			options.add(BooleanPermission.of(Permissions.INTERACTION_LINK));
		
		this.addPermissionOptions(options);
		
		return options;
	}
	
	@OnlyIn(Dist.CLIENT)
	protected abstract void addPermissionOptions(List<PermissionOption> options);
	
	public final void pushLocalNotification(Notification notification)
	{
		if(this.isClient)
			return;
		this.logger.addNotification(notification);
		this.markDirty(this::saveLogger);
	}
	
	public final void pushNotification(NonNullSupplier<Notification> notificationSource) {
		//Notifications are disabled
		if(this.isClient)
			return;
		
		//Push to local notification
		this.pushLocalNotification(notificationSource.get());
		
		if(!this.notificationsEnabled)
			return;
		
		Team team = this.owner.getTeam();
		if(team != null)
		{
			List<PlayerReference> sendTo = new ArrayList<>();
			if(this.teamNotificationLevel < 1)
				sendTo.addAll(team.getMembers());
			if(this.teamNotificationLevel < 2)
				sendTo.addAll(team.getAdmins());
			sendTo.add(team.getOwner());
			for(PlayerReference player: sendTo)
			{
				if(player != null && player.id != null)
					NotificationSaveData.PushNotification(player.id, notificationSource.get(), this.notificationsToChat);
			}
		}
		else if(this.owner.hasPlayer())
		{
			NotificationSaveData.PushNotification(this.owner.getPlayer().id, notificationSource.get(), this.notificationsToChat);
		}
		
	}
	
	public final TraderCategory getNotificationCategory() {
		return new TraderCategory(this.traderBlock != null ? this.traderBlock : ModItems.TRADING_CORE.get(), this.getName(), this.id);
	}
	
	public final @NotNull List<TraderData> getTraders() { return Lists.newArrayList(this); }
	public final boolean isSingleTrader() { return true; }
	
	public static MenuProvider getTraderMenuProvider(BlockPos traderSourcePosition) { return new TraderMenuProviderBlock(traderSourcePosition); }

	private record TraderMenuProviderBlock(BlockPos traderSourcePosition) implements MenuProvider {

		@Override
		public AbstractContainerMenu createMenu(int windowID, @NotNull Inventory inventory, @NotNull Player player) {
			return new TraderMenu.TraderMenuBlockSource(windowID, inventory, this.traderSourcePosition);
		}

		@Override
		public @NotNull Component getDisplayName() { return EasyText.empty(); }

	}
	
	@Deprecated
	public final void loadOldUniversalTraderData(CompoundTag compound) {
		
		//Core Settings
		if(compound.contains("CoreSettings", Tag.TAG_COMPOUND))
			this.loadOldCoreSettingData(compound.getCompound("CoreSettings"));
		
		//Position
		if(compound.contains("x") && compound.contains("y") && compound.contains("z"))
			this.pos = new BlockPos(compound.getInt("x"), compound.getInt("y"), compound.getInt("z"));
		if(compound.contains("World"))
			this.level = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(compound.getString("World")));
		//Stored Money
		if(compound.contains("StoredMoney"))
			this.storedMoney.load(compound, "StoredMoney");
		
		this.loadExtraOldUniversalTraderData(compound);
	}
	
	@Deprecated
	private void loadOldCoreSettingData(CompoundTag compound) {
		if(compound.contains("Owner",Tag.TAG_COMPOUND))
			this.owner.SetOwner(PlayerReference.load(compound.getCompound("Owner")));
		if(compound.contains("CustomOwnerName", Tag.TAG_STRING))
			this.owner.SetCustomOwner(compound.getString("CustomOwnerName"));
		if(compound.contains("Team", Tag.TAG_INT_ARRAY))
		{
			Team team = TeamSaveData.GetTeam(this.isClient, DataConverter.getNewTeamID(compound.getUUID("Team")));
			if(team != null)
				this.owner.SetOwner(team);
		}
		if(compound.contains("Allies"))
		{
			this.allies.clear();
			this.allies.addAll(PlayerReference.loadList(compound, "Allies"));
		}
		if(compound.contains("AllyPermissions", Tag.TAG_LIST))
		{
			this.allyPermissions.clear();
			ListTag list = compound.getList("AllyPermissions", Tag.TAG_COMPOUND);
			for(int i = 0; i < list.size(); ++i)
			{
				CompoundTag thisCompound = list.getCompound(i);
				String permission = thisCompound.getString("permission");
				int level = thisCompound.getInt("level");
				this.allyPermissions.put(permission, level);
			}
		}
		if(compound.contains("CustomName", Tag.TAG_STRING))
			this.customName = compound.getString("CustomName");
		if(compound.contains("Creative"))
			this.creative = compound.getBoolean("Creative");
		if(compound.contains("BankLink"))
			this.linkedToBank = compound.getBoolean("BankLink");
		if(compound.contains("Notifications"))
			this.notificationsEnabled = compound.getBoolean("Notifications");
		if(compound.contains("ChatNotifications"))
			this.notificationsToChat = compound.getBoolean("ChatNotifications");
		if(compound.contains("TeamNotifications"))
			this.teamNotificationLevel = compound.getInt("TeamNotifications");
		//Logger
		//Not going to bother loading the settings logger. I think this is something not worth fighting over.
	}
	
	@Deprecated
	protected final void loadOldUpgradeData(Container upgradeInventory) {
		this.upgrades.clearContent();
		for(int i = 0; i < this.upgrades.getContainerSize() && i < upgradeInventory.getContainerSize(); ++i)
			this.upgrades.setItem(i, upgradeInventory.getItem(i));
		this.upgrades.addListener(c -> this.markDirty(this::saveUpgrades));
	}
	
	@Deprecated
	protected final void loadOldTradeRuleData(List<TradeRule> rules) {
		this.rules = rules;
		for(TradeRule r : this.rules)
			r.setActive(true);
		TradeRule.ValidateTradeRuleList(this.rules, this::allowTradeRule);
	}
	
	@Deprecated
	protected abstract void loadExtraOldUniversalTraderData(CompoundTag compound);
	
	@Deprecated
	public final void loadOldBlockEntityData(CompoundTag compound) {
		if(compound.contains("CoreSettings"))
			this.loadOldCoreSettingData(compound.getCompound("CoreSettings"));
		
		this.storedMoney.load(compound, "StoredMoney");
		
		this.loadExtraOldBlockEntityData(compound);
	}
	
	@Deprecated
	protected abstract void loadExtraOldBlockEntityData(CompoundTag compound);
	
}