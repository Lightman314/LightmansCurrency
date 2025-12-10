package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
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
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SettingsClipboardTab extends TraderStorageTab {

    public SettingsClipboardTab(ITraderStorageMenu menu) { super(menu); }

    private Consumer<String> copyResultConsumer = s -> {};
    private final Container container = new SimpleContainer(1);
    private SettingsCopySlot slot;
    public EasySlot getSlot() { return this.slot; }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Object createClientTab(Object screen) { return new SettingsClipboardClientTab(screen,this); }

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

    public void setCopyResultConsumer(Consumer<String> consumer) { this.copyResultConsumer = Objects.requireNonNull(consumer); }

    public boolean canWriteSettingsToStack() { return InventoryUtil.ItemHasTag(this.container.getItem(0), LCTags.Items.SETTINGS_WRITABLE); }
    public boolean canReadSettingsFromStack() {
        ItemStack stack = this.container.getItem(0);
        return stack.has(ModDataComponents.SETTINGS_DATA) && InventoryUtil.ItemHasTag(stack, LCTags.Items.SETTINGS_READABLE);
    }

    public void copySettingsToStack(NodeSelections selections)
    {
        if(this.isClient())
            this.menu.SendMessage(this.builder().setCompound("CopySettings",selections.write()));
        else
        {
            if(!this.canWriteSettingsToStack())
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

    public void copySettingsDirectly(NodeSelections selections)
    {
        if(this.isClient())
            this.menu.SendMessage(this.builder().setCompound("CopySettingsDirectly",selections.write()));
        else
        {
            TraderData trader = this.menu.getTrader();
            if(trader == null)
                return;
            SavedSettingData data = trader.saveSettings(this.menu.getPlayer(),selections);
            this.menu.SendMessage(this.builder().setString("CopyDataResult",FileUtil.GSON.toJson(SavedSettingData.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE,this.registryAccess()),data).getOrThrow())));
        }
    }

    public void loadSettingsFromStack(NodeSelections selections)
    {
        if(this.isClient())
            this.menu.SendMessage(this.builder().setCompound("ReadSettings",selections.write()));
        else
        {
            if(!this.canReadSettingsFromStack())
                return;
            TraderData trader = this.menu.getTrader();
            if(trader == null)
                return;
            ItemStack readableStack = this.container.getItem(0);
            SavedSettingData data = readableStack.getOrDefault(ModDataComponents.SETTINGS_DATA,SavedSettingData.EMPTY);
            trader.loadSettings(this.menu.getPlayer(),data,selections);
        }
    }

    public void loadSettingsDirectly(NodeSelections selections, String clipboard)
    {
        if(this.isClient())
        {
            this.menu.SendMessage(this.builder()
                    .setCompound("ReadSettingsDirectly",selections.write())
                    .setString("Data",clipboard));
        }
        else
        {
            TraderData trader = this.menu.getTrader();
            if(trader == null)
                return;
            try {
                JsonElement json = GsonHelper.parse(clipboard,true);
                SavedSettingData data = SavedSettingData.CODEC.decode(RegistryOps.create(JsonOps.INSTANCE,this.registryAccess()),json).getOrThrow().getFirst();
                trader.loadSettings(this.menu.getPlayer(),data,selections);
            } catch (JsonParseException | IllegalStateException ignored) {}
        }
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("CopySettings"))
            this.copySettingsToStack(NodeSelections.read(message.getNBT("CopySettings")));
        if(message.contains("ReadSettings"))
            this.loadSettingsFromStack(NodeSelections.read(message.getNBT("ReadSettings")));
        if(message.contains("CopySettingsDirectly"))
            this.copySettingsDirectly(NodeSelections.read(message.getNBT("CopySettingsDirectly")));
        if(message.contains("ReadSettingsDirectly") && message.contains("Data"))
            this.loadSettingsDirectly(NodeSelections.read(message.getNBT("ReadSettingsDirectly")),message.getString("Data"));
        if(message.contains("CopyDataResult"))
            this.copyResultConsumer.accept(message.getString("CopyDataResult"));
    }

}
