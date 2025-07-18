package io.github.lightman314.lightmanscurrency.api.traders;

import java.util.*;
import java.util.function.*;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.api.misc.IPermissions;
import io.github.lightman314.lightmanscurrency.api.misc.ISidedObject;
import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyStorage;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.api.ownership.IOwnable;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.FakeOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.PlayerOwner;
import io.github.lightman314.lightmanscurrency.api.settings.ISaveableSettingsHolder;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsNode;
import io.github.lightman314.lightmanscurrency.api.settings.data.NodeSelections;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.stats.StatKey;
import io.github.lightman314.lightmanscurrency.api.stats.StatKeys;
import io.github.lightman314.lightmanscurrency.api.stats.StatTracker;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxCollector;
import io.github.lightman314.lightmanscurrency.api.taxes.TaxAPI;
import io.github.lightman314.lightmanscurrency.api.traders.menu.customer.ITraderScreen;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageScreen;
import io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.*;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.info.InfoSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.info.TraderInfoClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.info.core.TaxInfoClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.info.core.TraderLogClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.info.core.TraderStatsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core.*;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core.addons.MiscTabAddon;
import io.github.lightman314.lightmanscurrency.common.blockentity.variant.IVariantSupportingBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.data.types.TraderDataCache;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.TraderEjectionData;
import io.github.lightman314.lightmanscurrency.common.items.data.TraderItemData;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.providers.EasyMenuProvider;
import io.github.lightman314.lightmanscurrency.common.menus.validation.EasyMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.SimpleValidator;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.NullCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.*;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxable;
import io.github.lightman314.lightmanscurrency.api.misc.world.WorldPosition;
import io.github.lightman314.lightmanscurrency.api.taxes.reference.TaxableReference;
import io.github.lightman314.lightmanscurrency.api.taxes.reference.builtin.TaxableTraderReference;
import io.github.lightman314.lightmanscurrency.common.traders.rules.ITradeRuleHost;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import io.github.lightman314.lightmanscurrency.common.util.LookupHelper;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.ITraderBlock;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.api.ejection.IDumpable;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
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
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class TraderData implements ISidedObject, IDumpable, IUpgradeable, ITraderSource, ITradeRuleHost, ITaxable, IOwnable, ISaveableSettingsHolder, IPermissions {
	
	public static final int GLOBAL_TRADE_LIMIT = 100;
	public static final Predicate<Notification> LOGS_NORMAL_FILTER = n -> !n.getCategory().matches(NullCategory.INSTANCE);
	public static final Predicate<Notification> LOGS_SETTINGS_FILTER = n -> n.getCategory().matches(NullCategory.INSTANCE);

	private final Map<String,SettingsNode> settingsNodes = new HashMap<>();
	@Nullable
	@Override
	public SettingsNode getNode(String nodeKey) { return this.settingsNodes.get(nodeKey); }
	@Override
	public List<SettingsNode> getAllSettingNodes() {
		List<SettingsNode> nodes = new ArrayList<>(this.settingsNodes.values());
		nodes.sort(SettingsNode.SORTER);
		return new ArrayList<>(nodes);
	}

	@Override
	public void buildLoadContext(LoadContext.Builder builder) { builder.withOwner(this).withAllies(this.allies).withAllyPermissions(this.allyPermissions).withBlockedPermissions(this.getBlockedPermissions()); }

	private boolean canMarkDirty = false;
	public final TraderData allowMarkingDirty() { this.canMarkDirty = true; return this; }

    public final RegistryAccess registryAccess() { return LookupHelper.getRegistryAccess(); }

	public final TraderType<?> type;
	private long id = -1;
	public long getID() { return this.id; }
	public void setID(long id) { this.id = id; }

	@Nullable
	private PostTradeEvent latestTrade = null;

	/**
	 * Whether this trader is in a state that can be accessed through <b>any</b> means
	 */
	public boolean allowAccess() { return this.getState().allowAccess; }
	/**
	 * Whether the traders current state would allow the trader to be recovered with the <code>/lcadmin traderdata recover TRADER_ID</code> command`
	 */
	public boolean isRecoverable() { return this.getState().allowRecovery; }
	/**
	 * Whether the traders current state would make it possible for the block to be located in a world (if said chunk is loaded of course)
	 */
	public boolean hasWorldPosition() { return !this.worldPosition.isVoid() && this.getState().validateWorldPosition; }

	private boolean alwaysShowOnTerminal = false;
	public void setAlwaysShowOnTerminal() { this.alwaysShowOnTerminal = true; this.markDirty(this::saveShowOnTerminal); }
	public boolean shouldAlwaysShowOnTerminal() { return this.alwaysShowOnTerminal; }
	public boolean canShowOnTerminal() { return true; }
	public boolean showOnTerminal() {
		if(!this.allowAccess() || this.isInQuarantine()) //Hide from terminal if not accessible or if this trader is in a quarantined dimension
			return false;
		if(this.alwaysShowOnTerminal)
			return true;
		else
			return this.hasNetworkUpgrade();
	}
	
	protected final boolean hasNetworkUpgrade() { return UpgradeType.hasUpgrade(Upgrades.NETWORK, this.upgrades); }
	protected boolean allowVoidUpgrade() { return false; }
	protected final boolean shouldStoreGoods() { return !this.creative && !UpgradeType.hasUpgrade(Upgrades.VOID,this.upgrades); }

	public boolean readyForCustomers() { return this.hasValidTrade(); }
	
	private TraderState state = TraderState.NORMAL;
	
	public TraderState getState() { return this.state; }
	public void setState(TraderState state)
	{
		if(this.state == state)
			return;
		if(this.state == TraderState.PERSISTENT)
		{
			LightmansCurrency.LogError("Cannot change the state of a persistent trader!");
			return;
		}
		this.state = state;
		this.markDirty(this::saveState);
	}
	public void PickupTrader(Player player, boolean adminState)
	{
		if(this.isClient() || this.state != TraderState.NORMAL)
			return;
		if(!LCAdminMode.isAdminPlayer(player))
			adminState = false;
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{

			if(this.shouldRemove(server))
				return;
			TraderBlockEntity<?> be = this.getBlockEntity();
			if(be != null)
			{
				ItemStack result = be.PickupTrader(player, this);
				if(!result.isEmpty())
				{
					//Change trader state
					this.setState(adminState ? TraderState.ADMIN_HELD_AS_ITEM : TraderState.HELD_AS_ITEM);
					//Give the item this traders ID for future loading
					result.set(ModDataComponents.TRADER_ITEM_DATA,new TraderItemData(this.id));
					//Add the model variant to the item
					IVariantSupportingBlockEntity.copyDataToItem(this.blockVariant,this.blockVariantLocked,result);
					//Give the item to the player
					ItemHandlerHelper.giveItemToPlayer(player,result);
				}
			}
		}
	}
	public void OnTraderMoved(WorldPosition newPosition)
	{
		this.setState(TraderState.NORMAL);
		this.worldPosition = newPosition;
		this.markDirty(this::saveLevelData);
	}

	private boolean creative = false;
	public boolean isCreative() { return this.creative; }
	public void setCreative(boolean creative)
	{
		if(this.creative == creative)
			return;
		this.creative = creative;
		this.markDirty(this::saveCreativeSettings);
	}

	private boolean storeCreativeMoney = false;
	public boolean shouldStoreCreativeMoney() { return this.storeCreativeMoney; }
	public void setStoreCreativeMoney(boolean storeCreativeMoney)
	{
		if(this.storeCreativeMoney == storeCreativeMoney)
			return;
		this.storeCreativeMoney = storeCreativeMoney;
		this.markDirty(this::saveCreativeSettings);
	}

	public boolean canStoreMoney() { return !this.creative || this.storeCreativeMoney; }

	private boolean isClient = false;
	@Override
	public final TraderData flagAsClient() { return this.flagAsClient(true); }
	@Override
	public final TraderData flagAsClient(boolean isClient) { this.isClient = isClient; this.logger.flagAsClient(this); return this; }
	@Override
	public final TraderData flagAsClient(IClientTracker context) { return this.flagAsClient(context.isClient()); }
	public boolean isClient() { return this.isClient; }

	private final OwnerData owner = new OwnerData(this, () -> this.markDirty(this::saveOwner));
	
	@Override
	public final OwnerData getOwner() { return this.owner; }

	public final StatTracker statTracker = new StatTracker(() -> {},this);

	private final List<PlayerReference> allies = new ArrayList<>();
	public final List<PlayerReference> getAllies() { return new ArrayList<>(this.allies); }
	public final void overwriteAllies(List<PlayerReference> allies)
	{
		this.allies.clear();
		this.allies.addAll(allies);
		this.markDirty(this::saveAllySettings);
	}
	
	private final Map<String,Integer> allyPermissions = this.getDefaultAllyPermissions();
	public final Set<String> getAllyPermissionKeys() { return this.allyPermissions.keySet(); }
	public final Map<String,Integer> getAllyPermissionMap() { return new HashMap<>(this.allyPermissions); }
	public void overwriteAllyPermissions(Map<String,Integer> allyPermissions) {
		this.allyPermissions.clear();
        this.allyPermissions.putAll(allyPermissions);
		this.markDirty(this::saveAllyPermissionSettings);
	}
	
	private Map<String,Integer> getDefaultAllyPermissions() {
		Map<String,Integer> defaultValues = new HashMap<>();
		defaultValues.put(Permissions.OPEN_STORAGE, 1);
		defaultValues.put(Permissions.CHANGE_NAME, 1);
		defaultValues.put(Permissions.EDIT_TRADES, 1);
		defaultValues.put(Permissions.COLLECT_COINS, 0);
		defaultValues.put(Permissions.STORE_COINS, 0);
		defaultValues.put(Permissions.EDIT_TRADE_RULES, 1);
		defaultValues.put(Permissions.EDIT_SETTINGS, 1);
		defaultValues.put(Permissions.ADD_REMOVE_ALLIES, 0);
		defaultValues.put(Permissions.EDIT_PERMISSIONS, 0);
		defaultValues.put(Permissions.VIEW_LOGS, 1);
		defaultValues.put(Permissions.BREAK_TRADER, 0);
		defaultValues.put(Permissions.BANK_LINK, 0);
		defaultValues.put(Permissions.INTERACTION_LINK, 0);
		defaultValues.put(Permissions.TRANSFER_OWNERSHIP, 0);
		this.modifyDefaultAllyPermissions(defaultValues);
		return defaultValues;
	}
	
	protected void modifyDefaultAllyPermissions(Map<String,Integer> defaultValues) {}

	public List<String> getBlockedPermissions() { return ImmutableList.of(); }
	
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

	public final ImmutableList<String> persistentTraderBlockedPermissions() {
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
			this.markDirty(this::saveAllyPermissionSettings);
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
	public final List<Notification> getNotifications(boolean settingsView) { return this.getNotifications(settingsView ? LOGS_SETTINGS_FILTER : LOGS_NORMAL_FILTER); }
	public final List<Notification> getNotifications(Predicate<Notification> filter) { return this.logger.getNotifications(filter); }

	private String customName = "";
	public boolean hasCustomName() { return !this.customName.isBlank(); }
	public String getCustomName() { return this.customName; }
	public void setCustomName(String newName)
	{
		if(newName.equals(this.customName))
			return;
		this.customName = newName;
		this.markDirty(this::saveDisplaySettings);
	}
	
	public IconData getDisplayIcon() { return this.customIcon.isNull() ? this.getIcon() : this.customIcon; }

	public abstract IconData getIcon();

	@Override
	
	public MutableComponent getName() {
		if(this.hasCustomName())
			return EasyText.literal(this.customName);
		return this.getDefaultName();
	}
	
	public final MutableComponent getTitle() {
		if(this.creative)
			return this.getName();
		return LCText.GUI_TRADER_TITLE.get(this.getName(), this.owner.getName());
	}

	private IconData customIcon = IconData.Null();
	public IconData getCustomIcon() { return this.customIcon; }
	public void setCustomIcon(@Nullable IconData newIcon)
	{
		if(newIcon == null)
			newIcon = IconData.Null();
		if(newIcon.equals(this.customIcon))
			return;
		this.customIcon = newIcon;
		this.markDirty(this::saveDisplaySettings);
	}

	/**
	 * Method used to get the new {@link #customIcon} {@link IconData} when an item is used on the icon portion of the Display Settings tab.<br>
	 * For safety, you should override {@link #getIconForItem(ItemStack, IconData)} so that unexpected issues don't appear if I change how the data is saved, etc.
	 */
	public IconData getIconForItem(ItemStack stack) { return this.getIconForItem(stack,this.customIcon); }
	/**
	 * Can be overridden by child traders to make special icons from certain items<br>
	 * (i.e. an icon that renders lava if the item stack is a lava bucket, etc.)<br><br>
	 * By default, returns a simple item icon for the given item
	 */
	protected IconData getIconForItem(ItemStack stack,IconData originalIcon) { return IconData.of(stack.copyWithCount(1)); }

	private Item traderBlock;
	@Nullable
	public Item getTraderBlock() { return this.traderBlock; }
	protected MutableComponent getDefaultName() {
		if(this.traderBlock != null)
			return EasyText.literal(new ItemStack(this.traderBlock).getHoverName().getString());
		return LCText.GUI_TRADER_DEFAULT_NAME.get();
	}
	private ResourceLocation blockVariant;
	@Nullable
	public ResourceLocation getTraderBlockVariant() { return this.blockVariant; }
	public void setTraderBlockVariant(@Nullable ResourceLocation blockVariant, boolean variantLocked) {
		this.blockVariant = blockVariant;
		this.blockVariantLocked = variantLocked;
		//Manually write sync packet, so we can write "NoTraderVariant" data for sync purposes
		CompoundTag packet = new CompoundTag();
		if(this.blockVariant != null)
			packet.putString("TraderVariant",this.blockVariant.toString());
		else
			packet.putBoolean("NoTraderVariant",true);
		packet.putBoolean("TraderVariantLocked",this.blockVariantLocked);
		this.markDirty(packet);
	}
	private boolean blockVariantLocked = false;

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
		if(amount == null)
			return MoneyValue.empty();
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
			//Then pay taxes
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
	public void CollectStoredMoney(Player player)
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
			{
				MoneyValue paid = tax.CalculateAndPayTaxes(this, amount);
				MoneyValue temp = paidCache.addValue(paid);
				if(!temp.isEmpty())
					paidCache = temp;
			}
		}
		return paidCache;
	}

	protected boolean isInQuarantine() {
		ResourceKey<Level> level = this.worldPosition.getDimension();
		return level != null && QuarantineAPI.IsDimensionQuarantined(level);
	}
	private boolean linkedToBank = false;
	public boolean isLinkedToBank() { return this.linkedToBank; }
	public void setLinkedToBank(boolean newValue)
	{
		if(newValue == this.linkedToBank)
			return;
		this.linkedToBank = newValue;
		if(this.linkedToBank)
		{
			if(this.isInQuarantine())
				this.linkedToBank = false;
			else
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
			if(this.linkedToBank)
				this.markDirty(this::saveBankSettings);
		}
	}
	public boolean canLinkBankAccount()
	{
		BankReference reference = this.owner.getValidOwner().asBankReference();
		return !this.isInQuarantine() && reference != null && reference.get() != null;
	}
	
	public boolean hasBankAccount() { return this.getBankAccount() != null; }
	@Nullable
	public BankReference getBankReference() {
		if(this.linkedToBank && !this.isInQuarantine())
			return this.owner.getValidOwner().asBankReference();
		return null;
	}
	@Nullable
	public IBankAccount getBankAccount() {
		BankReference reference = this.getBankReference();
		if(reference != null)
			return reference.get();
		return null;
	}
	
	private SimpleContainer upgrades;
	@Override
	public Container getUpgrades() { return this.upgrades; }
	@Override
	public final boolean allowUpgrade(UpgradeType type) {
		if(type == Upgrades.NETWORK && !this.showOnTerminal() && this.canShowOnTerminal())
			return true;
		if(type == Upgrades.VOID && this.allowVoidUpgrade())
			return true;
		if(this instanceof IFlexibleOfferTrader && type == Upgrades.TRADE_OFFERS)
			return true;
		return this.allowAdditionalUpgradeType(type);
	}
	protected abstract boolean allowAdditionalUpgradeType(UpgradeType type);
	
	private List<TradeRule> rules = new ArrayList<>();
	
	@Override
	public List<TradeRule> getRules() { return Lists.newArrayList(this.rules); }
	protected void validateRules() { TradeRule.ValidateTradeRuleList(this.rules, this); }

	private boolean alwaysShowSearchBox = false;
	public final boolean alwaysShowSearchBox() { return this.alwaysShowSearchBox; }
	public final void setAlwaysShowSearchBox(Player player, boolean newVal)
	{
		if(this.hasPermission(player,Permissions.EDIT_SETTINGS) && this.alwaysShowSearchBox != newVal)
		{
			this.alwaysShowSearchBox = newVal;
			this.markDirty(this::saveMiscSettings);
		}
	}
	@Override
	public boolean showSearchBox() { return this.alwaysShowSearchBox; }

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
	public int validTradeCount() { return (int)this.getTradeData().stream().filter(TradeData::isValid).count(); }
	public boolean hasValidTrade() { return this.getTradeData().stream().anyMatch(TradeData::isValid); }
	public int tradesWithStock() {
		TradeContext context = TradeContext.createStorageMode(this);
		return (int)this.getTradeData().stream().filter(t -> t.isValid() && t.hasStock(context)).count();
	}
	public boolean anyTradeHasStock()
	{
		TradeContext context = TradeContext.createStorageMode(this);
		return this.getTradeData().stream().anyMatch(t -> t.isValid() && t.hasStock(context));
	}
	public boolean supportsMultiPriceEditing() { return true; }

	private int acceptableTaxRate = 99;
	public final int getAcceptableTaxRate() { return this.acceptableTaxRate; }
	public void setAcceptableTaxRate(int acceptableTaxRate) { this.acceptableTaxRate = MathUtil.clamp(acceptableTaxRate,0,99); }
	private final List<Long> ignoredTaxCollectors = new ArrayList<>();
	public List<Long> getIgnoredTaxCollectors() { return new ArrayList<>(this.ignoredTaxCollectors); }
	public void setIgnoredTaxCollectors(List<Long> ignoredTaxCollectors) {
		this.ignoredTaxCollectors.clear();
		this.ignoredTaxCollectors.addAll(ignoredTaxCollectors);
	}
	private boolean ignoreAllTaxes = false;
	public boolean ShouldIgnoreAllTaxes() { return this.ignoreAllTaxes; }
	public void setIgnoreAllTaxes(boolean ignoreAllTaxes) { this.ignoreAllTaxes = ignoreAllTaxes; }
	public boolean ShouldIgnoreTaxEntryOnly(ITaxCollector entry) { return this.ignoredTaxCollectors.contains(entry.getID()); }
	private boolean AllowTaxEntry(ITaxCollector entry) { return !this.ShouldIgnoreTaxEntry(entry); }
	public boolean ShouldIgnoreTaxEntry(ITaxCollector entry) { return this.ShouldIgnoreAllTaxes() || this.ShouldIgnoreTaxEntryOnly(entry); }

	private WorldPosition worldPosition = WorldPosition.VOID;
	public ResourceKey<Level> getLevel() { return this.worldPosition.getDimension(); }
	public BlockPos getPos() { return this.worldPosition.getPos(); }

	/**
	 * Gets the in-game Block Entity of this Trader Block (if one exists)
	 */
	@Nullable
	public TraderBlockEntity<?> getBlockEntity()
	{
		Level level = LightmansCurrency.getProxy().getDimension(this.isClient,this.getLevel());
		if(level != null && level.isLoaded(this.worldPosition.getPos()) && level.getBlockEntity(this.worldPosition.getPos()) instanceof TraderBlockEntity<?> be && be.getTraderID() == this.id)
			return be;
		return null;
	}
	
	@Override
	public TaxableReference getReference() { return new TaxableTraderReference(this.getID()); }
	
	@Override
	public WorldPosition getWorldPosition() { return this.worldPosition; }

	public final List<ITaxCollector> getApplicableTaxes() { return TaxAPI.API.GetTaxCollectorsFor(this).stream().filter(this::AllowTaxEntry).toList(); }
	public final List<ITaxCollector> getPossibleTaxes() { return TaxAPI.API.GetPotentialTaxCollectorsFor(this); }
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
	
	protected TraderData(TraderType<?> type) {
		this.type = type;
		this.upgrades = new SimpleContainer(5);
		this.upgrades.addListener(this::upgradesChanged);
		this.registerNodes(n -> this.settingsNodes.put(n.key,n));
	}
	
	protected TraderData(TraderType<?> type, Level level, BlockPos pos) {
		this(type);
		this.worldPosition = WorldPosition.ofLevel(level, pos);
		this.traderBlock = level.getBlockState(this.worldPosition.getPos()).getBlock().asItem();
	}

	protected void registerNodes(Consumer<SettingsNode> builder)
	{
		//Core settings
		builder.accept(new OwnerSettings(this));
		builder.accept(new AllySettings(this));
		builder.accept(new PermissionSettings(this));
		//Misc Settings
		builder.accept(new CreativeSettings(this));
		builder.accept(new DisplaySettings(this));
		builder.accept(new BankSettings(this));
		builder.accept(new TaxSettings(this));
		//Trader Trade Rule Settings
		builder.accept(new TraderRuleSettings(this));
	}

	@Override
	public void loadSettings(Player player, SavedSettingData data, NodeSelections selections) {
		ISaveableSettingsHolder.super.loadSettings(player, data, selections);
		//Mark the entire trader as changed after loading settings
		this.markDirty(this.save(this.registryAccess()));
	}

	private void upgradesChanged(Container container)
	{
		if(container == this.upgrades)
		{
			this.markDirty(this::saveUpgrades);
			if(this instanceof IFlexibleOfferTrader fot)
				fot.refactorTrades();
		}
	}
	
	private String persistentID = "";
	public boolean isPersistent() { return !this.persistentID.isEmpty(); }
	public String getPersistentID() { return this.persistentID; }
	public void makePersistent(long id, String persistentID) {
		this.state = TraderState.PERSISTENT;
		this.id = id;
		this.persistentID = persistentID;
		this.creative = true;
		this.alwaysShowOnTerminal = true;
	}
	
	protected final void markDirty(CompoundTag updateData) {
		if(this.isClient || !this.canMarkDirty)
			return;
		updateData.putLong("ID", this.id);
		TraderDataCache.TYPE.get(false).markTraderDirty(updateData);
	}

	protected final void markDirty(Consumer<CompoundTag> updateWriter) {
		if(this.isClient || !this.canMarkDirty)
			return;
		CompoundTag updateData = new CompoundTag();
		updateWriter.accept(updateData);
		this.markDirty(updateData);
	}

	protected final void markDirty(BiConsumer<CompoundTag,HolderLookup.Provider> updateWriter) {
		if(this.isClient || !this.canMarkDirty)
			return;
		CompoundTag updateData = new CompoundTag();
		updateWriter.accept(updateData,LookupHelper.getRegistryAccess());
		this.markDirty(updateData);
	}
	
	public final CompoundTag save(HolderLookup.Provider lookup) {
		
		CompoundTag compound = new CompoundTag();
		
		compound.putString("Type", this.type.toString());
		compound.putLong("ID", this.id);

		this.saveState(compound);
		this.saveLevelData(compound);
		this.saveTraderItem(compound);
		this.saveOwner(compound,lookup);
		this.saveAllySettings(compound);
		this.saveAllyPermissionSettings(compound);
		this.saveShowOnTerminal(compound);
		this.saveRules(compound,lookup);
		this.saveUpgrades(compound, lookup);
		this.saveStoredMoney(compound);
		this.saveLogger(compound, lookup);
		this.saveStatistics(compound, lookup);

		//Settings
		this.saveCreativeSettings(compound);
		this.saveDisplaySettings(compound,lookup);
		this.saveBankSettings(compound);
		this.saveMiscSettings(compound);
		this.saveTaxSettings(compound);
		
		//Save persistent trader id
		if(!this.persistentID.isEmpty())
			compound.putString("PersistentTraderID", this.persistentID);

		//Save trader-specific data
		this.saveAdditional(compound, lookup);
		
		return compound;
		
	}

	private void saveState(CompoundTag compoundTag) {
		compoundTag.putString("State", this.state.toString());
	}

	public final void saveLevelData(CompoundTag compound) {
		compound.put("Location", this.worldPosition.save());
	}

	private void saveTraderItem(CompoundTag compound) {
		if(this.traderBlock != null)
			compound.putString("TraderBlock", BuiltInRegistries.ITEM.getKey(this.traderBlock).toString());
		if(this.blockVariant != null)
			compound.putString("TraderVariant",this.blockVariant.toString());
		if(this.blockVariantLocked)
			compound.putBoolean("TraderVariantLocked",true);
	}
	
	protected final void saveOwner(CompoundTag compound,HolderLookup.Provider lookup) { compound.put("OwnerData", this.owner.save(lookup)); }

	protected final void saveShowOnTerminal(CompoundTag compound) { compound.putBoolean("AlwaysShowOnTerminal", this.alwaysShowOnTerminal); }

	protected final void saveRules(CompoundTag compound, HolderLookup.Provider lookup) { TradeRule.saveRules(compound, this.rules, "RuleData", lookup); }
	
	protected final void saveUpgrades(CompoundTag compound, HolderLookup.Provider lookup) { InventoryUtil.saveAllItems("Upgrades", compound, this.upgrades, lookup); }
	
	protected final void saveStoredMoney(CompoundTag compound) { compound.put("StoredMoney", this.storedMoney.save()); }

	protected final void saveLogger(CompoundTag compound, HolderLookup.Provider lookup) { compound.put("Logger", this.logger.save(lookup)); }

	protected final void saveStatistics(CompoundTag tag, HolderLookup.Provider lookup) {
		tag.put("Stats", this.statTracker.save(lookup));
	}

	private void saveCreativeSettings(CompoundTag tag) {
		tag.putBoolean("Creative",this.creative);
		tag.putBoolean("StoreCreativeMoney",this.storeCreativeMoney);
	}

	private void saveAllySettings(CompoundTag compound) {
		PlayerReference.saveList(compound, this.allies, "Allies");
	}

	private void saveAllyPermissionSettings(CompoundTag compound) {
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

	private void saveDisplaySettings(CompoundTag tag, HolderLookup.Provider lookup)
	{
		tag.putString("Name",this.customName);
		tag.put("CustomIcon",this.customIcon.save(lookup));
	}

	private void saveBankSettings(CompoundTag tag)
	{
		tag.putBoolean("LinkedToBank",this.linkedToBank);
	}

	protected final void saveMiscSettings(CompoundTag compound) {
		compound.putBoolean("NotificationsEnabled", this.notificationsEnabled);
		compound.putBoolean("ChatNotifications", this.notificationsToChat);
		compound.putInt("TeamNotifications", this.teamNotificationLevel);
		compound.putBoolean("AlwaysShowSearchBox", this.alwaysShowSearchBox);
	}

	protected final void saveTaxSettings(CompoundTag compound) {
		compound.putInt("AcceptableTaxRate", this.acceptableTaxRate);
		compound.putBoolean("IgnoreAllTaxCollectors", this.ignoreAllTaxes);
		compound.putLongArray("IgnoreTaxCollectors", this.ignoredTaxCollectors);
	}
	
	protected abstract void saveTrades(CompoundTag compound, HolderLookup.Provider lookup);
	
	protected abstract void saveAdditional(CompoundTag compound, HolderLookup.Provider lookup);
	
	public void markTradesDirty() { this.markDirty(this::saveTrades); }
	
	public void markTradeRulesDirty() { this.markDirty(this::saveRules); }

	public void markStatsDirty() { this.markDirty(this::saveStatistics); }

	public final JsonObject saveToJson(String id, String ownerName, HolderLookup.Provider lookup) throws Exception
	{
		if(!this.canMakePersistent())
			throw new Exception("Trader of type '" + this.type.toString() + "' cannot be saved to JSON!");
		
		JsonObject json = new JsonObject();
		
		json.addProperty("Type", this.type.toString());
		json.addProperty("ID", id);
		json.addProperty("Name", this.hasCustomName() ? this.customName : "Trader");
		json.addProperty("OwnerName", ownerName);
		
		JsonArray ruleData = TradeRule.saveRulesToJson(this.rules, lookup);
		if(!ruleData.isEmpty())
			json.add("Rules", ruleData);
		
		this.saveAdditionalToJson(json, lookup);
		return json;
	}
	
	protected abstract void saveAdditionalToJson(JsonObject json, HolderLookup.Provider lookup);
	
	public final void load(CompoundTag compound, HolderLookup.Provider lookup) {
		
		if(compound.contains("ID", Tag.TAG_LONG))
			this.setID(compound.getLong("ID"));
		
		//Load persistent trader id
		if(compound.contains("PersistentTraderID"))
			this.persistentID = compound.getString("PersistentTraderID");

		//Trader State
		if(compound.contains("State"))
			this.state = EnumUtil.enumFromString(compound.getString("State"),TraderState.values(),TraderState.NORMAL);

		//Position (Old Style)
		if(compound.contains("WorldPos") && compound.contains("Level"))
		{
			CompoundTag posTag = compound.getCompound("WorldPos");
			BlockPos pos = new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
			ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, VersionUtil.parseResource(compound.getString("Level")));
			this.worldPosition = WorldPosition.of(dimension, pos);
		}
		else if(compound.contains("Location"))
			this.worldPosition = WorldPosition.load(compound.getCompound("Location"));
		
		if(compound.contains("TraderBlock"))
		{
			try {
				this.traderBlock = BuiltInRegistries.ITEM.get(VersionUtil.parseResource(compound.getString("TraderBlock")));
			}catch (Throwable ignored) {}
		}

		if(compound.contains("TraderVariant"))
		{
			try { this.blockVariant = VersionUtil.parseResource(compound.getString("TraderVariant"));
			} catch (Throwable ignored) {}
		}
		else if(compound.contains("NoTraderVariant"))
			this.blockVariant = null;

		if(compound.contains("TraderVariantLocked"))
			this.blockVariantLocked = compound.getBoolean("TraderVariantLocked");
		
		if(compound.contains("OwnerData", Tag.TAG_COMPOUND))
			this.owner.load(compound.getCompound("OwnerData"),lookup);
		
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
		
		if(compound.contains("AlwaysShowOnTerminal"))
			this.alwaysShowOnTerminal = compound.getBoolean("AlwaysShowOnTerminal");

		if(compound.contains("RuleData"))
			this.rules = TradeRule.loadRules(compound, "RuleData", this, lookup);
		
		if(compound.contains("Upgrades"))
		{
			this.upgrades = InventoryUtil.loadAllItems("Upgrades", compound, 5, lookup);
			this.upgrades.addListener(this::upgradesChanged);
		}
		
		if(compound.contains("StoredMoney"))
			this.storedMoney.safeLoad(compound, "StoredMoney");
		
		if(compound.contains("Logger"))
			this.logger.load(compound.getCompound("Logger"), lookup);

		if(compound.contains("Stats"))
			this.statTracker.load(compound.getCompound("Stats"), lookup);

		//Settings
		//Creative Settings
		if(compound.contains("Creative"))
			this.creative = compound.getBoolean("Creative");
		if(compound.contains("StoreCreativeMoney"))
			this.storeCreativeMoney = compound.getBoolean("StoreCreativeMoney");

		//Display Settings
		if(compound.contains("Name"))
			this.customName = compound.getString("Name");
		if(compound.contains("CustomIcon"))
		{
			this.customIcon = IconData.load(compound.getCompound("CustomIcon"),lookup);
			if(this.customIcon == null)
				this.customIcon = IconData.Null();
		}

		//Bank Settings
		if(compound.contains("LinkedToBank"))
			this.linkedToBank = compound.getBoolean("LinkedToBank");

		//Tax Settings
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

		//Misc Settings
		if(compound.contains("NotificationsEnabled"))
			this.notificationsEnabled = compound.getBoolean("NotificationsEnabled");
		if(compound.contains("ChatNotifications"))
			this.notificationsToChat = compound.getBoolean("ChatNotifications");
		if(compound.contains("TeamNotifications"))
			this.teamNotificationLevel = compound.getInt("TeamNotifications");
		if(compound.contains("AlwaysShowSearchBox"))
			this.alwaysShowSearchBox = compound.getBoolean("AlwaysShowSearchBox");

		//Load trader-specific data
		this.loadAdditional(compound,lookup);
		
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
	
	protected abstract void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup);
	
	public final void loadFromJson(JsonObject json, HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException {
		this.owner.SetOwner(FakeOwner.of(GsonHelper.getAsString(json, "OwnerName", "Server")));
		
		if(json.has("Name"))
			this.customName = GsonHelper.getAsString(json, "Name");
		
		if(json.has("Rules"))
			this.rules = TradeRule.Parse(GsonHelper.getAsJsonArray(json, "Rules"), this, lookup);
		
		this.loadAdditionalFromJson(json, lookup);
	}
	
	protected abstract void loadAdditionalFromJson(JsonObject json, HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException;
	
	public final CompoundTag savePersistentData(HolderLookup.Provider lookup) {
		CompoundTag compound = new CompoundTag();
		//Save persistent trade rule data
		TradeRule.savePersistentData(compound, this.rules, "RuleData", lookup);
		//Save additional persistent data
		this.saveAdditionalPersistentData(compound, lookup);
		return compound;
	}
	
	protected abstract void saveAdditionalPersistentData(CompoundTag compound, HolderLookup.Provider lookup);
	
	public final void loadPersistentData(CompoundTag compound, HolderLookup.Provider lookup) {
		//Load persistent trade rule data
		TradeRule.loadPersistentData(compound, this.rules, "RuleData", lookup);
		//Load additional persistent data
		this.loadAdditionalPersistentData(compound, lookup);
	}
	
	protected abstract void loadAdditionalPersistentData(CompoundTag compound, HolderLookup.Provider lookup);

	@Deprecated(since = "2.1.2.3")
	public void openTraderMenu(Player player) { this.openTraderMenu(player, SimpleValidator.NULL); }
	public void openTraderMenu(Player player, MenuValidator validator) {
		if(player instanceof ServerPlayer)
			player.openMenu(this.getTraderMenuProvider(validator), EasyMenu.encoder(this.getMenuDataWriter(), validator));
	}
	
	protected MenuProvider getTraderMenuProvider(MenuValidator validator) { return new TraderMenuProvider(this.id, validator); }

	private record TraderMenuProvider(long traderID, MenuValidator validator) implements EasyMenuProvider {

		@Override
		public AbstractContainerMenu createMenu(int windowID, Inventory inventory, Player player) { return new TraderMenu(windowID, inventory, this.traderID, validator); }

	}

	@Deprecated(since = "2.1.2.3")
	public void openStorageMenu(Player player) { this.openStorageMenu(player, SimpleValidator.NULL); }
	public void openStorageMenu(Player player, MenuValidator validator) {
		if(!this.hasPermission(player, Permissions.OPEN_STORAGE))
			return;
		if(player instanceof ServerPlayer sp)
			player.openMenu(this.getTraderStorageMenuProvider(validator), EasyMenu.encoder(this.getMenuDataWriter(), validator));
	}
	
	protected MenuProvider getTraderStorageMenuProvider(MenuValidator validator)  { return new TraderStorageMenuProvider(this.id, validator); }

	private record TraderStorageMenuProvider(long traderID, MenuValidator validator) implements EasyMenuProvider {

		@Override
		public AbstractContainerMenu createMenu(int windowID, Inventory inventory, Player player) { return new TraderStorageMenu(windowID, inventory, this.traderID, this.validator); }

	}

	public Consumer<RegistryFriendlyByteBuf> getMenuDataWriter() { return b -> b.writeLong(this.id); }
	
	public PreTradeEvent runPreTradeEvent(TradeData trade, TradeContext context)
	{
		PreTradeEvent event = new PreTradeEvent(trade, context);
		
		//Trader trade rules
		for(TradeRule rule : this.rules)
		{
			if(rule.isActive())
				rule.beforeTrade(event);
		}	
		
		//Trades trade rules
		trade.beforeTrade(event);
		
		//Public posting
		NeoForge.EVENT_BUS.post(event);
		
		return event;
	}

	public TradeCostEvent runTradeCostEvent(TradeData trade, TradeContext context)
	{
		return runTradeCostEvent(trade, context, TradeRule.getBaseCost(trade,context));
	}
	public TradeCostEvent runTradeCostEvent(TradeData trade, TradeContext context, MoneyValue baseCost)
	{
		TradeCostEvent event = new TradeCostEvent(trade, context, baseCost);
		
		//Trader trade rules
		for(TradeRule rule : this.rules)
		{
			if(rule.isActive())
				rule.tradeCost(event);
		}
		
		//Trades trade rules
		trade.tradeCost(event);
		
		//Public posting
		NeoForge.EVENT_BUS.post(event);
		
		return event;
	}

	public void runPostTradeEvent(TradeData trade, TradeContext context, MoneyValue cost, MoneyValue taxesPaid)
	{
		PostTradeEvent event = new PostTradeEvent(trade, context, cost, taxesPaid);
		
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
		NeoForge.EVENT_BUS.post(event);

		this.latestTrade = event;
	}

	//Content drops
	public final List<ItemStack> getContents(Level level, BlockPos pos, @Nullable BlockState state, boolean dropBlock) {
		ItemStack blockStack = ItemStack.EMPTY;
		if(dropBlock)
		{
			Block block = state != null ? state.getBlock() : null;
			if(block != null)
				blockStack = new ItemStack(block);
			if(block instanceof ITraderBlock b)
				blockStack = b.getDropBlockItem(level, pos, state);
			if(blockStack.isEmpty())
				LightmansCurrency.LogWarning("Block drop for trader is empty!");
			IVariantSupportingBlockEntity.copyDataToItem(this.blockVariant,this.blockVariantLocked,blockStack);
		}
		return this.getContents(blockStack);
	}

	public final List<ItemStack> getContents(ItemStack item)
	{
		List<ItemStack> results = new ArrayList<>();
		if(!item.isEmpty())
			results.add(item);

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
			List<ItemStack> items = value.onBlockBroken(this.owner);
			if(items != null)
				results.addAll(items);
		}

		//Add trader-specific drops
		this.getAdditionalContents(results);

		return results;
	}

	
	@Override
	public EjectionData buildEjectionData(Level level, BlockPos pos, @Nullable BlockState state) {
		ItemStack item = ItemStack.EMPTY;
		if(state == null)
		{
			if(this.traderBlock != null)
				item = new ItemStack(this.traderBlock);
		}
		else
			item = new ItemStack(state.getBlock());
		if(!item.isEmpty())
		{
			item.set(ModDataComponents.TRADER_ITEM_DATA,new TraderItemData(this.getID()));
			IVariantSupportingBlockEntity.copyDataToItem(this.blockVariant,this.blockVariantLocked,item);
		}
		//Set State to Ejected
		this.setState(TraderState.EJECTED);
		return new TraderEjectionData(this.getID(),item);
	}

	protected abstract void getAdditionalContents(List<ItemStack> results);
	
	public static TraderData Deserialize(boolean isClient, CompoundTag compound, HolderLookup.Provider lookup)
	{
		if(compound.contains("Type"))
		{
			String type = compound.getString("Type");
			TraderType<?> traderType = TraderAPI.API.GetTraderType(VersionUtil.parseResource(type));
			if(traderType != null)
				return traderType.load(isClient, compound, lookup);
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
	
	public static TraderData Deserialize(JsonObject json, HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException
	{
		String thisType = GsonHelper.getAsString(json, "Type");
		TraderType<?> traderType = TraderAPI.API.GetTraderType(VersionUtil.parseResource(thisType));
		if(traderType != null)
			return traderType.loadFromJson(json,lookup);
		throw new JsonSyntaxException("Trader type '" + thisType + "' is undefined.");
	}
	
	//Network stuff
	public boolean shouldRemove(MinecraftServer server) {
		if(!this.hasWorldPosition())
			return false;
		TraderBlockEntity<?> be = this.getBlockEntity();
		return be != null && be.getTraderID() != this.id;
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

    public final TradeResult TryExecuteTrade(TradeContext context, int tradeIndex)
	{
		this.latestTrade = null;
		if(this.exceedsAcceptableTaxRate())
			return TradeResult.FAIL_TAX_EXCEEDED_LIMIT;
		TradeResult result = this.ExecuteTrade(context,tradeIndex);
		if(result.isSuccess())
		{
			//Increment trades executed
			this.incrementStat(StatKeys.Traders.TRADES_EXECUTED,1);
			//Mark Stats as changed
			this.markStatsDirty();
		}
		return result;
	}

	
	public final FullTradeResult TryExecuteTradeWithResults(TradeContext context, int tradeIndex)
	{
		TradeResult result = this.TryExecuteTrade(context,tradeIndex);
		if(result.isSuccess() && this.latestTrade != null)
			return FullTradeResult.success(this.latestTrade);
		return FullTradeResult.failure(result);
	}

	protected abstract TradeResult ExecuteTrade(TradeContext context, int tradeIndex);
	
	public void addInteractionSlots(List<InteractionSlotData> interactionSlots) {}
	
	public abstract boolean canMakePersistent();

	
	public Predicate<TradeData> getStorageTradeFilter(ITraderStorageMenu menu) { return t -> true; }

	public abstract void initStorageTabs(ITraderStorageMenu menu);

	public void handleSettingsChange(Player player, LazyPacketData message)
	{
		if(message.contains("ChangePlayerOwner"))
		{
			if(this.hasPermission(player, Permissions.TRANSFER_OWNERSHIP))
			{
				PlayerReference newOwnerPlayer = PlayerReference.of(this.isClient, message.getString("ChangePlayerOwner"));
				if(newOwnerPlayer != null)
				{
					Owner newOwner = PlayerOwner.of(newOwnerPlayer);
					Owner oldOwner = this.owner.getValidOwner();
					if(oldOwner.matches(newOwner))
					{
						LightmansCurrency.LogDebug("Set owner player to the same player who already owns this machine.");
						return;
					}

					this.owner.SetOwner(newOwner);
					this.linkedToBank = false;
					this.markDirty(this::saveBankSettings);
					//Send Notification
					this.pushLocalNotification(new ChangeOwnerNotification(PlayerReference.of(player), newOwner, oldOwner));
				}
			}
		}
		if(message.contains("ChangeOwner"))
		{
			if(this.hasPermission(player, Permissions.TRANSFER_OWNERSHIP))
			{
				Owner newOwner = message.getOwner("ChangeOwner");
				Owner oldOwner = this.owner.getValidOwner();
				if(newOwner != null && !oldOwner.matches(newOwner))
				{
					this.owner.SetOwner(newOwner);
					this.linkedToBank = false;
					this.markDirty(this::saveBankSettings);
					//Send Notification
					this.pushLocalNotification(new ChangeOwnerNotification(PlayerReference.of(player),newOwner,oldOwner));
				}
			}
		}
		if(message.contains("AddAlly"))
		{
			if(this.hasPermission(player, Permissions.ADD_REMOVE_ALLIES))
			{
				PlayerReference newAlly = PlayerReference.load(message.getNBT("AddAlly"));
				if(newAlly != null && !PlayerReference.isInList(this.allies, newAlly.id))
				{
					this.allies.add(newAlly);
					this.markDirty(this::saveAllySettings);

					this.pushLocalNotification(new AddRemoveAllyNotification(PlayerReference.of(player), true, newAlly));
				}
			}
		}
		if(message.contains("RemoveAlly"))
		{
			if(this.hasPermission(player, Permissions.ADD_REMOVE_ALLIES))
			{
				PlayerReference oldAlly = PlayerReference.load(message.getNBT("RemoveAlly"));
				if(PlayerReference.removeFromList(this.allies, oldAlly))
				{
					this.markDirty(this::saveAllySettings);

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
			if(this.hasPermission(player,Permissions.CHANGE_NAME))
			{
				String newName = message.getString("ChangeName");
				if(newName.length() > 32)
					newName = newName.substring(0,32);
				if(newName.equals(this.customName))
					return;
				this.customName = newName;
				this.pushLocalNotification(ChangeSettingNotification.simple(PlayerReference.of(player),LCText.DATA_ENTRY_TRADER_NAME.get(),this.customName));
				this.markDirty(this::saveDisplaySettings);
			}
		}
		if(message.contains("ChangeIcon"))
		{
			if(this.hasPermission(player,Permissions.CHANGE_NAME))
			{
				IconData newIcon = IconData.load(message.getNBT("ChangeIcon"),message.lookup);
				if(newIcon == null) //Force the icon to be non-null
					newIcon = IconData.Null();
				if(this.customIcon.equals(newIcon))
					return;
				this.customIcon = newIcon;
				this.pushLocalNotification(ChangeSettingNotification.dumb(PlayerReference.of(player),LCText.DATA_ENTRY_TRADER_ICON.get()));
				this.markDirty(this::saveDisplaySettings);
			}
		}
		if(message.contains("MakeCreative"))
		{
			if(LCAdminMode.isAdminPlayer(player))
			{
				boolean newValue = message.getBoolean("MakeCreative");
				if(newValue != this.creative)
				{
					this.creative = newValue;
					this.pushLocalNotification(new ChangeCreativeNotification(PlayerReference.of(player),this.creative));
					this.markDirty(this::saveCreativeSettings);
				}
			}
		}
		if(message.contains("StoreCreativeMoney"))
		{
			if(LCAdminMode.isAdminPlayer(player))
			{
				boolean newValue = message.getBoolean("StoreCreativeMoney");
				if(newValue != this.storeCreativeMoney)
				{
					this.storeCreativeMoney = newValue;
					this.pushLocalNotification(ChangeSettingNotification.simple(PlayerReference.of(player), LCText.DATA_ENTRY_STORE_CREATIVE_MONEY.get(),this.storeCreativeMoney));
					this.markDirty(this::saveCreativeSettings);
				}
			}
		}
		if(message.contains("LinkToBankAccount"))
		{
			if(this.hasPermission(player,Permissions.BANK_LINK))
			{
				boolean newValue = message.getBoolean("LinkToBankAccount");
				if(newValue != this.linkedToBank)
				{
					this.setLinkedToBank(newValue);
					//Extra check here, as this change may be overridden by quarantine or other such rules
					if(this.linkedToBank == newValue)
					{
						this.pushLocalNotification(ChangeSettingNotification.simple(PlayerReference.of(player),LCText.DATA_ENTRY_TRADER_BANK_LINK.get(),this.linkedToBank));
						this.markDirty(this::saveBankSettings);
					}
				}
			}
		}
		if(message.contains("Notifications"))
		{
			if(this.hasPermission(player, Permissions.EDIT_SETTINGS))
			{
				boolean enable = message.getBoolean("Notifications");
				if(this.notificationsEnabled != enable)
				{
					this.notificationsEnabled = enable;
					this.markDirty(this::saveMiscSettings);

					this.pushLocalNotification(ChangeSettingNotification.simple(PlayerReference.of(player), EasyText.literal("Notifications"), this.notificationsEnabled));
				}
			}
		}
		if(message.contains("NotificationsToChat"))
		{
			if(this.hasPermission(player, Permissions.EDIT_SETTINGS))
			{
				boolean enable = message.getBoolean("NotificationsToChat");
				if(this.notificationsToChat != enable)
				{
					this.notificationsToChat = enable;
					this.markDirty(this::saveMiscSettings);

					this.pushLocalNotification(ChangeSettingNotification.simple(PlayerReference.of(player), EasyText.literal("NotificationsToChat"), this.notificationsToChat));
				}
			}
		}
		if(message.contains("TeamNotificationLevel"))
		{
			if(this.hasPermission(player, Permissions.EDIT_SETTINGS))
			{
				int level = message.getInt("TeamNotificationLevel");
				if(this.teamNotificationLevel != level)
				{
					this.teamNotificationLevel = level;
					this.markDirty(this::saveMiscSettings);

					this.pushLocalNotification(ChangeSettingNotification.simple(PlayerReference.of(player), EasyText.literal("TeamNotificationLevel"), this.teamNotificationLevel));
				}
			}
		}
		if(message.contains("AlwaysShowSearchBox"))
		{
			if(this.hasPermission(player, Permissions.EDIT_SETTINGS))
			{
				boolean newVal = message.getBoolean("AlwaysShowSearchBox");
				if(this.alwaysShowSearchBox != newVal)
				{
					this.alwaysShowSearchBox = newVal;
					this.markDirty(this::saveMiscSettings);

					this.pushLocalNotification(ChangeSettingNotification.simple(PlayerReference.of(player), EasyText.literal("AlwaysShowSearchBox"), this.alwaysShowSearchBox));
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

				this.pushLocalNotification(ChangeSettingNotification.advanced(PlayerReference.of(player), EasyText.literal("AcceptableTaxRate"), newRate, this.acceptableTaxRate));
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
				this.pushLocalNotification(ChangeSettingNotification.simple(PlayerReference.of(player), EasyText.literal("IgnoreAllTaxes"), this.ignoreAllTaxes));
				this.markDirty(this::saveTaxSettings);
			}
		}
		if(message.contains("PickupTrader"))
		{
			if(this.hasPermission(player,Permissions.BREAK_TRADER))
				this.PickupTrader(player,message.getBoolean("PickupTrader"));
			else
				Permissions.PermissionWarning(player,"Pickup Trader", Permissions.BREAK_TRADER);
		}
		if(message.contains("DeleteNotification"))
		{
			if(this.hasPermission(player,Permissions.TRANSFER_OWNERSHIP))
			{
				int index = message.getInt("DeleteNotification");
				Predicate<Notification> filter = message.getBoolean("SettingsView") ? LOGS_SETTINGS_FILTER : LOGS_NORMAL_FILTER;
				this.logger.deleteNotification(filter,index);
				this.markDirty(this::saveLogger);
			}
		}
		if(message.contains("AcceptTaxCollector"))
		{
			if(this.hasPermission(player,Permissions.EDIT_SETTINGS))
			{
				ITaxCollector entry = TaxAPI.API.GetTaxCollector(this,message.getLong("AcceptTaxCollector"));
				if(entry != null && entry.IsInArea(this))
					entry.AcceptTaxable(this);
			}
		}
		if(message.contains("ForceIgnoreTaxCollector"))
		{
			if(LCAdminMode.isAdminPlayer(player))
			{
				ITaxCollector entry = TaxAPI.API.GetTaxCollector(this,message.getLong("ForceIgnoreTaxCollector"));
				if(entry != null && entry.IsInArea(this))
				{
					if(this.ignoredTaxCollectors.contains(entry.getID()))
						return;
					this.ignoredTaxCollectors.add(entry.getID());
					this.markDirty(this::saveTaxSettings);
				}
			}
		}
		if(message.contains("PardonTaxCollector"))
		{
			if(this.hasPermission(player,Permissions.EDIT_SETTINGS))
			{
				ITaxCollector entry = TaxAPI.API.GetTaxCollector(this,message.getLong("PardonTaxCollector"));
				if(entry != null && this.ignoredTaxCollectors.contains(entry.getID()))
				{
					this.ignoredTaxCollectors.remove(entry.getID());
					this.markDirty(this::saveTaxSettings);
				}
			}
		}
		if(message.contains("ClearStats"))
		{
			if(this.hasPermission(player,Permissions.EDIT_SETTINGS))
			{
				boolean fullClear = message.getBoolean("ClearStats");
				this.statTracker.clear(fullClear);
			}
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public final List<SettingsSubTab> getSettingsTabs(TraderSettingsClientTab tab) {
		//Set up defailt tabs
		List<SettingsSubTab> tabs = Lists.newArrayList(new NameTab(tab),new CreativeSettingsTab(tab),new PersistentTab(tab),new AllyTab(tab),new PermissionsTab(tab),new MiscTab(tab),new TaxSettingsTab(tab));
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
	public final List<InfoSubTab> getInfoTabs(TraderInfoClientTab tab) {
		List<InfoSubTab> tabs = Lists.newArrayList(new TraderLogClientTab(tab,false),new TraderStatsClientTab(tab),new TraderLogClientTab(tab,true),new TaxInfoClientTab(tab));
		this.addInfoTabs(tab,tabs);
		return tabs;
	}

	@OnlyIn(Dist.CLIENT)
	protected void addInfoTabs(TraderInfoClientTab tab, List<InfoSubTab> tabs) { }

	@OnlyIn(Dist.CLIENT)
	public void onScreenInit(ITraderScreen screen, Consumer<Object> addWidget) { }

	@OnlyIn(Dist.CLIENT)
	public void onStorageScreenInit(ITraderStorageScreen screen, Consumer<Object> addWidget) { }

	@OnlyIn(Dist.CLIENT)
	public List<MiscTabAddon> getMiscTabAddons() { return new ArrayList<>(); }

	public final void pushLocalNotification(Notification notification)
	{
		if(this.isClient)
			return;
		this.logger.addNotification(notification);
		this.markDirty(this::saveLogger);
	}
	
	public final void pushNotification(Supplier<Notification> notificationSource) {
		//Notifications are disabled
		if(this.isClient)
			return;
		
		//Push to local notification
		this.pushLocalNotification(notificationSource.get());
		
		if(!this.notificationsEnabled)
			return;

		//Push notifications is now handled by owner
		this.owner.getValidOwner().pushNotification(notificationSource, this.teamNotificationLevel, this.notificationsToChat);
		
	}

	public final <T> void incrementStat(StatKey<?,T> key, T addValue)
	{
		this.statTracker.incrementStat(key,addValue);
		this.owner.getValidOwner().incrementStat(key,addValue);
	}
	
	public final TraderCategory getNotificationCategory() {
		return new TraderCategory(this.traderBlock != null ? this.traderBlock : ModItems.TRADING_CORE.get(), this.getName(), this.id);
	}

	
	public final List<TraderData> getTraders() { return this.allowAccess() ? Lists.newArrayList(this) : new ArrayList<>(); }
	public final boolean isSingleTrader() { return true; }
	
	public static MenuProvider getTraderMenuProvider(BlockPos traderSourcePosition, MenuValidator validator) { return new TraderMenuProviderBlock(traderSourcePosition, validator); }

	private record TraderMenuProviderBlock(BlockPos traderSourcePosition, MenuValidator validator) implements EasyMenuProvider {

		@Override
		public AbstractContainerMenu createMenu(int windowID, Inventory inventory,  Player player) { return new TraderMenu.TraderMenuBlockSource(windowID, inventory, this.traderSourcePosition, this.validator); }

	}

	public static MenuProvider getTraderMenuForAllNetworkTraders(MenuValidator validator) { return new TraderMenuAllNetworkProvider(validator); }

	private record TraderMenuAllNetworkProvider(MenuValidator validator) implements EasyMenuProvider{
		@Nullable
		@Override
		public AbstractContainerMenu createMenu(int windowID, Inventory inventory, Player player) {
			return new TraderMenu.TraderMenuAllNetwork(windowID,inventory,this.validator);
		}
	}

	
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
	protected void appendTerminalInfo(List<Component> list, @Nullable Player player) { }

	public int getTerminalTextColor()
	{
		if(!this.hasValidTrade())
			return 0xFF0000;
		if(this.isCreative())
			return 0x00FF00;
		if(!this.anyTradeHasStock())
			return 0xFFAA00;
		return 0x404040;
	}

}
