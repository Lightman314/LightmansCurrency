package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeSettingNotification;
import io.github.lightman314.lightmanscurrency.common.traders.InputTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCArgumentHelper;
import net.minecraft.core.Direction;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class InputTraderPeripheral<BE extends TraderBlockEntity<T>,T extends InputTraderData> extends TraderPeripheral<BE,T> {

    public InputTraderPeripheral(BE be) { super(be); }
    public InputTraderPeripheral(T trader) { super(trader); }

    public static InputTraderPeripheral<TraderBlockEntity<InputTraderData>,InputTraderData> createSimpleInput(TraderBlockEntity<InputTraderData> blockEntity) { return new Simple(blockEntity); }
    public static InputTraderPeripheral<TraderBlockEntity<InputTraderData>,InputTraderData> createSimpleInput(InputTraderData trader) { return new Simple(trader); }

    @Override
    public Set<String> getAdditionalTypes() {
        Set<String> set = new HashSet<>(super.getAdditionalTypes());
        set.add("lc_trader_input");
        return set;
    }
    public boolean allowsInputs() throws LuaException { return this.getTrader().allowInputs(); }
    public boolean allowInputSide(IArguments args) throws LuaException { return this.getTrader().allowInputSide(LCArgumentHelper.parseEnum(args,0,Direction.class)); }
    public boolean setInputSide(IComputerAccess computer, IArguments args) throws LuaException
    {
        Direction side = LCArgumentHelper.parseEnum(args,0,Direction.class);
        boolean newInputState = args.getBoolean(1);
        if(this.hasPermissions(computer, Permissions.InputTrader.EXTERNAL_INPUTS))
        {
            InputTraderData trader = this.getTrader();
            if(trader.allowInputSide(side) != newInputState && trader.allowInputs() && !trader.ignoreSides.contains(side))
            {
                DirectionalSettingsState oldState = trader.getSidedState(side);
                DirectionalSettingsState newState;
                if(oldState.allowsOutputs())
                    newState =newInputState ? DirectionalSettingsState.INPUT_AND_OUTPUT : DirectionalSettingsState.OUTPUT;
                else
                    newState = newInputState ? DirectionalSettingsState.INPUT : DirectionalSettingsState.NONE;
                trader.setDirectionalState(null,side,newState);
                trader.pushLocalNotification(ChangeSettingNotification.simple(this.getFakePlayer(computer), EasyText.empty().append(LCText.DATA_ENTRY_INPUT_OUTPUT_SIDES.get()).append(InputTraderData.getFacingName(side)), newState.getText()));
                return true;
            }
        }
        return false;
    }
    public String[] getInputSides() throws LuaException
    {
        InputTraderData trader = this.getTrader();
        List<String> sides = new ArrayList<>();
        for(Direction side : Direction.values())
        {
            if(trader.allowInputSide(side))
                sides.add(side.toString());
        }
        return sides.toArray(String[]::new);
    }

    public boolean allowsOutputs() throws LuaException { return this.getTrader().allowOutputs(); }
    public boolean allowOutputSide(IArguments args) throws LuaException { return this.getTrader().allowOutputSide(LCArgumentHelper.parseEnum(args,0,Direction.class)); }
    public boolean setOutputSide(IComputerAccess computer, IArguments args) throws LuaException
    {
        Direction side = LCArgumentHelper.parseEnum(args,0,Direction.class);
        boolean newOutputState = args.getBoolean(1);
        if(this.hasPermissions(computer, Permissions.InputTrader.EXTERNAL_INPUTS))
        {
            InputTraderData trader = this.getTrader();
            if(trader.allowOutputSide(side) != newOutputState && trader.allowOutputs() && !trader.ignoreSides.contains(side))
            {
                DirectionalSettingsState oldState = trader.getSidedState(side);
                DirectionalSettingsState newState;
                if(oldState.allowsInputs())
                    newState = newOutputState ? DirectionalSettingsState.INPUT_AND_OUTPUT : DirectionalSettingsState.INPUT;
                else
                    newState = newOutputState ? DirectionalSettingsState.OUTPUT : DirectionalSettingsState.NONE;
                trader.setDirectionalState(null,side,newState);
                trader.pushLocalNotification(ChangeSettingNotification.simple(this.getFakePlayer(computer), EasyText.empty().append(LCText.DATA_ENTRY_INPUT_OUTPUT_SIDES.get()).append(InputTraderData.getFacingName(side)), newState.getText()));
                return true;
            }
        }
        return false;
    }
    public String[] getOutputSides() throws LuaException
    {
        InputTraderData trader = this.getTrader();
        List<String> sides = new ArrayList<>();
        for(Direction side : Direction.values())
        {
            if(trader.allowOutputSide(side))
                sides.add(side.toString());
        }
        return sides.toArray(String[]::new);
    }

    @Override
    protected void registerMethods(LCPeripheralMethod.Registration registration) {
        super.registerMethods(registration);
        registration.register(LCPeripheralMethod.builder("allowsInputs").simple(this::allowsInputs));
        registration.register(LCPeripheralMethod.builder("allowsInputSide").withArgs(this::allowInputSide));
        registration.register(LCPeripheralMethod.builder("setInputSide").withContext(this::setInputSide));
        registration.register(LCPeripheralMethod.builder("getInputSides").simpleArray(this::getInputSides));
        registration.register(LCPeripheralMethod.builder("allowsOutputs").simple(this::allowsOutputs));
        registration.register(LCPeripheralMethod.builder("allowsOutputSide").withArgs(this::allowOutputSide));
        registration.register(LCPeripheralMethod.builder("setOutputSide").withContext(this::setOutputSide));
        registration.register(LCPeripheralMethod.builder("getOutputSides").simpleArray(this::getOutputSides));
    }

    private static final class Simple extends InputTraderPeripheral<TraderBlockEntity<InputTraderData>,InputTraderData>
    {

        private Simple(TraderBlockEntity<InputTraderData> blockEntity) { super(blockEntity); }
        private Simple(InputTraderData trader) { super(trader); }

        @Nullable
        @Override
        protected LCPeripheral wrapTrade(TradeData trade) throws LuaException {
            int index = this.getTrader().indexOfTrade(trade);
            return TradeWrapper.createSimple(() -> {
                TraderData trader = this.safeGetTrader();
                if(trader != null)
                {
                    if(index < 0 || index >= trader.getTradeCount())
                        return null;
                    return trader.getTrade(index);
                }
                return null;
            },this::safeGetTrader);
        }

        @Override
        public String getType() { return "lc_trader_input"; }
        @Override
        public Set<String> getAdditionalTypes() {
            Set<String> set = new HashSet<>(super.getAdditionalTypes());
            set.remove("lc_trader_input");
            return set;
        }

    }

}