package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.attachments.builtin.ExternalAuthorizationAttachment;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCArgumentHelper;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.AccessTrackingPeripheral;

import java.util.Set;
import java.util.function.Supplier;

public abstract class TradeWrapper<T extends TradeData> extends AccessTrackingPeripheral {

    public static final String BASE_TYPE = "lc_trade";

    private final Supplier<T> source;
    private final Supplier<TraderData> trader;
    public TradeWrapper(Supplier<T> tradeSource, Supplier<TraderData> trader) { this.source = tradeSource; this.trader = trader; }

    public static TradeWrapper<TradeData> createSimple(Supplier<TradeData> tradeSource, Supplier<TraderData> trader) { return new Simple(tradeSource,trader); }

    @Override
    public Set<String> getAdditionalTypes() { return Set.of(BASE_TYPE); }

    public final T getTrade() throws LuaException{
        T trade = this.source.get();
        if(trade == null)
            throw new LuaException("An unexpected error occurred trying to access the trade!");
        return trade;
    }

    public int getPermissionLevel(IComputerAccess computer)
    {
        String id = this.getComputerID(computer);
        if(id == null)
            return 0;
        TraderData trader = this.trader.get();
        if(trader == null || !trader.hasAttachment(ExternalAuthorizationAttachment.TYPE))
            return 0;
        //Deny blocked permissions early
        if(trader.getBlockedPermissions().contains(Permissions.EDIT_TRADES))
            return 0;
        ExternalAuthorizationAttachment.AccessLevel access = trader.getAttachment(ExternalAuthorizationAttachment.TYPE).getAccessLevel(id);
        return switch (access) {
            case NONE -> 0;
            case ALLY -> trader.getAllyPermissionMap().getOrDefault(Permissions.EDIT_TRADES, 0);
            case ADMIN -> Integer.MAX_VALUE;
        };
    }
    public boolean hasPermission(IComputerAccess computer) { return this.getPermissionLevel(computer) > 0; }

    public final void markTradeDirty()
    {
        TraderData trader = this.trader.get();
        if(trader != null)
            trader.markTradesDirty();
    }

    public boolean isValid() {
        try { return this.getTrade().isValid();
        } catch (LuaException exception) { return false; }
    }

    public LCLuaTable getPrice() throws LuaException {
        LCLuaTable table = new LCLuaTable();
        return LCLuaTable.fromMoney(this.getTrade().getCost());
    }
    public boolean setPrice(IComputerAccess computer, IArguments args) throws LuaException
    {
        MoneyValue newPrice = LCArgumentHelper.parseMoneyValue(args,0,true);
        TradeData trade = this.getTrade();
        if(this.hasPermission(computer))
        {
            trade.setCost(newPrice);
            this.markTradeDirty();
            return true;
        }
        return false;
    }

    public int getStock() throws LuaException {
        TraderData trader = this.trader.get();
        if(trader == null)
            throw new LuaException("An unexpected error occurred trying to access the trader!");
        TradeData trade = this.getTrade();
        return trade.getStock(TradeContext.createStorageMode(trader));
    }

    public String getDirection() throws LuaException { return this.getTrade().getTradeDirection().toString(); }

    public boolean isSale() throws LuaException { return this.getTrade().getTradeDirection() == TradeDirection.SALE; }
    public boolean isPurchase() throws LuaException { return this.getTrade().getTradeDirection() == TradeDirection.PURCHASE; }
    public boolean isBarter() throws LuaException { return this.getTrade().getTradeDirection() == TradeDirection.BARTER; }
    public boolean isOther() throws LuaException { return this.getTrade().getTradeDirection() == TradeDirection.OTHER; }

    @Override
    protected void registerMethods(LCPeripheralMethod.Registration registration) {
        registration.register(LCPeripheralMethod.builder("isValid").simple(this::isValid));
        registration.register(LCPeripheralMethod.builder("getPrice").simple(this::getPrice));
        registration.register(LCPeripheralMethod.builder("setPrice").withContext(this::setPrice));
        registration.register(LCPeripheralMethod.builder("getStock").simple(this::getStock));
        registration.register(LCPeripheralMethod.builder("getDirection").simple(this::getDirection));
        registration.register(LCPeripheralMethod.builder("isSale").simple(this::isSale));
        registration.register(LCPeripheralMethod.builder("isPurchase").simple(this::isPurchase));
        registration.register(LCPeripheralMethod.builder("isBarter").simple(this::isBarter));
        registration.register(LCPeripheralMethod.builder("isOther").simple(this::isOther));
    }

    private static final class Simple extends TradeWrapper<TradeData>
    {

        public Simple(Supplier<TradeData> tradeSource, Supplier<TraderData> trader) { super(tradeSource, trader); }
        @Override
        public String getType() { return BASE_TYPE; }
        @Override
        public Set<String> getAdditionalTypes() { return Set.of(); }
    }

}
