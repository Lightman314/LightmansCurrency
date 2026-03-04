package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.nodes.builtin;

import com.google.common.base.Predicates;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaValues;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.core.util.ArgumentHelpers;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.stats.StatType;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.LoggerNode;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCComputerHelper;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.nodes.TraderNodeMethods;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class LoggerNodeMethods extends TraderNodeMethods<LoggerNode> {

    public LoggerNodeMethods(TraderPeripheral peripheral) { super(peripheral,LoggerNode.TYPE); }

    public LCLuaTable getStats() throws LuaException
    {
        LCLuaTable table = new LCLuaTable();
        TraderData trader = this.getTrader();
        LoggerNode node = trader.getNode(LoggerNode.TYPE);
        if(node != null)
        {
            for(String statKey : node.statTracker.getKeys())
            {
                StatType.Instance<?,?> entry = node.statTracker.getStat(statKey);
                if(entry == null)
                    continue;
                table.put(statKey,LCComputerHelper.getStatDisplay(entry));
            }
        }

        return table;
    }

    public LCLuaTable getLogs(IArguments args) throws LuaException {
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
        final Predicate<Notification> f = filter;
        List<Notification> notifications = this.getTrader().findNodeValue(LoggerNode.TYPE, node -> node.getNotifications(f),new ArrayList<>());
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
        return LCLuaTable.fromList(result);
    }

    public boolean hasPushNotifications() throws LuaException { return this.getNode().getNotificationsEnabled(); }
    public boolean setPushNotifications(IComputerAccess computer, IArguments args) throws LuaException
    {
        boolean newState = args.getBoolean(0);
        if(this.hasPermissions(computer, Permissions.EDIT_SETTINGS))
        {
            LoggerNode node = this.getNode();
            return node.setNotificationsEnabled(this.getFakePlayer(computer),newState);
        }
        return false;
    }
    public boolean hasChatNotifications() throws LuaException { return this.getNode().getNotificationsToChat(); }
    public boolean setChatNotifications(IComputerAccess computer, IArguments args) throws LuaException
    {
        boolean newState = args.getBoolean(0);
        if(this.hasPermissions(computer,Permissions.EDIT_SETTINGS))
        {
            LoggerNode node = this.getNode();
            return node.setNotificationsToChat(this.getFakePlayer(computer),newState);
        }
        return false;
    }
    public int teamNotificationLevel() throws LuaException { return this.getNode().getTeamNotificationLevel(); }
    public boolean setTeamNotificationLevel(IComputerAccess computer, IArguments args) throws LuaException
    {
        int newLevel = args.getInt(0);
        ArgumentHelpers.assertBetween(newLevel,0,2,"Level is not in range (%s)");
        if(this.hasPermissions(computer,Permissions.EDIT_SETTINGS))
        {
            LoggerNode node = this.getNode();
            return node.setTeamNotificationLevel(this.getFakePlayer(computer),newLevel);
        }
        return false;
    }

    @Override
    public void registerNodeMethods(LCPeripheralMethod.Registration registration) {
        registration.register(LCPeripheralMethod.builder("getStats").simple(this::getStats));
        registration.register(LCPeripheralMethod.builder("getLogs").withArgs(this::getLogs));
        registration.register(LCPeripheralMethod.builder("hasPushNotifications").simple(this::hasPushNotifications));
        registration.register(LCPeripheralMethod.builder("setPushNotification").withContext(this::setPushNotifications));
        registration.register(LCPeripheralMethod.builder("hasChatNotifications").simple(this::hasChatNotifications));
        registration.register(LCPeripheralMethod.builder("setChatNotifications").withContext(this::setChatNotifications));
        registration.register(LCPeripheralMethod.builder("teamNotificationLevel").simple(this::teamNotificationLevel));
        registration.register(LCPeripheralMethod.builder("setTeamNotificationLevel").withContext(this::setTeamNotificationLevel));
    }
}
