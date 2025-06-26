package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.core;

import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.settings.data.NodeSelections;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.settings.pretty.PrettyTextData;
import io.github.lightman314.lightmanscurrency.api.settings.pretty.PrettyTextWriter;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.core.SettingsClipboardClientTab;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.menus.slots.SettingsCopySlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasySlot;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class SettingsClipboardTab extends TraderStorageTab {

    public SettingsClipboardTab(@Nonnull ITraderStorageMenu menu) { super(menu); }

    private final Container container = new SimpleContainer(1);
    private SettingsCopySlot slot;
    public EasySlot getSlot() { return this.slot; }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public Object createClientTab(@Nonnull Object screen) { return new SettingsClipboardClientTab(screen,this); }

    @Override
    public boolean canOpen(Player player) { return true; }

    @Override
    public void addStorageMenuSlots(Function<Slot, Slot> addSlot) {
        this.slot = new SettingsCopySlot(this.container,0, 95, 122);
        this.slot.active = false;
        addSlot.apply(this.slot);
    }
    @Override
    public void onTabOpen() { this.slot.active = true; }
    @Override
    public void onTabClose() {
        this.slot.active = false;
        //Kick the item out of the slot when leaving this tab
        ItemStack stackInSlot = this.container.getItem(0);
        if(!stackInSlot.isEmpty())
        {
            this.container.setItem(0,ItemStack.EMPTY);
            ItemHandlerHelper.giveItemToPlayer(this.menu.getPlayer(),stackInSlot);
        }
    }
    @Override
    public void onMenuClose() { this.menu.clearContainer(this.container); }

    public boolean canWriteSettings() { return InventoryUtil.ItemHasTag(this.container.getItem(0), LCTags.Items.SETTINGS_WRITABLE); }
    public boolean canReadSettings() {
        ItemStack stack = this.container.getItem(0);
        return stack.has(ModDataComponents.SETTINGS_DATA) && InventoryUtil.ItemHasTag(stack, LCTags.Items.SETTINGS_READABLE);
    }

    public void copySettings(NodeSelections selections)
    {
        if(this.isClient())
            this.menu.SendMessage(this.builder().setCompound("CopySettings",selections.write()));
        else
        {
            if(!this.canWriteSettings())
                return;
            TraderData trader = this.menu.getTrader();
            if(trader == null)
                return;
            ItemStack writableStack = this.container.getItem(0);
            ItemStack result = writableStack;
            SavedSettingData data = trader.saveSettings(this.menu.getPlayer(),selections);
            writableStack.set(ModDataComponents.SETTINGS_DATA,data);
            PrettyTextWriter writer = PrettyTextWriter.getForStack(writableStack);
            PrettyTextData lines = trader.writePrettySettings(data);
            result = writer.writeLinesToStack(this.menu.getPlayer(),result,lines);
            this.container.setItem(0,result);
        }
    }

    public void loadSettings(NodeSelections selections)
    {
        if(this.isClient())
            this.menu.SendMessage(this.builder().setCompound("ReadSettings",selections.write()));
        else
        {
            if(!this.canReadSettings())
                return;
            TraderData trader = this.menu.getTrader();
            if(trader == null)
                return;
            ItemStack readableStack = this.container.getItem(0);
            SavedSettingData data = readableStack.getOrDefault(ModDataComponents.SETTINGS_DATA,SavedSettingData.EMPTY);
            trader.loadSettings(this.menu.getPlayer(),data,selections);
        }
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("CopySettings"))
            this.copySettings(NodeSelections.read(message.getNBT("CopySettings")));
        if(message.contains("ReadSettings"))
            this.loadSettings(NodeSelections.read(message.getNBT("ReadSettings")));
    }

}
