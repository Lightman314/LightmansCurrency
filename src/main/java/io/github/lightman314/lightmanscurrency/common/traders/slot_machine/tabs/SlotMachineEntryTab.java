package io.github.lightman314.lightmanscurrency.common.traders.slot_machine.tabs;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageNodeTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.client.tabs.SlotMachineEntryClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.nodes.SlotMachineNode;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.util.DebugUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import java.util.List;

public class SlotMachineEntryTab extends TraderStorageNodeTab<SlotMachineNode> {

    public SlotMachineEntryTab(ITraderStorageMenu menu) { super(SlotMachineNode.TYPE,menu); }

    @Override
    public ResourceLocation tabKey() { return BasicTradeEditTab.KEY; }

    @Override
    protected boolean isDefaultTab() { return true; }

    @Override
    public Object createClientTab(Object screen) { return new SlotMachineEntryClientTab(screen, this); }

    public void AddEntry()
    {
        if(!this.menu.hasPermission(Permissions.EDIT_TRADES))
            return;
        SlotMachineNode node = this.getNode();
        if(node != null)
        {
            node.addEntry();
            if(this.menu.isClient())
                this.menu.SendMessage(this.builder().setFlag("AddEntry"));
        }
    }

    public void RemoveEntry(int entryIndex)
    {
        if(!this.menu.hasPermission(Permissions.EDIT_TRADES))
            return;
        SlotMachineNode node = this.getNode();
        if(node != null)
        {
            node.removeEntry(entryIndex);
            if(this.menu.isClient())
                this.menu.SendMessage(this.builder().setInt("RemoveEntry", entryIndex));
        }
    }

    @Nullable
    private SlotMachineEntry getEntry(int entryIndex)
    {
        SlotMachineNode node = this.getNode();
        if(node != null)
        {
            List<SlotMachineEntry> entries = node.getAllEntries();
            if(entryIndex < 0 || entryIndex >= entries.size())
                return null;
            return entries.get(entryIndex);
        }
        return null;
    }

    public void AddEntryItem(int entryIndex, ItemStack item)
    {
        if(!this.menu.hasPermission(Permissions.EDIT_TRADES))
        {
            Permissions.PermissionWarning(this.menu.getPlayer(), "edit slot machine trade", Permissions.EDIT_TRADES);
            return;
        }
        SlotMachineEntry entry = this.getEntry(entryIndex);
        if(entry != null)
        {
            //Use TryAddItem to enforce item limit
            entry.TryAddItem(item);
            if(this.isClient())
            {
                this.menu.SendMessage(this.builder()
                        .setInt("EditEntry", entryIndex)
                        .setItem("AddItem",item));
            }
        }
    }

    public void EditEntryItem(int entryIndex, int itemIndex, ItemStack item)
    {
        if(!this.menu.hasPermission(Permissions.EDIT_TRADES))
        {
            Permissions.PermissionWarning(this.menu.getPlayer(), "edit slot machine trade", Permissions.EDIT_TRADES);
            return;
        }
        if(item.isEmpty())
        {
            this.RemoveEntryItem(entryIndex, itemIndex);
            return;
        }
        SlotMachineEntry entry = this.getEntry(entryIndex);
        if(entry != null)
        {
            if(itemIndex < 0 || itemIndex >= entry.items.size())
                return;
            entry.items.set(itemIndex,item);
            entry.validateItems();
            entry.setChanged();
            if(this.isClient())
            {
                this.menu.SendMessage(this.builder()
                        .setInt("EditEntry", entryIndex)
                        .setInt("ItemIndex", itemIndex)
                        .setItem("EditItem",item));
            }
        }
    }

    public void RemoveEntryItem(int entryIndex, int itemIndex)
    {
        if(!this.menu.hasPermission(Permissions.EDIT_TRADES))
        {
            Permissions.PermissionWarning(this.menu.getPlayer(), "edit slot machine trade", Permissions.EDIT_TRADES);
            return;
        }
        SlotMachineEntry entry = this.getEntry(entryIndex);
        if(entry != null)
        {
            if(itemIndex < 0 || itemIndex >= entry.items.size())
                return;
            entry.items.remove(itemIndex);
            entry.validateItems();
            entry.setChanged();
            if(this.isClient())
            {
                this.menu.SendMessage(this.builder()
                        .setInt("EditEntry", entryIndex)
                        .setInt("RemoveItem", itemIndex));
            }
        }
    }

    public void ChangeEntryOdds(int entryIndex, double newOdds)
    {
        if(!this.menu.hasPermission(Permissions.EDIT_TRADES))
        {
            Permissions.PermissionWarning(this.menu.getPlayer(), "edit slot machine trade", Permissions.EDIT_TRADES);
            return;
        }
        SlotMachineEntry entry = this.getEntry(entryIndex);
        if(entry != null)
        {
            entry.setOdds(newOdds);
            LightmansCurrency.LogDebug("Changed entry[" + entryIndex + "]'s odds on the " + DebugUtil.getSideText(this.menu) + "!");
            if(this.isClient())
            {
                this.menu.SendMessage(this.builder()
                        .setInt("EditEntry", entryIndex)
                        .setDouble("SetOdds", newOdds));
            }
        }
    }

    public void ChangeEntryHasCustomIcons(int entryIndex, boolean hasCustomIcons)
    {
        if(!this.menu.hasPermission(Permissions.EDIT_TRADES))
        {
            Permissions.PermissionWarning(this.menu.getPlayer(),"edit slot machine trade", Permissions.EDIT_TRADES);
            return;
        }
        SlotMachineEntry entry = this.getEntry(entryIndex);
        if(entry != null)
        {
            entry.setHasCustomIcons(hasCustomIcons);
            if(this.isClient())
            {
                this.menu.SendMessage(this.builder()
                        .setInt("EditEntry",entryIndex)
                        .setBoolean("SetHasCustomIcon",hasCustomIcons));
            }
        }
    }

    public void ChangeEntryCustomIcon(int entryIndex, int iconIndex, IconData icon)
    {
        if(!this.menu.hasPermission(Permissions.EDIT_TRADES))
        {
            Permissions.PermissionWarning(this.menu.getPlayer(),"edit slot machine trade", Permissions.EDIT_TRADES);
            return;
        }
        SlotMachineEntry entry = this.getEntry(entryIndex);
        if(entry != null)
        {
            entry.setCustomIcon(iconIndex,icon);
            //LightmansCurrency.LogDebug("Set custom icon on the " + DebugUtil.getSideText(this) + "\nData: " + icon.save(this.registryAccess()).getAsString());
            if(this.isClient())
            {
                this.menu.SendMessage(this.builder()
                        .setInt("EditEntry",entryIndex)
                        .setInt("IconIndex",iconIndex)
                        .setTag("ChangeIcon",icon.save(this.registryAccess())));
            }
        }
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("AddEntry"))
            this.AddEntry();
        if(message.contains("RemoveEntry"))
            this.RemoveEntry(message.getInt("RemoveEntry"));
        if(message.contains("EditEntry"))
        {
            int entryIndex = message.getInt("EditEntry");
            if(message.contains("AddItem"))
                this.AddEntryItem(entryIndex, message.getItem("AddItem"));
            else if(message.contains("EditItem") && message.contains("ItemIndex"))
                this.EditEntryItem(entryIndex, message.getInt("ItemIndex"), message.getItem("EditItem"));
            else if(message.contains("RemoveItem"))
                this.RemoveEntryItem(entryIndex, message.getInt("RemoveItem"));
            else if(message.contains("SetOdds"))
                this.ChangeEntryOdds(entryIndex, message.getDouble("SetOdds"));
            else if(message.contains("SetHasCustomIcon"))
                this.ChangeEntryHasCustomIcons(entryIndex,message.getBoolean("SetHasCustomIcon"));
            else if(message.contains("IconIndex") && message.contains("ChangeIcon"))
                this.ChangeEntryCustomIcon(entryIndex,message.getInt("IconIndex"),IconData.loadOldData(message.getTag("ChangeIcon"),this.registryAccess()));
        }
    }


}
