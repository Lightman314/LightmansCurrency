package io.github.lightman314.lightmanscurrency.api.traders;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyStorage;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxCollector;
import io.github.lightman314.lightmanscurrency.api.taxes.TaxAPI;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core.*;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.providers.EasyMenuProvider;
import io.github.lightman314.lightmanscurrency.common.menus.validation.EasyMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.SimpleValidator;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxable;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.api.misc.world.WorldPosition;
import io.github.lightman314.lightmanscurrency.api.taxes.reference.TaxableReference;
import io.github.lightman314.lightmanscurrency.api.taxes.reference.builtin.TaxableTraderReference;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.rules.ITradeRuleHost;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.ResourceLocationException;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.ITraderBlock;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.bank.BankSaveData;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.IDumpable;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationSaveData;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.AddRemoveAllyNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeAllyPermissionNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeCreativeNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeNameNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeOwnerNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeSettingNotification;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.BooleanPermission;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.PermissionOption;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.network.message.trader.SPacketSyncUsers;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.api.upgrades.IUpgradeable;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
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
import net.minecraftforge.registries.ForgeRegistries;

public abstract class TraderData implements IClientTracker, IDumpable, IUpgradeable, ITraderSource, ITradeRuleHost, ITaxable {
	
	public static final int GLOBAL_TRADE_LIMIT = 100;
	
	private boolean canMarkDirty = false;
	public final TraderData allowMarkingDirty() { this.canMarkDirty = true; return this; }
	
	public final TraderType<?> type;
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
	
	protected final boolean hasNetworkUpgrade() { return UpgradeType.hasUpgrade(Upgrades.NETWORK, this.upgrades); }
	
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

	protected List<String> getBlockedPermissions() { return ImmutableList.of(); }
	
	public boolean hasPermission(Player player, String permission) { return this.getPermissionLevel(player, permission) > 0; }
	public boolean hasPermission(PlayerReference player, String permission) { return this.getPermissionLevel(player, permission) > 0; }
	
	public int getPermissionLevel(Player player, String permission) {
		if(this.isPersistent() && player != null && this.persistentTraderBlockedPermissions().contains(permission))
			return 0;
		if(player != null && this.getBlockedPermissions().contains(permission))
			return 0;
		if(this.isAdmin(player))
			return Integer.MAX_VALUE;
		
		if(this.isAlly(player))
			return this.getAllyPermissionLevel(permission);
		
		return 0;
	}
	public int getPermissionLevel(PlayerReference player, String permission) {
		if(this.isPersistent() && player != null && this.persistentTraderBlockedPermissions().contains(permission))
			return 0;
		if(player != null && this.getBlockedPermissions().contains(permission))
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

	protected void blockPermissionsForPersistentTrader(List<String> list) { }
	
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

	private boolean isAlly(Player player) {
		if(this.owner.isMember(player))
			return true;
		return PlayerReference.isInList(this.allies, player);
	}
	private boolean isAlly(PlayerReference player) {
		if(this.owner.isMember(player))
			return true;
		return PlayerReference.isInList(this.allies, player);
	}
	
	private final NotificationData logger = new NotificationData();
	public final List<Notification> getNotifications() { return this.logger.getNotifications(); }
	
	private String customName = "";
	public boolean hasCustomName() { return !this.customName.isBlank(); }
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

	@Override
	@Nonnull
	public MutableComponent getName() {
		if(this.hasCustomName())
			return EasyText.literal(this.customName);
		return this.getDefaultName();
	}
	
	public final MutableComponent getTitle() {
		if(this.creative)
			return this.getName();
		return EasyText.translatable("gui.lightmanscurrency.trading.title", this.getName(), this.owner.getOwnerName(this.isClient));
	}
	
	private Item traderBlock;
	protected MutableComponent getDefaultName() {
		if(this.traderBlock != null)
			return EasyText.literal(new ItemStack(this.traderBlock).getHoverName().getString());
		return EasyText.translatable("gui.lightmanscurrency.universaltrader.default");
	}
	
	private final MoneyStorage storedMoney = new MoneyStorage(() -> this.markDirty(this::saveStoredMoney));
	public IMoneyHolder getStoredMoney()
	{
		IBankAccount ba = this.getBankAccount();
		if(ba != null)
			return ba.getMoneyStorage();
		return this.getInternalStoredMoney();
	}
	public MoneyStorage getInternalStoredMoney() { return this.storedMoney; }

	public MoneyValue addStoredMoney(MoneyValue amount, boolean shouldTax) {
		MoneyValue taxesPaid = MoneyValue.empty();
		if(shouldTax)
		{
			taxesPaid = this.payTaxesOn(amount);
			if(!amount.containsValue(taxesPaid))
			{
				//Remove excess money
				//Will add warning for owner if tax percent is somehow greater than 100%
				//May also lock the trader from interactions until they can report the issue to the relevant parties
				this.removeStoredMoney(taxesPaid.subtractValue(amount), false);
				return taxesPaid;
			}
			else
			{
				amount = amount.subtractValue(taxesPaid);
				if(amount.isEmpty())
					return taxesPaid;
			}
		}
		IBankAccount ba = this.getBankAccount();
		if(ba != null)
		{
			ba.depositMoney(amount);
			if(ba instanceof BankAccount ba2)
				ba2.LogInteraction(this, amount, true);
			return taxesPaid;
		}
		this.storedMoney.addValue(amount);
		return taxesPaid;
	}

	public MoneyValue removeStoredMoney(MoneyValue amount, boolean shouldTax) {
		MoneyValue taxesPaid = MoneyValue.empty();
		if(shouldTax)
		{
			taxesPaid = this.payTaxesOn(amount);
			if(!taxesPaid.isEmpty())
				amount = amount.addValue(taxesPaid);
		}
		IBankAccount ba = this.getBankAccount();
		if(ba != null) {
			ba.withdrawMoney(amount);
			if(ba instanceof BankAccount ba2)
				ba2.LogInteraction(this, amount, false);
			return taxesPaid;
		}
		this.storedMoney.removeValue(amount);
		return taxesPaid;
	}
	public void CollectStoredMoney(@Nonnull Player player)
	{
		if(this.hasPermission(player, Permissions.COLLECT_COINS))
		{
			MoneyStorage storedMoney = this.getInternalStoredMoney();
			if(storedMoney.isEmpty())
				return;
			storedMoney.GiveToPlayer(player);
		}
		else
			Permissions.PermissionWarning(player, "collect stored coins", Permissions.COLLECT_COINS);
	}

	public final MoneyValue payTaxesOn(MoneyValue amount)
	{
		MoneyValue paidCache = MoneyValue.empty();
		for(ITaxCollector tax : this.getApplicableTaxes())
		{
			//Obey ignored tax settings
			if(!this.ShouldIgnoreTaxEntry(tax))
				paidCache = tax.CalculateAndPayTaxes(this, amount);
		}
		return paidCache;
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
				IBankAccount account = this.getBankAccount();
				if(account != null)
				{
					for(MoneyValue value : this.storedMoney.allValues())
						account.depositMoney(value);
					this.storedMoney.clear();
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
	public IBankAccount getBankAccount() {
		
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
	public final boolean allowUpgrade(@Nonnull UpgradeType type) {
		if(!this.showOnTerminal() && this.canShowOnTerminal() && type == Upgrades.NETWORK)
			return true;
		return this.allowAdditionalUpgradeType(type);
	}
	protected abstract boolean allowAdditionalUpgradeType(UpgradeType type);
	
	private List<TradeRule> rules = new ArrayList<>();
	@Nonnull
	@Override
	public List<TradeRule> getRules() { return Lists.newArrayList(this.rules); }
	protected void validateRules() { TradeRule.ValidateTradeRuleList(this.rules, this); }
	
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

	private int acceptableTaxRate = 99;
	public final int getAcceptableTaxRate() { return this.acceptableTaxRate; }
	private final List<Long> ignoredTaxCollectors = new ArrayList<>();
	private boolean ignoreAllTaxes = false;
	public boolean ShouldIgnoreAllTaxes() { return this.ignoreAllTaxes; }
	public boolean ShouldIgnoreTaxEntryOnly(@Nonnull ITaxCollector entry) { return this.ignoredTaxCollectors.contains(entry.getID()); }
	public void FlagTaxEntryToIgnore(@Nonnull TaxEntry entry, @Nonnull Player player) {
		if(this.ignoredTaxCollectors.contains(entry.getID()))
			return;
		if(!LCAdminMode.isAdminPlayer(player))
		{
			Permissions.PermissionWarning(player, "ignore tax collector", Permissions.ADMIN_MODE);
			return;
		}
		this.ignoredTaxCollectors.add(entry.getID());
		this.markDirty(this::saveTaxSettings);
	}
	public void PardonTaxEntry(@Nonnull TaxEntry entry)
	{
		if(this.ignoredTaxCollectors.contains(entry.getID()))
		{
			this.ignoredTaxCollectors.remove(entry.getID());
			this.markDirty(this::saveTaxSettings);
		}
	}
	private boolean AllowTaxEntry(@Nonnull ITaxCollector entry) { return !this.ShouldIgnoreTaxEntry(entry); }
	public boolean ShouldIgnoreTaxEntry(@Nonnull ITaxCollector entry) { return this.ShouldIgnoreAllTaxes() || this.ShouldIgnoreTaxEntryOnly(entry); }

	private WorldPosition worldPosition = WorldPosition.VOID;
	public ResourceKey<Level> getLevel() { return this.worldPosition.getDimension(); }
	public BlockPos getPos() { return this.worldPosition.getPos(); }

	@Nonnull
	@Override
	public TaxableReference getReference() { return new TaxableTraderReference(this.getID()); }

	@Nonnull
	@Override
	public WorldPosition getWorldPosition() { return this.worldPosition; }

	public final List<ITaxCollector> getApplicableTaxes() { return TaxAPI.GetActiveTaxCollectorsFor(this).stream().filter(this::AllowTaxEntry).toList(); }
	public final List<ITaxCollector> getPossibleTaxes() { return TaxAPI.GetPossibleTaxCollectorsFor(this); }
	public final int getTotalTaxPercentage()
	{
		List<? extends ITaxCollector> entries = this.getApplicableTaxes();
		int taxPercentage = 0;
		for(ITaxCollector entry : entries)
			taxPercentage += entry.getTaxRate();
		return taxPercentage;
	}
	public final boolean exceedsAcceptableTaxRate() { return this.getTotalTaxPercentage() > this.acceptableTaxRate; }

	//Updates the world position of the trader.
	public void move(Level level, BlockPos pos)
	{
		this.worldPosition = WorldPosition.ofLevel(level, pos);
		if(this.id >= 0)
			this.markDirty(this::saveLevelData);
	}
	
	protected TraderData(@Nonnull TraderType<?> type) {
		this.type = type;
		this.upgrades = new SimpleContainer(5);
		this.upgrades.addListener(c -> this.markDirty(this::saveUpgrades));
	}
	
	protected TraderData(@Nonnull TraderType<?> type, @Nonnull Level level, @Nonnull BlockPos pos) {
		this(type);
		this.worldPosition = WorldPosition.ofLevel(level, pos);
		this.traderBlock = level == null ? ModItems.TRADING_CORE.get() : level.getBlockState(this.worldPosition.getPos()).getBlock().asItem();
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
		this.saveTaxSettings(compound);
		
		//Save persistent trader id
		if(this.persistentID.length() > 0)
			compound.putString("PersistentTraderID", this.persistentID);
		
		//Save trader-specific data
		this.saveAdditional(compound);
		
		return compound;
		
	}
	
	public final void saveLevelData(CompoundTag compound) {
		compound.put("Location", this.worldPosition.save());
	}
	
	private void saveTraderItem(CompoundTag compound) { if(this.traderBlock != null) compound.putString("TraderBlock", ForgeRegistries.ITEMS.getKey(this.traderBlock).toString()); }
	
	protected final void saveOwner(CompoundTag compound) { compound.put("OwnerData", this.owner.save()); }
	
	protected final void saveAllies(CompoundTag compound) {
		PlayerReference.saveList(compound, this.allies, "Allies");
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
	
	protected final void saveStoredMoney(CompoundTag compound) { compound.put("StoredMoney", this.storedMoney.save()); }
	
	protected final void saveLinkedBankAccount(CompoundTag compound) { compound.putBoolean("LinkedToBank", this.linkedToBank); }
	
	protected final void saveLogger(CompoundTag compound) { compound.put("Logger", this.logger.save()); }
	
	protected final void saveNotificationData(CompoundTag compound) {
		compound.putBoolean("NotificationsEnabled", this.notificationsEnabled);
		compound.putBoolean("ChatNotifications", this.notificationsToChat);
		compound.putInt("TeamNotifications", this.teamNotificationLevel);
	}

	protected final void saveTaxSettings(CompoundTag compound) {
		compound.putInt("AcceptableTaxRate", this.acceptableTaxRate);
		compound.putBoolean("IgnoreAllTaxCollectors", this.ignoreAllTaxes);
		compound.putLongArray("IgnoreTaxCollectors", this.ignoredTaxCollectors);
	}
	
	protected abstract void saveTrades(CompoundTag compound);
	
	protected abstract void saveAdditional(CompoundTag compound);
	
	public void markTradesDirty() { this.markDirty(this::saveTrades); }
	
	public void markTradeRulesDirty() { this.markDirty(this::saveRules); }
	
	public final JsonObject saveToJson(String id, String ownerName) throws Exception
	{
		if(!this.canMakePersistent())
			throw new Exception("Trader of type '" + this.type.toString() + "' cannot be saved to JSON!");
		
		JsonObject json = new JsonObject();
		
		json.addProperty("Type", this.type.toString());
		json.addProperty("ID", id);
		json.addProperty("Name", this.hasCustomName() ? this.customName : "Trader");
		json.addProperty("OwnerName", ownerName);
		
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
		
		//Position (Old Style)
		if(compound.contains("WorldPos") && compound.contains("Level"))
		{
			CompoundTag posTag = compound.getCompound("WorldPos");
			BlockPos pos = new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
			ResourceKey<Level> dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(compound.getString("Level")));
			this.worldPosition = WorldPosition.of(dimension, pos);
		}
		else if(compound.contains("Location"))
			this.worldPosition = WorldPosition.load(compound.getCompound("Location"));
		
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
			this.allies.addAll(PlayerReference.loadList(compound, "Allies"));
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
			this.rules = TradeRule.loadRules(compound, "RuleData", this);
		
		if(compound.contains("Upgrades"))
		{
			this.upgrades = InventoryUtil.loadAllItems("Upgrades", compound, 5);
			this.upgrades.addListener(c -> this.markDirty(this::saveUpgrades));
		}
		
		if(compound.contains("StoredMoney"))
			this.storedMoney.safeLoad(compound, "StoredMoney");
		
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

		if(compound.contains("AcceptableTaxRate"))
			this.acceptableTaxRate = compound.getInt("AcceptableTaxRate");
		if(compound.contains("IgnoreAllTaxCollectors"))
			this.ignoreAllTaxes = compound.getBoolean("IgnoreAllTaxCollectors");
		if(compound.contains("IgnoreTaxCollectors"))
		{
			this.ignoredTaxCollectors.clear();
			for(long val : compound.getLongArray("IgnoreTaxCollectors"))
				this.ignoredTaxCollectors.add(val);
		}

		//Load trader-specific data
		this.loadAdditional(compound);
		
	}

	/**
	 * Code ran when the Trader is in it's fully registered/added state.
	 * Does not promise that other traders are also fully loaded and/or registered to the Trader Save Data.
	 * Run on both Server and Client, so ensure you check this.isClient() or this.isServer()
	 * to confirm what logical side this trader is loaded on.
	 */
	public void OnRegisteredToOffice() {
		if(this.isServer() && !this.isPersistent())
			TradeRule.ValidateTradeRuleList(this.rules, this);
	}
	
	protected abstract void loadAdditional(CompoundTag compound);
	
	public final void loadFromJson(JsonObject json) throws JsonSyntaxException, ResourceLocationException {
		this.owner.SetCustomOwner(GsonHelper.getAsString(json, "OwnerName", "Server"));
		
		if(json.has("Name"))
			this.customName = GsonHelper.getAsString(json, "Name");
		
		if(json.has("Rules"))
			this.rules = TradeRule.Parse(GsonHelper.getAsJsonArray(json, "Rules"), this);
		
		this.loadAdditionalFromJson(json);
	}
	
	protected abstract void loadAdditionalFromJson(JsonObject json) throws JsonSyntaxException, ResourceLocationException;
	
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

	@Deprecated(since = "2.1.2.3")
	public void openTraderMenu(Player player) { this.openTraderMenu(player, SimpleValidator.NULL); }
	public void openTraderMenu(Player player, @Nonnull MenuValidator validator) {
		if(player instanceof ServerPlayer sp)
			NetworkHooks.openScreen(sp, this.getTraderMenuProvider(validator), EasyMenu.encoder(this.getMenuDataWriter(), validator));
	}
	
	protected MenuProvider getTraderMenuProvider(@Nonnull MenuValidator validator) { return new TraderMenuProvider(this.id, validator); }

	private record TraderMenuProvider(long traderID, @Nonnull MenuValidator validator) implements EasyMenuProvider {

		@Override
		public AbstractContainerMenu createMenu(int windowID, @Nonnull Inventory inventory, @Nonnull Player player) { return new TraderMenu(windowID, inventory, this.traderID, validator); }

	}

	@Deprecated(since = "2.1.2.3")
	public void openStorageMenu(@Nonnull Player player) { this.openStorageMenu(player, SimpleValidator.NULL); }
	public void openStorageMenu(@Nonnull Player player, @Nonnull MenuValidator validator) {
		if(!this.hasPermission(player, Permissions.OPEN_STORAGE))
			return;
		if(player instanceof ServerPlayer sp)
			NetworkHooks.openScreen(sp, this.getTraderStorageMenuProvider(validator), EasyMenu.encoder(this.getMenuDataWriter(), validator));
	}
	
	protected MenuProvider getTraderStorageMenuProvider(@Nonnull MenuValidator validator)  { return new TraderStorageMenuProvider(this.id, validator); }

	private record TraderStorageMenuProvider(long traderID, @Nonnull MenuValidator validator) implements EasyMenuProvider {

		@Override
		public AbstractContainerMenu createMenu(int windowID, @Nonnull Inventory inventory, @Nonnull Player player) { return new TraderStorageMenu(windowID, inventory, this.traderID, this.validator); }

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

	public void runPostTradeEvent(PlayerReference player, TradeData trade, MoneyValue cost, MoneyValue taxesPaid)
	{
		PostTradeEvent event = new PostTradeEvent(player, trade, this, cost, taxesPaid);
		
		//Trader trade rules
		for(TradeRule rule : this.rules)
		{
			if(rule.isActive())
				rule.afterTrade(event);
		}
		if(event.isDirty())
			this.markTradeRulesDirty();
		event.clean();
		
		//Trades trade rules
		trade.afterTrade(event);
		if(event.isDirty())
			this.markTradesDirty();
		event.clean();
		
		//Public posting
		MinecraftForge.EVENT_BUS.post(event);
	}
	
	@Nonnull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction relativeSide) { return LazyOptional.empty(); }
	
	//Content drops
	public final List<ItemStack> getContents(Level level, BlockPos pos, BlockState state, boolean dropBlock) {
		List<ItemStack> results = new ArrayList<>();
		if(dropBlock)
		{
			Block block = state != null ? state.getBlock() : null;
			ItemStack blockStack = block != null ? new ItemStack(block.asItem()) : ItemStack.EMPTY;
			if(block instanceof ITraderBlock b)
				blockStack = b.getDropBlockItem(level, pos, state);
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
		for(MoneyValue value : this.storedMoney.allValues())
		{
			List<ItemStack> items = value.onBlockBroken(level, this.owner);
			if(items != null)
				results.addAll(items);
		}
		
		//Add trader-specific drops
		this.getAdditionalContents(results);
		
		return results;
	}
	
	protected abstract void getAdditionalContents(List<ItemStack> results);
	
	public static TraderData Deserialize(boolean isClient, CompoundTag compound)
	{
		if(compound.contains("Type"))
		{
			String type = compound.getString("Type");
			TraderType<?> traderType = TraderAPI.getTraderType(new ResourceLocation(type));
			if(traderType != null)
				return traderType.load(isClient, compound);
			else
			{
				LightmansCurrency.LogWarning("Could not deserialize TraderData of type '" + type + "' as no TraderType has been registered with that id!");
				return null;
			}
		}
		else
		{
			LightmansCurrency.LogError("Could not deserialize TraderData as no 'Type' entry was given!");
			return null;
		}
	}
	
	public static TraderData Deserialize(JsonObject json) throws JsonSyntaxException, ResourceLocationException
	{
		String thisType = GsonHelper.getAsString(json, "Type");
		TraderType<?> traderType = TraderAPI.getTraderType(new ResourceLocation(thisType));
		if(traderType != null)
			return traderType.loadFromJson(json);
		throw new JsonSyntaxException("Trader type '" + thisType + "' is undefined.");
	}
	
	//Network stuff
	public boolean shouldRemove(MinecraftServer server) {
		if(this.worldPosition.isVoid())
			return false;
		ServerLevel level = server.getLevel(this.getLevel());
		if(level != null && level.isLoaded(this.getPos()))
		{
			BlockEntity be = level.getBlockEntity(this.getPos());
			if(be instanceof TraderBlockEntity<?> tbe)
				return tbe.getTraderID() != this.id;
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
			new SPacketSyncUsers(this.id, this.userCount).sendToAll();
		}
	}
	public void updateUserCount(int userCount)
	{
		if(this.isClient)
			this.userCount = userCount;
	}

	@Nonnull
	public abstract List<? extends TradeData> getTradeData();

	@Nullable
	public abstract TradeData getTrade(int tradeIndex);

	
	public int indexOfTrade(TradeData trade) { return this.getTradeData().indexOf(trade); }
	
	public abstract void addTrade(Player requestor);
	
	public abstract void removeTrade(Player requestor);


	//ITradeRuleHost Overrides
	@Override
	public final boolean isTrader() { return true; }

	@Override
	public final boolean isTrade() { return false; }

	@Override
	public boolean canMoneyBeRelevant() {
		List<? extends TradeData> trades = this.getTradeData();
		if(trades != null)
			return trades.stream().anyMatch(TradeData::canMoneyBeRelevant);
		return true;
	}

	//For Traders, allow rules that affect money if money can be relevant at any point.
	@Override
	public boolean isMoneyRelevant() { return this.canMoneyBeRelevant(); }

	@Override
	public boolean allowTradeRule(@Nonnull TradeRule rule) { return true; }

	public final TradeResult TryExecuteTrade(@Nonnull TradeContext context, int tradeIndex)
	{
		if(this.exceedsAcceptableTaxRate())
			return TradeResult.FAIL_TAX_EXCEEDED_LIMIT;
		return this.ExecuteTrade(context, tradeIndex);
	}

	/**
	 * Should not be called directly. Call TryExecuteTrade when possible!
	 */
	protected abstract TradeResult ExecuteTrade(TradeContext context, int tradeIndex);
	
	public void addInteractionSlots(@Nonnull List<InteractionSlotData> interactionSlots) {}
	
	public abstract boolean canMakePersistent();
	
	public Function<TradeData,Boolean> getStorageDisplayFilter(@Nonnull ITraderStorageMenu menu) { return TradeButtonArea.FILTER_ANY; }
	
	public abstract void initStorageTabs(@Nonnull ITraderStorageMenu menu);

	public void handleSettingsChange(@Nonnull Player player, @Nonnull LazyPacketData message)
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

					if(oldTeam != null)
						this.pushLocalNotification(new ChangeOwnerNotification(PlayerReference.of(player), newOwner, oldTeam));
					else if(oldPlayer != null)
						this.pushLocalNotification(new ChangeOwnerNotification(PlayerReference.of(player), newOwner, oldPlayer));
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

					if(oldTeam != null)
						this.pushLocalNotification(new ChangeOwnerNotification(PlayerReference.of(player), team, oldTeam));
					else if(oldPlayer != null)
						this.pushLocalNotification(new ChangeOwnerNotification(PlayerReference.of(player), team, oldPlayer));
				}
			}
		}
		if(message.contains("AddAlly"))
		{
			if(this.hasPermission(player, Permissions.ADD_REMOVE_ALLIES))
			{
				PlayerReference newAlly = PlayerReference.of(this.isClient, message.getString("AddAlly"));
				if(newAlly != null && !PlayerReference.isInList(this.allies, newAlly.id))
				{
					this.allies.add(newAlly);
					this.markDirty(this::saveAllies);

					this.pushLocalNotification(new AddRemoveAllyNotification(PlayerReference.of(player), true, newAlly));
				}
			}
		}
		if(message.contains("RemoveAlly"))
		{
			if(this.hasPermission(player, Permissions.ADD_REMOVE_ALLIES))
			{
				PlayerReference oldAlly = PlayerReference.of(this.isClient, message.getString("RemoveAlly"));
				if(PlayerReference.removeFromList(this.allies, oldAlly))
				{
					this.markDirty(this::saveAllies);

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

					this.pushLocalNotification(new ChangeSettingNotification.Simple(PlayerReference.of(player), "TeamNotificationLevel", String.valueOf(this.teamNotificationLevel)));
				}
			}
		}
		if(message.contains("AcceptableTaxRate"))
		{
			if(this.hasPermission(player, Permissions.EDIT_SETTINGS))
			{
				int newRate = MathUtil.clamp(message.getInt("AcceptableTaxRate"), 0, 99);
				if(newRate == this.acceptableTaxRate)
					return;

				this.pushLocalNotification(new ChangeSettingNotification.Advanced(PlayerReference.of(player), "AcceptableTaxRate", String.valueOf(newRate), String.valueOf(this.acceptableTaxRate)));
				this.acceptableTaxRate = newRate;

				this.markDirty(this::saveTaxSettings);
			}
		}
		if(message.contains("ForceIgnoreAllTaxCollectors"))
		{
			boolean newState = message.getBoolean("ForceIgnoreAllTaxCollectors");
			if((!newState || LCAdminMode.isAdminPlayer(player)) && newState != this.ignoreAllTaxes)
			{
				this.ignoreAllTaxes = newState;
				this.pushLocalNotification(new ChangeSettingNotification.Simple(PlayerReference.of(player), "IgnoreAllTaxes", String.valueOf(this.ignoreAllTaxes)));
				this.markDirty(this::saveTaxSettings);
			}
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public final List<SettingsSubTab> getSettingsTabs(TraderSettingsClientTab tab) {
		//Set up defailt tabs
		List<SettingsSubTab> tabs = Lists.newArrayList(new MainTab(tab), new AllyTab(tab), new PermissionsTab(tab), new NotificationTab(tab), new TaxSettingsTab(tab));
		//Add Trader-Defined tabs
		this.addSettingsTabs(tab, tabs);
		//Add Ownership Tab last
		tabs.add(new OwnershipTab(tab));
		return tabs;
	}
	
	@OnlyIn(Dist.CLIENT)
	protected void addSettingsTabs(TraderSettingsClientTab tab, List<SettingsSubTab> tabs) { }
	
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

		this.handleBlockedPermissions(options);
		
		return options;
	}
	
	@OnlyIn(Dist.CLIENT)
	protected abstract void addPermissionOptions(List<PermissionOption> options);

	@OnlyIn(Dist.CLIENT)
	protected final void handleBlockedPermissions(List<PermissionOption> options)
	{
		//Remove blocked permissions from the permissions options list.
		for(String blockedPerm : this.getBlockedPermissions())
		{
			for(int i = 0; i < options.size(); i++)
			{
				if(Objects.equals(options.get(i).permission, blockedPerm))
				{
					options.remove(i);
					i--;
				}
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void onScreenInit(TraderScreen screen, Consumer<Object> addWidget) { }

	@OnlyIn(Dist.CLIENT)
	public void onStorageScreenInit(TraderStorageScreen screen, Consumer<Object> addWidget) { }

	public final void pushLocalNotification(Notification notification)
	{
		if(this.isClient)
			return;
		this.logger.addNotification(notification);
		this.markDirty(this::saveLogger);
	}
	
	public final void pushNotification(@Nonnull NonNullSupplier<Notification> notificationSource) {
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

	@Nonnull
	public final List<TraderData> getTraders() { return Lists.newArrayList(this); }
	public final boolean isSingleTrader() { return true; }
	
	public static MenuProvider getTraderMenuProvider(@Nonnull BlockPos traderSourcePosition, @Nonnull MenuValidator validator) { return new TraderMenuProviderBlock(traderSourcePosition, validator); }

	private record TraderMenuProviderBlock(@Nonnull BlockPos traderSourcePosition, @Nonnull MenuValidator validator) implements EasyMenuProvider {

		@Override
		public AbstractContainerMenu createMenu(int windowID, @Nonnull Inventory inventory, @Nonnull  Player player) { return new TraderMenu.TraderMenuBlockSource(windowID, inventory, this.traderSourcePosition, this.validator); }

	}

	@Nonnull
	public final List<Component> getTerminalInfo(@Nullable Player player)
	{
		List<Component> info = new ArrayList<>();
		this.appendTerminalInfo(info, player);
		return info;
	}

	/**
	 * Adds info about this trader to the {@link io.github.lightman314.lightmanscurrency.client.gui.widget.button.NetworkTraderButton NetworkTraderButton}'s tooltip.<br>
	 * Can be used to add useful info about how many trades are available, how many are in stock, etc.
	 */
	protected void appendTerminalInfo(@Nonnull List<Component> list, @Nullable Player player) { }

}
