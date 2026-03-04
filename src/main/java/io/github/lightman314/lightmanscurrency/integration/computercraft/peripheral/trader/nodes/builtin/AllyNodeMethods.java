package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.nodes.builtin;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.core.util.ArgumentHelpers;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.AlliesNode;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.nodes.TraderNodeMethods;

import java.util.ArrayList;
import java.util.List;

public class AllyNodeMethods extends TraderNodeMethods<AlliesNode> {

    public AllyNodeMethods(TraderPeripheral peripheral) { super(peripheral,AlliesNode.TYPE); }

    public String[] getAllies() throws LuaException {
        List<PlayerReference> empty = new ArrayList<>();
        return this.getTrader().findNodeValue(AlliesNode.TYPE,AlliesNode::getAllies,empty).stream().map(p -> p.getName(false)).toArray(String[]::new);
    }

    public boolean addAlly(IComputerAccess computer, IArguments args) throws LuaException
    {
        if(this.hasPermissions(computer, Permissions.ADD_REMOVE_ALLIES))
        {
            AlliesNode node = this.getNode();
            PlayerReference player = PlayerReference.of(false,args.getString(0));
            if(player == null)
                return false;
            return node.addAlly(this.getFakePlayer(computer),player);
        }
        return false;
    }

    public boolean removeAlly(IComputerAccess computer,IArguments args) throws LuaException
    {
        if(this.hasPermissions(computer,Permissions.ADD_REMOVE_ALLIES))
        {
            AlliesNode node = this.getNode();
            PlayerReference player = PlayerReference.of(false,args.getString(0));
            if(player == null)
                return false;
            return node.removeAlly(this.getFakePlayer(computer),player);
        }
        return false;
    }

    public LCLuaTable getAllyPermissions() throws LuaException{
        TraderData trader = this.getTrader();
        AlliesNode node = this.getNode();
        LCLuaTable table = new LCLuaTable();
        this.getNode().getAllyPermissionsMap().forEach((key,level) -> {
            if(!trader.isPermissionBlocked(key))
                table.put(key,level);
        });
        return table;
    }

    public int getAllyPermissionLevel(IArguments args) throws LuaException {
        String permission = args.getString(0);
        TraderData trader = this.getTrader();
        AlliesNode node = this.getNode();
        //Block permissions as applicable
        if(trader.isPermissionBlocked(permission))
            return 0;
        return node.getAllyPermissionsMap().getOrDefault(permission,0);
    }

    public boolean setAllyPermissionLevel(IComputerAccess computer,IArguments args) throws LuaException
    {
        String permission = args.getString(0);
        int level = args.getInt(1);
        ArgumentHelpers.assertBetween(level,0,Integer.MAX_VALUE,"Permission Level is not in range (%s)");
        if(this.hasPermissions(computer,Permissions.EDIT_PERMISSIONS))
            return this.getNode().setAllyPermission(this.getFakePlayer(computer),permission,level);
        return false;
    }

    @Override
    public void registerNodeMethods(LCPeripheralMethod.Registration registration) {
        registration.register(LCPeripheralMethod.builder("getAllies").simpleArray(this::getAllies));
        registration.register(LCPeripheralMethod.builder("addAlly").withContext(this::addAlly));
        registration.register(LCPeripheralMethod.builder("removeAlly").withContext(this::removeAlly));
        registration.register(LCPeripheralMethod.builder("getAllyPermissions").simple(this::getAllyPermissions));
        registration.register(LCPeripheralMethod.builder("getAllyPermissionLevel").withArgs(this::getAllyPermissionLevel));
        registration.register(LCPeripheralMethod.builder("setAllyPermissionLevel").withContext(this::setAllyPermissionLevel));
    }

}
