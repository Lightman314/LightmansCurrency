package io.github.lightman314.lightmanscurrency.api.traders;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.easy_data.EasyData;
import io.github.lightman314.lightmanscurrency.api.easy_data.IEasyDataHost;
import io.github.lightman314.lightmanscurrency.api.easy_data.categories.DataCategories;
import io.github.lightman314.lightmanscurrency.api.easy_data.types.BoolData;
import io.github.lightman314.lightmanscurrency.api.easy_data.types.StringData;
import io.github.lightman314.lightmanscurrency.api.easy_data.complex.types.IconDataData;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.api.ejection.IDumpable;
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
import io.github.lightman314.lightmanscurrency.api.stats.StatKey;
import io.github.lightman314.lightmanscurrency.api.stats.StatKeys;
import io.github.lightman314.lightmanscurrency.api.stats.StatTracker;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxCollector;
import io.github.lightman314.lightmanscurrency.api.taxes.TaxAPI;
import io.github.lightman314.lightmanscurrency.api.traders.easy_data.TraderNotificationReplacers;
import io.github.lightman314.lightmanscurrency.api.traders.menu.customer.ITraderScreen;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core.*;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.data.types.TraderDataCache;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.TraderEjectionData;
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
import io.github.lightman314.lightmanscurrency.common.traders.rules.ITradeRuleHost;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.Registries;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.ITraderBlock;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.AddRemoveAllyNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeAllyPermissionNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeOwnerNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeSettingNotification;
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
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class TraderData implements ISidedObject, IDumpable, IUpgradeable, ITraderSource, ITradeRuleHost, ITaxable, IOwnable, IEasyDataHost, IPermissions {

	public static final int GLOBAL_TRADE_LIMIT = 100;

	private final List<EasyData<?>> easyData = new ArrayList<>();

	@Override
	public void registerData(EasyData<?> data) {
		if(!this.easyData.contains(data))
			this.easyData.add(data);
	}

	private boolean canMarkDirty = false;
	public final TraderData allowMarkingDirty() { this.canMarkDirty = true; return this; }

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
	protected final boolean shouldStoreGoods() { return !this.creative.get() && !UpgradeType.hasUpgrade(Upgrades.VOID,this.upgrades); }

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
					CompoundTag itemTag = result.getOrCreateTag();
					itemTag.putLong("StoredTrader",this.id);
					//Add the model variant to the item
					if(this.blockVariant != null)
						IVariantBlock.setItemVariant(result,this.blockVariant);
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

	public final BoolData creative = BoolData.builder(false).host(this)
			.key("creative").tagKey("Creative")
			.name(LCText.DATA_ENTRY_CREATIVE.get())
			.category(DataCategories.Traders.CREATIVE)
			.notificationReplacer(TraderNotificationReplacers.CREATIVE_NOTIFICATION)
			.build();
	public boolean isCreative() { return this.creative.get(); }

	public final BoolData storeCreativeMoney = BoolData.builder(false).host(this)
			.key("store_money_in_creative").tagKey("StoreCreativeMoney")
			.name(LCText.DATA_ENTRY_STORE_CREATIVE_MONEY.get())
			.category(DataCategories.Traders.CREATIVE)
			.build();

	public boolean canStoreMoney() { return !this.creative.get() || this.storeCreativeMoney.get(); }

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

	private final Map<String,Integer> allyPermissions = this.getDefaultAllyPermissions();

	private Map<String,Integer> getDefaultAllyPermissions() {
		Map<String,Integer> defaultValues = new HashMap<>();
		defaultValues.put(Permissions.OPEN_STORAGE, 1);
		defaultValues.put(Permissions.EDIT_TRADES, 1);
		defaultValues.put(Permissions.EDIT_TRADE_RULES, 1);
		defaultValues.put(Permissions.EDIT_SETTINGS, 1);
		defaultValues.put(Permissions.CHANGE_NAME, 1);
		defaultValues.put(Permissions.VIEW_LOGS, 1);

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
	public final void deleteNotification(Player player, int notificationIndex)
	{
		if(this.hasPermission(player, Permissions.TRANSFER_OWNERSHIP))
		{
			this.logger.deleteNotification(notificationIndex);
			this.markDirty(this::saveLogger);
		}
	}

	public final StringData customName = StringData.builder().host(this)
			.key("name").tagKey("Name")
			.category(DataCategories.Traders.DISPLAY)
			.name(LCText.DATA_ENTRY_TRADER_NAME.get())
			.build();
	public boolean hasCustomName() { return !this.customName.get().isBlank(); }
	
	public IconData getDisplayIcon() { return this.customIcon.get().isNull() ? this.getIcon() : this.customIcon.get(); }

	public abstract IconData getIcon();

	@Override
	
	public MutableComponent getName() {
		if(this.hasCustomName())
			return EasyText.literal(this.customName.get());
		return this.getDefaultName();
	}

	public final MutableComponent getTitle() {
		if(this.creative.get())
			return this.getName();
		return LCText.GUI_TRADER_TITLE.get(this.getName(), this.owner.getName());
	}

	public final IconDataData customIcon = IconDataData.builder().host(this)
			.key("trader_icon").tagKey("CustomIcon")
			.name(LCText.DATA_ENTRY_TRADER_ICON.get())
			.category(DataCategories.Traders.DISPLAY)
			.build();


	/**
	 * Method used to get the new {@link #customIcon} {@link IconData} when an item is used on the icon portion of the Display Settings tab<br>
	 * For safety, you should override {@link #getIconForItem(ItemStack,IconData)} so that unexpected issues don't appear if I change how the data is saved, etc.
	 */
	public IconData getIconForItem(ItemStack stack) { return this.getIconForItem(stack,this.customIcon.get()); }
	/**
	 * Can be overridden by child traders to make special icons from certain items<br>
	 * (i.e. an icon that renders lava if the item stack is a lava bucket, etc.)<br><br>
	 * By default, returns a simple item icon for the given item
	 */
	protected IconData getIconForItem(ItemStack stack, IconData original) { return IconData.of(stack.copyWithCount(1)); }

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
	public void setTraderBlockVariant(@Nullable ResourceLocation blockVariant)
	{
		this.blockVariant = blockVariant;
		this.markDirty(this::saveTraderItem);
	}

	@Override
	public void onDataChanged(EasyData<?> data) { this.markDirty(data::write); }

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

	public final BoolData linkedToBank = BoolData.builder(false).host(this)
			.key("linked_to_bank").tagKey("LinkedToBank")
			.name(LCText.DATA_ENTRY_TRADER_BANK_LINK.get())
			.category(DataCategories.Traders.BANK)
			.build();
	public boolean canLinkBankAccount()
	{
		BankReference reference = this.owner.getValidOwner().asBankReference();
		return !this.isInQuarantine() && reference != null && reference.get() != null;
	}
	private void onBankLinkChanged(boolean newState)
	{
		if(newState)
		{
			if(this.isInQuarantine())
				this.linkedToBank.set(false);
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
					this.linkedToBank.set(false);
			}
		}
	}

	public boolean hasBankAccount() { return this.getBankAccount() != null; }
	public IBankAccount getBankAccount() {
		if(this.linkedToBank.get() && !this.isInQuarantine())
		{
			BankReference reference = this.owner.getValidOwner().asBankReference();
			if(reference != null)
				return reference.get();
		}
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
	private final List<Long> ignoredTaxCollectors = new ArrayList<>();
	private boolean ignoreAllTaxes = false;
	public boolean ShouldIgnoreAllTaxes() { return this.ignoreAllTaxes; }
	public boolean ShouldIgnoreTaxEntryOnly(ITaxCollector entry) { return this.ignoredTaxCollectors.contains(entry.getID()); }
	public void FlagTaxEntryToIgnore(TaxEntry entry, Player player) {
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
	public void PardonTaxEntry(TaxEntry entry)
	{
		if(this.ignoredTaxCollectors.contains(entry.getID()))
		{
			this.ignoredTaxCollectors.remove(entry.getID());
			this.markDirty(this::saveTaxSettings);
		}
	}
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
		//Add linked to bank listener
		this.linkedToBank.addListener(this::onBankLinkChanged);
	}

	protected TraderData(TraderType<?> type, Level level, BlockPos pos) {
		this(type);
		this.worldPosition = WorldPosition.ofLevel(level, pos);
		this.traderBlock = level.getBlockState(this.worldPosition.getPos()).getBlock().asItem();
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
		this.creative.set(true);
		this.alwaysShowOnTerminal = true;
	}

	protected final void markDirty(CompoundTag updateData) {
		if(this.isClient || !this.canMarkDirty)
			return;
		updateData.putLong("ID", this.id);
		TraderDataCache.TYPE.get(false).markTraderDirty(updateData);
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

		this.saveState(compound);
		this.saveLevelData(compound);
		this.saveTraderItem(compound);
		this.saveOwner(compound);
		this.saveAllies(compound);
		this.saveAllyPermissions(compound);
		this.saveShowOnTerminal(compound);
		this.saveRules(compound);
		this.saveUpgrades(compound);
		this.saveStoredMoney(compound);
		this.saveLogger(compound);
		this.saveMiscSettings(compound);
		this.saveTaxSettings(compound);
		this.saveStatistics(compound);

		//Save persistent trader id
		if(!this.persistentID.isEmpty())
			compound.putString("PersistentTraderID", this.persistentID);

		//Save Easy Data
		for(EasyData<?> data : this.easyData)
			data.write(compound);

		//Save trader-specific data
		this.saveAdditional(compound);

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
			compound.putString("TraderBlock", ForgeRegistries.ITEMS.getKey(this.traderBlock).toString());
		if(this.blockVariant != null)
			compound.putString("TraderVariant",this.blockVariant.toString());
		else
			compound.putBoolean("NoTraderVariant",true);
	}

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

	protected final void saveShowOnTerminal(CompoundTag compound) { compound.putBoolean("AlwaysShowOnTerminal", this.alwaysShowOnTerminal); }

	protected final void saveRules(CompoundTag compound) { TradeRule.saveRules(compound, this.rules, "RuleData"); }

	protected final void saveUpgrades(CompoundTag compound) { InventoryUtil.saveAllItems("Upgrades", compound, this.upgrades); }

	protected final void saveStoredMoney(CompoundTag compound) { compound.put("StoredMoney", this.storedMoney.save()); }

	protected final void saveLogger(CompoundTag compound) { compound.put("Logger", this.logger.save()); }

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

	protected final void saveStatistics(CompoundTag tag) {
		tag.put("Stats", this.statTracker.save());
	}

	protected abstract void saveTrades(CompoundTag compound);

	protected abstract void saveAdditional(CompoundTag compound);

	public void markTradesDirty() { this.markDirty(this::saveTrades); }

	public void markTradeRulesDirty() { this.markDirty(this::saveRules); }

	public void markStatsDirty() { this.markDirty(this::saveStatistics); }

	public final JsonObject saveToJson(String id, String ownerName) throws Exception
	{
		if(!this.canMakePersistent())
			throw new Exception("Trader of type '" + this.type.toString() + "' cannot be saved to JSON!");

		JsonObject json = new JsonObject();

		json.addProperty("Type", this.type.toString());
		json.addProperty("ID", id);
		json.addProperty("Name", this.hasCustomName() ? this.customName.get() : "Trader");
		json.addProperty("OwnerName", ownerName);

		JsonArray ruleData = TradeRule.saveRulesToJson(this.rules);
		if(!ruleData.isEmpty())
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
				this.traderBlock = ForgeRegistries.ITEMS.getValue(VersionUtil.parseResource(compound.getString("TraderBlock")));
			}catch (Throwable ignored) {}
		}

		if(compound.contains("TraderVariant"))
		{
			try { this.blockVariant = VersionUtil.parseResource(compound.getString("TraderVariant"));
			} catch (Throwable ignored) {}
		}
		else if(compound.contains("NoTraderVariant"))
			this.blockVariant = null;

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

		if(compound.contains("AlwaysShowOnTerminal"))
			this.alwaysShowOnTerminal = compound.getBoolean("AlwaysShowOnTerminal");

		if(compound.contains("RuleData"))
			this.rules = TradeRule.loadRules(compound, "RuleData", this);

		if(compound.contains("Upgrades"))
		{
			this.upgrades = InventoryUtil.loadAllItems("Upgrades", compound, 5);
			this.upgrades.addListener(this::upgradesChanged);
		}

		if(compound.contains("StoredMoney"))
			this.storedMoney.safeLoad(compound, "StoredMoney");

		if(compound.contains("Logger"))
			this.logger.load(compound.getCompound("Logger"));

		if(compound.contains("NotificationsEnabled"))
			this.notificationsEnabled = compound.getBoolean("NotificationsEnabled");
		if(compound.contains("ChatNotifications"))
			this.notificationsToChat = compound.getBoolean("ChatNotifications");
		if(compound.contains("TeamNotifications"))
			this.teamNotificationLevel = compound.getInt("TeamNotifications");
		if(compound.contains("AlwaysShowSearchBox"))
			this.alwaysShowSearchBox = compound.getBoolean("AlwaysShowSearchBox");

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

		if(compound.contains("Stats"))
			this.statTracker.load(compound.getCompound("Stats"));

		//Load Easy Data
		for(EasyData<?> data : this.easyData)
			data.read(compound);

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
		this.owner.SetOwner(FakeOwner.of(GsonHelper.getAsString(json, "OwnerName", "Server")));

		if(json.has("Name"))
			this.customName.set(GsonHelper.getAsString(json, "Name"));

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
	public void openTraderMenu(Player player, MenuValidator validator) {
		if(player instanceof ServerPlayer sp)
			NetworkHooks.openScreen(sp, this.getTraderMenuProvider(validator), EasyMenu.encoder(this.getMenuDataWriter(), validator));
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
			NetworkHooks.openScreen(sp, this.getTraderStorageMenuProvider(validator), EasyMenu.encoder(this.getMenuDataWriter(), validator));
	}

	protected MenuProvider getTraderStorageMenuProvider(MenuValidator validator)  { return new TraderStorageMenuProvider(this.id, validator); }

	private record TraderStorageMenuProvider(long traderID, MenuValidator validator) implements EasyMenuProvider {

		@Override
		public AbstractContainerMenu createMenu(int windowID, Inventory inventory, Player player) { return new TraderStorageMenu(windowID, inventory, this.traderID, this.validator); }

	}

	public Consumer<FriendlyByteBuf> getMenuDataWriter() { return b -> b.writeLong(this.id); }

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
		MinecraftForge.EVENT_BUS.post(event);

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
		MinecraftForge.EVENT_BUS.post(event);

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
		MinecraftForge.EVENT_BUS.post(event);

		this.latestTrade = event;

	}

	
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction relativeSide) { return LazyOptional.empty(); }

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
			else if(this.blockVariant != null)
				IVariantBlock.setItemVariant(blockStack,this.blockVariant);
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
			//Give the item this traders ID for future loading
			CompoundTag itemTag = item.getOrCreateTag();
			itemTag.putLong("StoredTrader",this.id);
			if(this.blockVariant != null)
				IVariantBlock.setItemVariant(item,this.blockVariant);
		}
		//Set State to Ejected
		this.setState(TraderState.EJECTED);
		return new TraderEjectionData(this.getID(),item);
	}

	protected abstract void getAdditionalContents(List<ItemStack> results);

	public static TraderData Deserialize(boolean isClient, CompoundTag compound)
	{
		if(compound.contains("Type"))
		{
			String type = compound.getString("Type");
			TraderType<?> traderType = TraderAPI.API.GetTraderType(VersionUtil.parseResource(type));
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
		TraderType<?> traderType = TraderAPI.API.GetTraderType(VersionUtil.parseResource(thisType));
		if(traderType != null)
			return traderType.loadFromJson(json);
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

	/**
	 * Should not be called directly. Call TryExecuteTrade when possible!
	 */
	protected abstract TradeResult ExecuteTrade(TradeContext context, int tradeIndex);

	public void addInteractionSlots(List<InteractionSlotData> interactionSlots) {}

	public abstract boolean canMakePersistent();

	public Predicate<TradeData> getStorageDisplayFilter(ITraderStorageMenu menu) { return t -> true; }

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
					this.linkedToBank.set(false);
					//Send Notification
					this.pushLocalNotification(new ChangeOwnerNotification(PlayerReference.of(player), newOwner, oldOwner));
				}
			}
		}
		if(message.contains("ChangeOwner"))
		{
			if(this.hasPermission(player, Permissions.TRANSFER_OWNERSHIP))
			{
				Owner newOwner = Owner.load(message.getNBT("ChangeOwner"));
				Owner oldOwner = this.owner.getValidOwner();
				if(newOwner != null && !oldOwner.matches(newOwner))
				{
					this.owner.SetOwner(newOwner);
					this.linkedToBank.set(false);
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
					this.markDirty(this::saveAllies);

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
			this.customName.trySet(player,message.getString("ChangeName"));
		}
		if(message.contains("ChangeIcon"))
		{
			IconData newIcon = IconData.load(message.getNBT("ChangeIcon"));
			if(newIcon == null) //Force the icon to be non-nnull
				newIcon = IconData.Null();
			this.customIcon.trySet(player,newIcon);
		}
		if(message.contains("MakeCreative"))
		{
			this.creative.trySet(player,message.getBoolean("MakeCreative"));
		}
		if(message.contains("StoreCreativeMoney"))
		{
			this.storeCreativeMoney.trySet(player,message.getBoolean("StoreCreativeMoney"));
		}
		if(message.contains("LinkToBankAccount"))
		{
			this.linkedToBank.trySet(player,message.getBoolean("LinkToBankAccount"));
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
	}

	@OnlyIn(Dist.CLIENT)
	public final List<SettingsSubTab> getSettingsTabs(TraderSettingsClientTab tab) {
		//Set up defailt tabs
		List<SettingsSubTab> tabs = Lists.newArrayList(new NameTab(tab), new CreativeSettingsTab(tab),new PersistentTab(tab), new AllyTab(tab), new PermissionsTab(tab), new MiscTab(tab), new TaxSettingsTab(tab));
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
	public void onScreenInit(ITraderScreen screen, Consumer<Object> addWidget) { }

	@OnlyIn(Dist.CLIENT)
	public void onStorageScreenInit(ITraderStorageScreen screen, Consumer<Object> addWidget) { }

	@Nullable
	@Override
	public Consumer<Notification> dataChangeNotifier() { return this::pushLocalNotification; }

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
