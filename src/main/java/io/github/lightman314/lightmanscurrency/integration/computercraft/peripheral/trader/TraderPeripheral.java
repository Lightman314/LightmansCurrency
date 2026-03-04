package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader;

import com.mojang.datafixers.util.Either;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.interfaces.IPersistentTrader;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.*;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCComputerHelper;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.AccessTrackingPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.nodes.TradeOfferNodeMethods;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.nodes.TraderNodeMethods;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public final class TraderPeripheral extends AccessTrackingPeripheral {

    public static final String BASE_TYPE = "lc_trader";

    private final Either<TraderBlockEntity<?>,Long> source;
    public TraderPeripheral(TraderBlockEntity<?> be) { this.source = Either.left(be); }
    public TraderPeripheral(TraderData trader) { this.source = Either.right(trader.getID()); }

    private List<TraderNodeMethods<?>> nodeMethods;
    private TradeOfferNodeMethods<?,?> tradeOfferNodeMethods;

    private final Consumer<TradeEvent.PreTradeEvent> preTradeEventListener = this::preTradeEvent;
    private final Consumer<TradeEvent.PostTradeEvent> postTradeEventListener = this::postTradeEvent;

    public int getPermissionLevel(IComputerAccess computer, String permission)
    {
        String id = this.getComputerID(computer);
        if(id == null)
            return 0;
        TraderData trader = this.safeGetTrader();
        if(trader == null || !trader.hasNode(MachineAccessNode.TYPE))
            return 0;
        //Deny blocked permissions early
        if(trader.isPermissionBlocked(permission))
            return 0;
        MachineAccessNode.AccessLevel access = trader.getNode(MachineAccessNode.TYPE).getAccessLevel(id);

        return switch (access) {
            case NONE -> 0;
            case ALLY -> trader.findNodeValue(AlliesNode.TYPE,AlliesNode::getAllyPermissionsMap,new HashMap<String,Integer>()).getOrDefault(permission, 0);
            case ADMIN -> Integer.MAX_VALUE;
        };
    }
    public boolean hasPermissions(IComputerAccess computer,String permission) { return this.getPermissionLevel(computer,permission) > 0; }

    @Nullable
    public TraderBlockEntity<?> getBlockEntity() {
        AtomicReference<TraderBlockEntity<?>> result = new AtomicReference<>(null);
        this.source.ifLeft(result::set);
        return result.get();
    }

    @Nullable
    public TraderData safeGetTrader() {
        AtomicReference<TraderData> result = new AtomicReference<>(null);
        this.source.ifLeft(be -> {
            if(be.isRemoved())
                return;
            result.set(be.getTraderData());
        });
        this.source.ifRight(id -> result.set(TraderAPI.getApi().GetTrader(false,id)));
        return result.get();
    }

    public TraderData getTrader() throws LuaException {
        if(!this.stillValid())
            throw new LuaException("An unexpected error occurred trying to get the traders data!");
        TraderData trader = this.safeGetTrader();
        if(trader == null)
            throw new LuaException("An unexpected error occurred trying to get the traders data!");
        return trader;
    }

    @Nullable
    public AccessTrackingPeripheral wrapTrade(TradeData trade) throws LuaException
    {
        if(this.tradeOfferNodeMethods != null)
            return this.tradeOfferNodeMethods.wrapTrade(trade);
        throw new LuaException("Trader does not support trades!");
    }

    @Nullable
    public AccessTrackingPeripheral safeWrapTrade(TradeData trade)
    {
        try { return this.wrapTrade(trade);
        } catch (LuaException e) { return null; }
    }

    @Override
    public String getType() { return BASE_TYPE; }

    @Override
    public Set<String> getAdditionalTypes() {
        //Trigger the initialization
        this.getMethodNames();
        Set<String> set = new HashSet<>();
        for(TraderNodeMethods<?> method : this.nodeMethods)
            method.addToTypes(set::add);
        return set;
    }

    @Override
    public boolean equals(@Nullable IPeripheral peripheral) {
        if(peripheral instanceof TraderPeripheral other)
            return other.source.equals(this.source) && super.equals(peripheral);
        return false;
    }

    @Override
    protected void onAttachment(IComputerAccess computer) {
        TraderData trader = this.safeGetTrader();
        if(trader != null && trader.hasNode(MachineAccessNode.TYPE))
            trader.getNode(MachineAccessNode.TYPE).flagAttemptedAccess(this.getComputerID(computer));
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

    public boolean isValid() { return this.safeGetTrader() != null; }

    public long getID() throws LuaException { return this.getTrader().getID(); }

    public boolean isVisibleOnNetwork() throws LuaException { return this.getTrader().isNetworkAccessible(); }

    public boolean isCreative() throws LuaException { return this.getTrader().hasInfiniteStock(); }

    public boolean isPersistent() throws LuaException {
        if(this.getTrader() instanceof IPersistentTrader pt)
            return pt.isPersistent();
        return false;
    }

    public Object getOwner() throws LuaException {
        TraderData trader = this.getTrader();
        Owner owner = trader.getOwner().getValidOwner();
        return LCLuaTable.fromCustom(owner,Owner.CODEC,this.dataContext());
    }

    public String getOwnerName() throws LuaException { return this.getTrader().getOwner().getName().getString(); }

    public int getPlayerPermissionLevel(IArguments args) throws LuaException {
        TraderData trader = this.getTrader();
        PlayerReference player = PlayerReference.of(false,args.getString(0));
        if(player == null)
            return 0;
        return trader.getPermissionLevel(player,args.getString(1));
    }

    public int getMyPermissionLevel(IComputerAccess computer, IArguments args) throws LuaException { return this.getPermissionLevel(computer,args.getString(0)); }

    public String getName() throws LuaException { return this.getTrader().getName().getString(); }

    public int tradeCount() throws LuaException { return this.getTrader().getTradeCount(); }
    public int validTradeCount() throws LuaException { return this.getTrader().validTradeCount(); }
    public int tradesWithStock() throws LuaException { return this.getTrader().tradesWithStock(); }

    public LCLuaTable getWorldPosition() throws LuaException { return LCLuaTable.fromTag(this.getTrader().getWorldPosition().save()); }

    public LCLuaTable getCurrentUsers() throws LuaException {
        List<String> users = new ArrayList<>();
        for(Player user : this.getTrader().getUsers())
            users.add(user.getName().getString());
        return LCLuaTable.fromList(users);
    }

    public Object getUpgradeSlots(IComputerAccess computer) { return wrapInventory(computer,() -> this.hasPermissions(computer,Permissions.OPEN_STORAGE),this::safeGetUpgradeContainer,() -> {},this); }

    private IItemHandler safeGetUpgradeContainer()
    {
        TraderData trader = this.safeGetTrader();
        if(trader != null)
            return trader.getUpgrades();
        return null;
    }

    //Listen to trade events
    public void preTradeEvent(TradeEvent.PreTradeEvent event)
    {
        TraderData trader = this.safeGetTrader();
        //Only push event if relevant to this specific peripheral
        if(event.getTrader() == trader)
        {
            try {
                AccessTrackingPeripheral tradeWrapper = this.wrapTrade(event.getTrade());
                LCLuaTable player = LCLuaTable.fromPlayer(event.getPlayerReference());
                boolean canceled = event.isCanceled();
                this.queueEvent("lc_trade_pre",computer -> new Object[] { this.asTable(computer),event.getTradeIndex(),tradeWrapper.asTable(computer),player,canceled});
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
                AccessTrackingPeripheral tradeWrapper = this.wrapTrade(event.getTrade());
                LCLuaTable player = LCLuaTable.fromPlayer(event.getPlayerReference());
                LCLuaTable finalPrice = LCLuaTable.fromMoney(event.getPricePaid());
                LCLuaTable taxesPaid = LCLuaTable.fromMoney(event.getTaxesPaid());
                this.queueEvent("lc_trade",computer -> new Object[] { this.asTable(computer),event.getTradeIndex(),tradeWrapper.asTable(computer),player,finalPrice,taxesPaid});
            } catch (LuaException ignored) {}
        }
    }

    @Override
    protected void registerMethods(LCPeripheralMethod.Registration registration) {
        //Common Trader Methods that exist regardless of node presence
        registration.register(LCPeripheralMethod.builder("isValid").simple(this::isValid));
        registration.register(LCPeripheralMethod.builder("getID").simple(this::getID));
        registration.register(LCPeripheralMethod.builder("isVisibleOnNetwork").simple(this::isVisibleOnNetwork));
        registration.register(LCPeripheralMethod.builder("isCreative").simple(this::isCreative));
        registration.register(LCPeripheralMethod.builder("isPersistent").simple(this::isPersistent));
        registration.register(LCPeripheralMethod.builder("getOwner").simple(this::getOwner));
        registration.register(LCPeripheralMethod.builder("getOwnerName").simple(this::getOwnerName));
        registration.register(LCPeripheralMethod.builder("getName").simple(this::getName));

        //Common permission methods
        registration.register(LCPeripheralMethod.builder("getPlayerPermissionLevel").withArgs(this::getPlayerPermissionLevel));
        registration.register(LCPeripheralMethod.builder("getMyPermissionLevel").withContext(this::getMyPermissionLevel));

        //Node Methods
        this.nodeMethods = LCComputerHelper.getTraderNodeMethods(this);
        for(TraderNodeMethods<?> method : this.nodeMethods)
        {
            if(method instanceof TradeOfferNodeMethods<?,?> offerNode)
                this.tradeOfferNodeMethods = offerNode;
            method.registerNodeMethods(registration);
        }

        registration.register(LCPeripheralMethod.builder("tradeCount").simple(this::tradeCount));
        registration.register(LCPeripheralMethod.builder("validTradeCount").simple(this::validTradeCount));
        registration.register(LCPeripheralMethod.builder("tradesWithStock").simple(this::tradesWithStock));

        registration.register(LCPeripheralMethod.builder("getWorldPosition").simple(this::getWorldPosition));
        registration.register(LCPeripheralMethod.builder("getCurrentUsers").simple(this::getCurrentUsers));
        registration.register(LCPeripheralMethod.builder("getUpgrades").withContextOnly(this::getUpgradeSlots));

    }

}