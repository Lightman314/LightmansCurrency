package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.nodes.builtin;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.InputNode;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCArgumentHelper;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.nodes.TraderNodeMethods;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

public class InputNodeMethods extends TraderNodeMethods<InputNode> {

    public InputNodeMethods(TraderPeripheral peripheral) { super(peripheral,InputNode.TYPE); }

    public boolean allowsInputs() throws LuaException { return this.getNode().allowInputs(); }
    public boolean allowInputSide(IArguments args) throws LuaException { return this.getNode().allowInputSide(LCArgumentHelper.parseEnum(args,0,Direction.class)); }
    public boolean setInputSide(IComputerAccess computer, IArguments args) throws LuaException
    {
        Direction side = LCArgumentHelper.parseEnum(args,0,Direction.class);
        boolean newInputState = args.getBoolean(1);
        if(this.hasPermissions(computer,Permissions.InputTrader.EXTERNAL_INPUTS))
        {
            InputNode node = this.getNode();
            if(node.allowInputSide(side) != newInputState && node.allowInputs())
            {
                DirectionalSettingsState oldState = node.getSidedState(side);
                DirectionalSettingsState newState;
                if(oldState.allowsOutputs())
                    newState =newInputState ? DirectionalSettingsState.INPUT_AND_OUTPUT : DirectionalSettingsState.OUTPUT;
                else
                    newState = newInputState ? DirectionalSettingsState.INPUT : DirectionalSettingsState.NONE;
                return node.setDirectionalState(this.getFakePlayer(computer),side,newState);
            }
        }
        return false;
    }
    public LCLuaTable getInputSides() throws LuaException
    {
        InputNode node = this.getNode();
        List<String> sides = new ArrayList<>();
        for(Direction side : Direction.values())
        {
            if(node.allowInputSide(side))
                sides.add(side.toString());
        }
        return LCLuaTable.fromList(sides);
    }

    public boolean allowsOutputs() throws LuaException { return this.getNode().allowOutputs(); }
    public boolean allowOutputSide(IArguments args) throws LuaException { return this.getNode().allowOutputSide(LCArgumentHelper.parseEnum(args,0,Direction.class)); }
    public boolean setOutputSide(IComputerAccess computer, IArguments args) throws LuaException
    {
        Direction side = LCArgumentHelper.parseEnum(args,0,Direction.class);
        boolean newOutputState = args.getBoolean(1);
        if(this.hasPermissions(computer, Permissions.InputTrader.EXTERNAL_INPUTS))
        {
            InputNode node = this.getNode();
            if(node.allowOutputSide(side) != newOutputState && node.allowOutputs())
            {
                DirectionalSettingsState oldState = node.getSidedState(side);
                DirectionalSettingsState newState;
                if(oldState.allowsInputs())
                    newState = newOutputState ? DirectionalSettingsState.INPUT_AND_OUTPUT : DirectionalSettingsState.INPUT;
                else
                    newState = newOutputState ? DirectionalSettingsState.OUTPUT : DirectionalSettingsState.NONE;
                return node.setDirectionalState(this.getFakePlayer(computer),side,newState);
            }
        }
        return false;
    }
    public LCLuaTable getOutputSides() throws LuaException
    {
        InputNode node = this.getNode();
        List<String> sides = new ArrayList<>();
        for(Direction side : Direction.values())
        {
            if(node.allowOutputSide(side))
                sides.add(side.toString());
        }
        return LCLuaTable.fromList(sides);
    }

    @Override
    public void registerNodeMethods(LCPeripheralMethod.Registration registration) {
        registration.register(LCPeripheralMethod.builder("allowsInputs").simple(this::allowsInputs));
        registration.register(LCPeripheralMethod.builder("allowsInputSide").withArgs(this::allowInputSide));
        registration.register(LCPeripheralMethod.builder("setInputSide").withContext(this::setInputSide));
        registration.register(LCPeripheralMethod.builder("getInputSides").simple(this::getInputSides));
        registration.register(LCPeripheralMethod.builder("allowsOutputs").simple(this::allowsOutputs));
        registration.register(LCPeripheralMethod.builder("allowsOutputSide").withArgs(this::allowOutputSide));
        registration.register(LCPeripheralMethod.builder("setOutputSide").withContext(this::setOutputSide));
        registration.register(LCPeripheralMethod.builder("getOutputSides").simple(this::getOutputSides));
    }

}