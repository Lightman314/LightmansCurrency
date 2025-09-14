package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader;

import com.google.common.base.Predicates;
import com.mojang.datafixers.util.Either;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.LuaValues;
import dan200.computercraft.api.peripheral.IPeripheral;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.stats.StatType;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.AccessTrackingPeripheral;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public abstract class TraderPeripheral<BE extends TraderBlockEntity<T>,T extends TraderData> extends AccessTrackingPeripheral {

    public static final String BASE_TYPE = "lc_trader";

    protected final Either<BE,Long> source;
    public TraderPeripheral(BE be) { this.source = Either.left(be); }
    public TraderPeripheral(T trader) { this.source = Either.right(trader.getID()); }

    public static IPeripheral createSimple(TraderBlockEntity<TraderData> be) { return new Simple(be); }
    public static IPeripheral createSimple(TraderData trader) { return new Simple(trader); }

    protected boolean isAuthorized() {
        //TODO change authorized state of a trader based on the connected computer ids
        return false;
    }

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
            try { result.set((T)TraderAPI.API.GetTrader(false,id));
            } catch (Exception ignored) { }
        });
        return result.get();
    }

    @Nonnull
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
    protected boolean eventListener() { return true; }

    @LuaFunction(mainThread = true)
    public boolean isValid() {
        try {
            this.getTrader();
            return true;
        } catch (LuaException e) { return false; }
    }

    @LuaFunction(mainThread = true)
    public long getID() throws LuaException { return this.getTrader().getID(); }

    @LuaFunction(mainThread = true)
    public boolean isVisibleOnNetwork() throws LuaException { return this.getTrader().showOnTerminal(); }

    @LuaFunction(mainThread = true)
    public boolean isCreative() throws LuaException { return this.getTrader().isCreative(); }

    @LuaFunction(mainThread = true)
    public boolean isPersistent() throws LuaException { return this.getTrader().isPersistent(); }

    @LuaFunction
    public LCLuaTable getOwner() throws LuaException {
        TraderData trader = this.getTrader();
        Owner owner = trader.getOwner().getValidOwner();
        return LCLuaTable.fromTag(owner.save());
    }

    @LuaFunction(mainThread = true)
    public String getOwnerName() throws LuaException { return this.getTrader().getOwner().getName().getString(); }

    @LuaFunction(mainThread = true)
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

    @LuaFunction(mainThread = true)
    public String[] getAllies() throws LuaException {
        return this.getTrader().getAllies().stream().map(p -> p.getName(false)).toArray(String[]::new);
    }

    @LuaFunction(mainThread = true)
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

    @LuaFunction(mainThread = true)
    public int getAllyPermissionLevel(String permission) throws LuaException {
        TraderData trader = this.getTrader();
        //Block permissions as applicable
        if(trader.isPersistent() || trader.getBlockedPermissions().contains(permission))
            return 0;
        return trader.getAllyPermissionMap().getOrDefault(permission,0);
    }

    @LuaFunction(mainThread = true)
    public int getPlayerPermissionLevel(String playerName,String permission) throws LuaException {
        TraderData trader = this.getTrader();
        PlayerReference player = PlayerReference.of(false,playerName);
        if(player == null)
            return 0;
        return trader.getPermissionLevel(player,permission);
    }

    @LuaFunction(mainThread = true)
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
            for(MutableComponent line : not.getMessageLines())
                lines.add(line.getString());
            entry.put("Text",lines.toArray(String[]::new));
            result.add(entry);
        }
        return result.toArray(LCLuaTable[]::new);
    }

    @LuaFunction(mainThread = true)
    public String getName() throws LuaException { return this.getTrader().getName().getString(); }

    @LuaFunction(mainThread = true)
    public LCLuaTable getIcon() throws LuaException {
        TraderData t = this.getTrader();
        return LCLuaTable.fromTag(t.getCustomIcon().save());
    }

    @LuaFunction(mainThread = true)
    public boolean hasBankAccount() throws LuaException { return this.getTrader().hasBankAccount(); }

    @LuaFunction(mainThread = true)
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

    @LuaFunction(mainThread = true)
    public boolean showsSearchBox() throws LuaException { return this.getTrader().showSearchBox(); }

    @LuaFunction(mainThread = true)
    public boolean hasPushNotifications() throws LuaException { return this.getTrader().notificationsEnabled(); }
    @LuaFunction(mainThread = true)
    public boolean hasChatNotifications() throws LuaException { return this.getTrader().notificationsToChat(); }
    @LuaFunction(mainThread = true)
    public int teamNotificationLevel() throws LuaException { return this.getTrader().teamNotificationLevel(); }

    @LuaFunction(mainThread = true)
    public int tradeCount() throws LuaException { return this.getTrader().getTradeCount(); }
    @LuaFunction(mainThread = true)
    public int validTradeCount() throws LuaException { return this.getTrader().validTradeCount(); }
    @LuaFunction(mainThread = true)
    public int tradesWithStock() throws LuaException { return this.getTrader().tradesWithStock(); }

    @LuaFunction(mainThread = true)
    public int acceptableTaxRate() throws LuaException { return this.getTrader().getAcceptableTaxRate(); }
    @LuaFunction(mainThread = true)
    public int currentTaxRate() throws LuaException { return this.getTrader().getTotalTaxPercentage(); }

    @LuaFunction(mainThread = true)
    public LCLuaTable getWorldPosition() throws LuaException { return LCLuaTable.fromTag(this.getTrader().getWorldPosition().save()); }

    @LuaFunction(mainThread = true)
    public String[] getCurrentUsers() throws LuaException {
        List<String> users = new ArrayList<>();
        for(Player user : this.getTrader().getUsers())
            users.add(user.getName().getString());
        return users.toArray(String[]::new);
    }

    @LuaFunction(mainThread = true)
    public LCLuaTable[] getUpgradeItems() throws LuaException {
        List<LCLuaTable> list = new ArrayList<>();
        Container upgrades = this.getTrader().getUpgrades();
        for(int i = 0; i < upgrades.getContainerSize(); ++i)
            list.add(LCLuaTable.fromTag(InventoryUtil.saveItemNoLimits(upgrades.getItem(i))));
        return list.toArray(LCLuaTable[]::new);
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
    @SubscribeEvent(priority = EventPriority.LOWEST,receiveCanceled = true)
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void postTradeEvent(TradeEvent.PostTradeEvent event)
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


}