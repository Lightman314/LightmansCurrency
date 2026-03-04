package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.nodes.builtin;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.DisplayNode;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCArgumentHelper;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.nodes.TraderNodeMethods;
import io.github.lightman314.lightmanscurrency.util.DebugUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;

import java.util.Map;

public class DisplayNodeMethods extends TraderNodeMethods<DisplayNode> {

    public DisplayNodeMethods(TraderPeripheral peripheral) { super(peripheral,DisplayNode.TYPE); }

    public boolean setName(IComputerAccess computer,IArguments args) throws LuaException
    {
        Component newName = LCArgumentHelper.parseText(args,0);
        if(this.hasPermissions(computer,Permissions.CHANGE_NAME))
        {
            DisplayNode node = this.getNode();
            if(newName.getContents() instanceof PlainTextContents.LiteralContents contents && contents.text().length() > DisplayNode.MAX_NAME_LENGTH)
                newName = EasyText.literal(contents.text().substring(0,DisplayNode.MAX_NAME_LENGTH));
            TraderData trader = this.getTrader();
            return node.setCustomName(this.getFakePlayer(computer),newName);
        }
        return false;
    }

    public Object getIcon() throws LuaException {
        TraderData t = this.getTrader();
        return LCLuaTable.fromCustom(t.getIcon(),IconData.CODEC,this.dataContext());
    }

    public boolean setIcon(IComputerAccess computer, IArguments args) throws LuaException {
        if(this.hasPermissions(computer, Permissions.CHANGE_NAME))
        {
            DisplayNode node = this.getNode();
            Map<?,?> table = args.getTable(0);
            CompoundTag tag = LCLuaTable.toTag(table);
            LightmansCurrency.LogDebug("Parsed table as Compound Tag\nTable: " + DebugUtil.debugMap(table) + "\nTag: " + tag.getAsString());
            if(tag.isEmpty())
                return false;
            IconData icon = IconData.CODEC.decode(this.dataContext().ops(),tag).getOrThrow(LuaException::new).getFirst();
            if(icon == null)
                return false;
            return node.setCustomIcon(this.getFakePlayer(computer),icon);
        }
        return false;
    }

    public boolean showsSearchBox() throws LuaException { return this.getTrader().showSearchBox(); }
    public boolean setShowsSearchBox(IComputerAccess computer,IArguments args) throws LuaException
    {
        boolean newState = args.getBoolean(0);
        if(this.hasPermissions(computer,Permissions.EDIT_SETTINGS))
        {
            DisplayNode node = this.getNode();
            return node.setAlwaysShowSearchBox(this.getFakePlayer(computer),newState);
        }
        return false;
    }

    @Override
    public void registerNodeMethods(LCPeripheralMethod.Registration registration) {
        registration.register(LCPeripheralMethod.builder("setName").withContext(this::setName));
        registration.register(LCPeripheralMethod.builder("getIcon").simple(this::getIcon));
        registration.register(LCPeripheralMethod.builder("setIcon").withContext(this::setIcon));
        registration.register(LCPeripheralMethod.builder("showsSearchBox").simple(this::showsSearchBox));
        registration.register(LCPeripheralMethod.builder("setShowsSearchBox").withContext(this::setShowsSearchBox));
    }

}
