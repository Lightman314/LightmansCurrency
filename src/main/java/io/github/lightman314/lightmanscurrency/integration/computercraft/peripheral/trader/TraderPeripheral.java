package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader;

import com.google.common.base.Predicates;
import com.mojang.datafixers.util.Either;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaValues;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.core.util.ArgumentHelpers;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.stats.StatType;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.attachments.builtin.ExternalAuthorizationAttachment;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.AddRemoveAllyNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeAllyPermissionNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeNameNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeSettingNotification;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.integration.computercraft.PeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.AccessTrackingPeripheral;
import io.github.lightman314.lightmanscurrency.util.DebugUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class TraderPeripheral<BE extends TraderBlockEntity<T>,T extends TraderData> extends AccessTrackingPeripheral {

    public static final String BASE_TYPE = "lc_trader";

    protected final Either<BE,Long> source;
    public TraderPeripheral(BE be) { this.source = Either.left(be); }
    public TraderPeripheral(T trader) { this.source = Either.right(trader.getID()); }

    public static IPeripheral createSimple(TraderBlockEntity<TraderData> be) { return new Simple(be); }
    public static IPeripheral createSimple(TraderData trader) { return new Simple(trader); }

    private final Consumer<TradeEvent.PreTradeEvent> preTradeEventListener = this::preTradeEvent;
    private final Consumer<TradeEvent.PostTradeEvent> postTradeEventListener = this::postTradeEvent;

    protected int getPermissionLevel(IComputerAccess computer,String permission)
    {
        String id = this.getComputerID(computer);
        if(id == null)
            return 0;
        TraderData trader = this.safeGetTrader();
        if(trader == null || !trader.hasAttachment(ExternalAuthorizationAttachment.TYPE))
            return 0;
        //Deny blocked permissions early
        if(trader.getBlockedPermissions().contains(permission))
            return 0;
        ExternalAuthorizationAttachment.AccessLevel access = trader.getAttachment(ExternalAuthorizationAttachment.TYPE).getAccessLevel(id);
        return switch (access) {
            case NONE -> 0;
            case ALLY -> trader.getAllyPermissionMap().getOrDefault(permission, 0);
            case ADMIN -> Integer.MAX_VALUE;
        };
    }
    protected boolean hasPermissions(IComputerAccess computer,String permission) { return this.getPermissionLevel(computer,permission) > 0; }

    @Nullable
    protected final BE getBlockEntity() {
        AtomicReference<BE> result = new AtomicReference<>(null);
        this.source.ifLeft(result::set);
        return result.get();
    }

    @Nullable
    public T safeGetTrader() {
        AtomicReference<T> result = new AtomicReference<>(null);
        this.source.ifLeft(be -> {
            if(be.isRemoved())
                return;
            result.set(be.getTraderData());
        });
        this.source.ifRight(id -> {
            try { result.set((T)TraderAPI.getApi().GetTrader(false,id));
            } catch (Exception ignored) { }
        });
        return result.get();
    }

    protected T getTrader() throws LuaException {
        if(!this.stillValid())
            throw new LuaException("An unexpected error occurred trying to get the traders data!");
        T trader = this.safeGetTrader();
        if(trader == null)
            throw new LuaException("An unexpected error occurred trying to get the traders data!");
        return trader;
    }

    @Nullable
    protected abstract IPeripheral wrapTrade(TradeData trade) throws LuaException;

    @Override
    public Set<String> getAdditionalTypes() { return Set.of(BASE_TYPE); }

    @Override
    public boolean equals(@Nullable IPeripheral peripheral) {
        if(peripheral instanceof TraderPeripheral<?,?> other)
            return other.source.equals(this.source) && super.equals(peripheral);
        return false;
    }

    @Override
    protected void onAttachment(IComputerAccess computer) {
        TraderData trader = this.safeGetTrader();
        if(trader != null && trader.hasAttachment(ExternalAuthorizationAttachment.TYPE))
            trader.getAttachment(ExternalAuthorizationAttachment.TYPE).flagAttemptedAccess(this.getComputerID(computer));
    }

    @Override
    protected void onFirstAttachment() {
        super.onFirstAttachment();
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,this.preTradeEventListener);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,this.postTradeEventListener);
    }

    @Override
    protected void onLastDetachment() {
        super.onLastDetachment();
        NeoForge.EVENT_BUS.unregister(this.preTradeEventListener);
        NeoForge.EVENT_BUS.unregister(this.postTradeEventListener);
    }

    public boolean isValid() {
        try {
            this.getTrader();
            return true;
        } catch (LuaException e) { return false; }
    }

    public long getID() throws LuaException { return this.getTrader().getID(); }

    public boolean isVisibleOnNetwork() throws LuaException { return this.getTrader().showOnTerminal(); }

    public boolean isCreative() throws LuaException { return this.getTrader().isCreative(); }

    public boolean isPersistent() throws LuaException { return this.getTrader().isPersistent(); }

    public LCLuaTable getOwner() throws LuaException {
        TraderData trader = this.getTrader();
        Owner owner = trader.getOwner().getValidOwner();
        return LCLuaTable.fromTag(owner.save(trader.registryAccess()));
    }

    public String getOwnerName() throws LuaException { return this.getTrader().getOwner().getName().getString(); }

    public LCLuaTable getStats() throws LuaException
    {
        LCLuaTable table = new LCLuaTable();
        TraderData trader = this.getTrader();
        for(String statKey : trader.statTracker.getKeys())
        {
            StatType.Instance<?,?> entry = trader.statTracker.getStat(statKey);
            if(entry == null)
                continue;
            Object display = entry.getDisplay();
            Object result = display.toString();
            if(display instanceof Component text)
                result = text.getString();
            if(display instanceof Number || display instanceof Boolean)
                result = display;
            table.put(statKey,result);
        }
        return table;
    }

    public String[] getAllies() throws LuaException {
        return this.getTrader().getAllies().stream().map(p -> p.getName(false)).toArray(String[]::new);
    }

    public boolean addAlly(IComputerAccess computer,IArguments args) throws LuaException
    {
        if(this.hasPermissions(computer,Permissions.ADD_REMOVE_ALLIES))
        {
            TraderData trader = this.getTrader();
            PlayerReference player = PlayerReference.of(false,args.getString(0));
            if(player == null)
                return false;
            List<PlayerReference> allies = trader.getAllies();
            if(PlayerReference.addToList(allies,player))
            {
                trader.overwriteAllies(allies);
                trader.pushLocalNotification(new AddRemoveAllyNotification(this.getFakePlayer(computer),true,player));
                return true;
            }
        }
        return false;
    }

    public boolean removeAlly(IComputerAccess computer,IArguments args) throws LuaException
    {
        if(this.hasPermissions(computer,Permissions.ADD_REMOVE_ALLIES))
        {
            TraderData trader = this.getTrader();
            PlayerReference player = PlayerReference.of(false,args.getString(0));
            if(player == null)
                return false;
            List<PlayerReference> allies = trader.getAllies();
            if(PlayerReference.removeFromList(allies,player))
            {
                trader.overwriteAllies(allies);
                trader.pushLocalNotification(new AddRemoveAllyNotification(this.getFakePlayer(computer),false,player));
            }
        }
        return false;
    }

    public LCLuaTable getAllyPermissions() throws LuaException{
        T trader = this.getTrader();
        LCLuaTable table = new LCLuaTable();
        //No permissions if the trader is persistent
        if(trader.isPersistent())
            return table;
        trader.getAllyPermissionMap().forEach((key,level) -> {
            if(!trader.getBlockedPermissions().contains(key))
                table.put(key,level);
        });
        return table;
    }

    public int getAllyPermissionLevel(IArguments args) throws LuaException {
        String permission = args.getString(0);
        TraderData trader = this.getTrader();
        //Block permissions as applicable
        if(trader.isPersistent() || trader.getBlockedPermissions().contains(permission))
            return 0;
        return trader.getAllyPermissionMap().getOrDefault(permission,0);
    }

    public boolean setAllyPermissionLevel(IComputerAccess computer,IArguments args) throws LuaException
    {
        String permission = args.getString(0);
        int level = args.getInt(1);
        ArgumentHelpers.assertBetween(level,0,Integer.MAX_VALUE,"Permission Level is not in range (%s)");
        if(this.hasPermissions(computer,Permissions.EDIT_PERMISSIONS))
        {
            TraderData trader = this.getTrader();
            Map<String,Integer> permissions = trader.getAllyPermissionMap();
            int oldLevel = permissions.getOrDefault(permission,0);
            if(oldLevel != level)
            {
                permissions.put(permission,level);
                trader.overwriteAllyPermissions(permissions);
                trader.pushLocalNotification(new ChangeAllyPermissionNotification(this.getFakePlayer(computer),permission,level,oldLevel));
                return true;
            }
        }
        return false;
    }

    public int getPlayerPermissionLevel(IArguments args) throws LuaException {
        TraderData trader = this.getTrader();
        PlayerReference player = PlayerReference.of(false,args.getString(0));
        if(player == null)
            return 0;
        return trader.getPermissionLevel(player,args.getString(1));
    }

    public int getMyPermissionLevel(IComputerAccess computer, IArguments args) throws LuaException { return this.getPermissionLevel(computer,args.getString(0)); }

    public LCLuaTable[] getLogs(IArguments args) throws LuaException {
        Predicate<Notification> filter = Predicates.alwaysTrue();
        int limit = 0;
        Tuple<Boolean,Boolean> argsLoaded = new Tuple<>(false,false);
        if(args.count() > 0)
        {
            if(args.count() > 2)
                throw new LuaException("Too many arguments, expected a max of 2");
            for(int i = 0; i < 2 && i < args.count(); ++i)
            {
                Object arg = args.get(i);
                if(arg instanceof Number num)
                {
                    if(argsLoaded.getA())
                        throw LuaValues.badArgument(i,"boolean","number");
                    limit = num.intValue();
                    argsLoaded.setA(true);
                }
                else if(arg instanceof Boolean boo)
                {
                    if(argsLoaded.getB())
                        throw LuaValues.badArgument(i,"number","boolean");
                    filter = boo ? TraderData.LOGS_SETTINGS_FILTER : TraderData.LOGS_NORMAL_FILTER;
                    argsLoaded.setB(true);
                }
                else
                    throw LuaValues.badArgument(i,"number or boolean",arg.getClass().getSimpleName());
            }
        }
        List<Notification> notifications = this.getTrader().getNotifications(filter);
        //View all if limit is 0 (or less)
        if(limit <= 0)
            limit = notifications.size();
        List<LCLuaTable> result = new ArrayList<>();
        for(int i = 0; i < limit && i < notifications.size(); ++i)
        {
            Notification not = notifications.get(i);
            LCLuaTable entry = new LCLuaTable();
            entry.put("Timestamp",not.getTimeStamp());
            entry.put("Count",not.getCount());
            List<String> lines = new ArrayList<>();
            for(Component line : not.getMessageLines())
                lines.add(line.getString());
            entry.put("Text",lines.toArray(String[]::new));
            result.add(entry);
        }
        return result.toArray(LCLuaTable[]::new);
    }

    public String getName() throws LuaException { return this.getTrader().getName().getString(); }

    public boolean setName(IComputerAccess computer,IArguments args) throws LuaException
    {
        String newName = args.getString(0);
        if(this.hasPermissions(computer,Permissions.CHANGE_NAME))
        {
            if(newName.length() > 16)
                newName = newName.substring(0,16);
            TraderData trader = this.getTrader();
            String oldName = trader.getCustomName();
            if(!oldName.equals(newName))
            {
                trader.setCustomName(newName);
                trader.pushLocalNotification(new ChangeNameNotification(this.getFakePlayer(computer),newName,oldName));
                return true;
            }
        }
        return false;
    }

    public LCLuaTable getIcon() throws LuaException {
        TraderData t = this.getTrader();
        return LCLuaTable.fromTag(t.getCustomIcon().save(t.registryAccess()));
    }

    public boolean setIcon(IComputerAccess computer,IArguments args) throws LuaException {
        if(this.hasPermissions(computer,Permissions.CHANGE_NAME))
        {
            TraderData trader = this.getTrader();
            Map<?,?> table = args.getTable(0);
            CompoundTag tag = LCLuaTable.toTag(table);
            LightmansCurrency.LogDebug("Parsed table as Compound Tag\nTable: " + DebugUtil.debugMap(table) + "\nTag: " + tag.getAsString());
            if(tag.isEmpty())
                return false;
            IconData icon = IconData.load(tag,this.registryAccess());
            if(icon == null)
                return false;
            trader.setCustomIcon(icon);
            trader.pushLocalNotification(ChangeSettingNotification.dumb(this.getFakePlayer(computer),LCText.DATA_ENTRY_TRADER_ICON.get()));
            return true;
        }
        return false;
    }

    public boolean hasBankAccount() throws LuaException { return this.getTrader().hasBankAccount(); }

    @Nullable
    public LCLuaTable getLinkedAccount() throws LuaException {
        if(!this.hasBankAccount())
            return null;
        BankReference br = this.getTrader().getBankReference();
        LCLuaTable table = LCLuaTable.fromTag(br.save());
        //Hide the players id
        if(br instanceof PlayerBankReference pbr)
            table.put("Player",pbr.getPlayer().getName(false));
        return table;
    }

    public boolean setLinkedToBankAccount(IComputerAccess computer,IArguments args) throws LuaException {
        boolean newState = args.getBoolean(0);
        if(this.hasPermissions(computer,Permissions.BANK_LINK))
        {
            TraderData trader = this.getTrader();
            boolean linkedToBankAccount = trader.isLinkedToBank();
            if(linkedToBankAccount != newState)
            {
                //Confirm that we're *allowed* to link the bank account
                if(newState && !trader.canLinkBankAccount())
                    return false;
                trader.setLinkedToBank(newState);
                //Confirm that we succeeded
                if(newState != trader.isLinkedToBank())
                    return false;
                trader.pushLocalNotification(ChangeSettingNotification.simple(this.getFakePlayer(computer),LCText.DATA_ENTRY_TRADER_BANK_LINK.get(),newState));
                return true;
            }
        }
        return false;
    }

    public boolean showsSearchBox() throws LuaException { return this.getTrader().showSearchBox(); }
    public boolean setShowsSearchBox(IComputerAccess computer,IArguments args) throws LuaException
    {
        boolean newState = args.getBoolean(0);
        if(this.hasPermissions(computer,Permissions.EDIT_SETTINGS))
        {
            TraderData trader = this.getTrader();
            if(trader.alwaysShowSearchBox() != newState)
            {
                trader.setAlwaysShowSearchBox(newState);
                trader.pushLocalNotification(ChangeSettingNotification.simple(this.getFakePlayer(computer),LCText.DATA_ENTRY_TRADER_ALWAYS_SHOW_SEARCH_BOX.get(), newState));
                return true;
            }
        }
        return false;
    }
    public boolean hasPushNotifications() throws LuaException { return this.getTrader().notificationsEnabled(); }
    public boolean setPushNotifications(IComputerAccess computer,IArguments args) throws LuaException
    {
        boolean newState = args.getBoolean(0);
        if(this.hasPermissions(computer,Permissions.EDIT_SETTINGS))
        {
            TraderData trader = this.getTrader();
            if(trader.notificationsEnabled() != newState)
            {
                trader.setNotificationsEnabled(newState);
                trader.pushLocalNotification(ChangeSettingNotification.simple(this.getFakePlayer(computer),LCText.DATA_ENTRY_TRADER_NOTIFICATIONS_ENABLED.get(), newState));
                return true;
            }
        }
        return false;
    }
    public boolean hasChatNotifications() throws LuaException { return this.getTrader().notificationsToChat(); }
    public boolean setChatNotifications(IComputerAccess computer, IArguments args) throws LuaException
    {
        boolean newState = args.getBoolean(0);
        if(this.hasPermissions(computer,Permissions.EDIT_SETTINGS))
        {
            TraderData trader = this.getTrader();
            if(trader.notificationsToChat() != newState)
            {
                trader.setNotificationsToChat(newState);
                trader.pushLocalNotification(ChangeSettingNotification.simple(this.getFakePlayer(computer),LCText.DATA_ENTRY_TRADER_NOTIFICATIONS_TO_CHAT.get(), newState));
                return true;
            }
        }
        return false;
    }
    public int teamNotificationLevel() throws LuaException { return this.getTrader().teamNotificationLevel(); }
    public boolean setTeamNotificationLevel(IComputerAccess computer, IArguments args) throws LuaException
    {
        int newLevel = args.getInt(0);
        ArgumentHelpers.assertBetween(newLevel,0,2,"Level is not in range (%s)");
        if(this.hasPermissions(computer,Permissions.EDIT_SETTINGS))
        {
            TraderData trader = this.getTrader();
            if(trader.teamNotificationLevel() != newLevel)
            {
                trader.setTeamNotificationLevel(newLevel);
                trader.pushLocalNotification(ChangeSettingNotification.simple(this.getFakePlayer(computer),LCText.DATA_ENTRY_TRADER_TEAM_NOTIFICATION_LEVEL.get(), newLevel));
                return true;
            }
        }
        return false;
    }

    public int tradeCount() throws LuaException { return this.getTrader().getTradeCount(); }
    public int validTradeCount() throws LuaException { return this.getTrader().validTradeCount(); }
    public int tradesWithStock() throws LuaException { return this.getTrader().tradesWithStock(); }

    public int acceptableTaxRate() throws LuaException { return this.getTrader().getAcceptableTaxRate(); }
    public boolean setAcceptableTaxRate(IComputerAccess computer,IArguments args) throws LuaException
    {
        int newValue = args.getInt(0);
        ArgumentHelpers.assertBetween(newValue,0,99,"Tax Rate is not in range (%s)");
        if(this.hasPermissions(computer,Permissions.EDIT_SETTINGS))
        {
            TraderData trader = this.getTrader();
            if(trader.getAcceptableTaxRate() != newValue)
            {
                int oldRate = trader.getAcceptableTaxRate();
                trader.setAcceptableTaxRate(newValue);
                trader.pushLocalNotification(ChangeSettingNotification.advanced(this.getFakePlayer(computer),LCText.DATA_ENTRY_TRADER_TAXES_RATE.get(),newValue,oldRate));
                return true;
            }
        }
        return false;
    }
    public int currentTaxRate() throws LuaException { return this.getTrader().getTotalTaxPercentage(); }

    public LCLuaTable getWorldPosition() throws LuaException { return LCLuaTable.fromTag(this.getTrader().getWorldPosition().save()); }

    public String[] getCurrentUsers() throws LuaException {
        List<String> users = new ArrayList<>();
        for(Player user : this.getTrader().getUsers())
            users.add(user.getName().getString());
        return users.toArray(String[]::new);
    }

    public Object getUpgradeSlots(IComputerAccess computer) { return wrapContainer(() -> this.hasPermissions(computer,Permissions.OPEN_STORAGE),this::safeGetUpgradeContainer); }

    private Container safeGetUpgradeContainer()
    {
        TraderData trader = this.safeGetTrader();
        if(trader != null)
            return trader.getUpgrades();
        return null;
    }

    private static class Simple extends TraderPeripheral<TraderBlockEntity<TraderData>,TraderData>
    {
        private Simple(TraderBlockEntity<TraderData> blockEntity) { super(blockEntity); }
        private Simple(TraderData trader) { super(trader); }
        @Override
        public String getType() { return BASE_TYPE; }
        @Override
        public Set<String> getAdditionalTypes() { return Set.of(); }
        @Nullable
        @Override
        protected IPeripheral wrapTrade(TradeData trade) throws LuaException {
            int index = this.getTrader().indexOfTrade(trade);
            return TradeWrapper.createSimple(() -> {
                TraderData trader = this.safeGetTrader();
                if(trader != null && index >= 0 && index < trader.getTradeCount())
                    return trader.getTrade(index);
                return null;
            },this::safeGetTrader);
        }
    }

    //Listen to trade events
    public void preTradeEvent(TradeEvent.PreTradeEvent event)
    {
        TraderData trader = this.safeGetTrader();
        //Only push event if relevant to this specific peripheral
        if(event.getTrader() == trader)
        {
            try {
                IPeripheral tradeWrapper = this.wrapTrade(event.getTrade());
                LCLuaTable player = LCLuaTable.fromPlayer(event.getPlayerReference());
                boolean canceled = event.isCanceled();
                this.getConnectedComputers().queueEvent("lc_trade_pre",this,event.getTradeIndex(),tradeWrapper,player);
            } catch (LuaException ignored) {}
        }
    }

    private void postTradeEvent(TradeEvent.PostTradeEvent event)
    {
        TraderData trader = this.safeGetTrader();
        //Only push event if relevant to this specific peripheral
        if(event.getTrader() == trader)
        {
            try {
                IPeripheral tradeWrapper = this.wrapTrade(event.getTrade());
                LCLuaTable player = LCLuaTable.fromPlayer(event.getPlayerReference());
                LCLuaTable finalPrice = LCLuaTable.fromMoney(event.getPricePaid());
                LCLuaTable taxesPaid = LCLuaTable.fromMoney(event.getTaxesPaid());
                this.getConnectedComputers().queueEvent("lc_trade",this,event.getTradeIndex(),tradeWrapper,player,finalPrice,taxesPaid);
            } catch (LuaException ignored) {}
        }
    }

    @Override
    protected void registerMethods(PeripheralMethod.Registration registration) {
        registration.register(PeripheralMethod.builder("isValid").simple(this::isValid));
        registration.register(PeripheralMethod.builder("getID").simple(this::getID));
        registration.register(PeripheralMethod.builder("isVisibleOnNetwork").simple(this::isVisibleOnNetwork));
        registration.register(PeripheralMethod.builder("isCreative").simple(this::isCreative));
        registration.register(PeripheralMethod.builder("isPersistent").simple(this::isPersistent));
        registration.register(PeripheralMethod.builder("getOwner").simple(this::getOwner));
        registration.register(PeripheralMethod.builder("getOwnerName").simple(this::getOwnerName));
        registration.register(PeripheralMethod.builder("getStats").simple(this::getStats));
        registration.register(PeripheralMethod.builder("getAllies").simpleArray(this::getAllies));
        registration.register(PeripheralMethod.builder("addAlly").withContext(this::addAlly));
        registration.register(PeripheralMethod.builder("removeAlly").withContext(this::removeAlly));
        registration.register(PeripheralMethod.builder("getAllyPermissions").simple(this::getAllyPermissions));
        registration.register(PeripheralMethod.builder("getAllyPermissionLevel").withArgs(this::getAllyPermissionLevel));
        registration.register(PeripheralMethod.builder("setAllyPermissionLevel").withContext(this::setAllyPermissionLevel));
        registration.register(PeripheralMethod.builder("getPlayerPermissionLevel").withArgs(this::getPlayerPermissionLevel));
        registration.register(PeripheralMethod.builder("getMyPermissionLevel").withContext(this::getMyPermissionLevel));
        registration.register(PeripheralMethod.builder("getLogs").withArgsArray(this::getLogs));
        registration.register(PeripheralMethod.builder("getName").simple(this::getName));
        registration.register(PeripheralMethod.builder("setName").withContext(this::setName));
        registration.register(PeripheralMethod.builder("getIcon").simple(this::getIcon));
        registration.register(PeripheralMethod.builder("setIcon").withContext(this::setIcon));
        registration.register(PeripheralMethod.builder("hasBankAccount").simple(this::hasBankAccount));
        registration.register(PeripheralMethod.builder("getLinkedAccount").simple(this::getLinkedAccount));
        registration.register(PeripheralMethod.builder("setLinkedToBankAccount").withContext(this::setLinkedToBankAccount));
        registration.register(PeripheralMethod.builder("showsSearchBox").simple(this::showsSearchBox));
        registration.register(PeripheralMethod.builder("setShowsSearchBox").withContext(this::setShowsSearchBox));
        registration.register(PeripheralMethod.builder("hasPushNotifications").simple(this::hasPushNotifications));
        registration.register(PeripheralMethod.builder("setPushNotification").withContext(this::setPushNotifications));
        registration.register(PeripheralMethod.builder("hasChatNotifications").simple(this::hasChatNotifications));
        registration.register(PeripheralMethod.builder("setChatNotifications").withContext(this::setChatNotifications));
        registration.register(PeripheralMethod.builder("teamNotificationLevel").simple(this::teamNotificationLevel));
        registration.register(PeripheralMethod.builder("setTeamNotificationLevel").withContext(this::setTeamNotificationLevel));
        registration.register(PeripheralMethod.builder("tradeCount").simple(this::tradeCount));
        registration.register(PeripheralMethod.builder("validTradeCount").simple(this::validTradeCount));
        registration.register(PeripheralMethod.builder("tradesWithStock").simple(this::tradesWithStock));
        registration.register(PeripheralMethod.builder("acceptableTaxRate").simple(this::acceptableTaxRate));
        registration.register(PeripheralMethod.builder("setAcceptableTaxRate").withContext(this::setAcceptableTaxRate));
        registration.register(PeripheralMethod.builder("currentTaxRate").simple(this::currentTaxRate));
        registration.register(PeripheralMethod.builder("getWorldPosition").simple(this::getWorldPosition));
        registration.register(PeripheralMethod.builder("getCurrentUsers").simpleArray(this::getCurrentUsers));
        registration.register(PeripheralMethod.builder("getUpgrades").withContextOnly(this::getUpgradeSlots));
    }
}
