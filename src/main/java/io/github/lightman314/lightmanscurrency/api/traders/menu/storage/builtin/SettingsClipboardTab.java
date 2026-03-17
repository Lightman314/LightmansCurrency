package io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.LCItemStackHandler;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.settings.data.NodeSelections;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.settings.pretty.PrettyTextData;
import io.github.lightman314.lightmanscurrency.api.settings.pretty.PrettyTextWriter;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.SettingsClipboardClientTab;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.menus.slots.SettingsCopySlot;
import io.github.lightman314.lightmanscurrency.api.misc.menus.slots.EasySlot;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class SettingsClipboardTab extends TraderStorageTab {

    public static final ResourceLocation KEY = LightmansCurrency.id("settings_clipboard");

    public SettingsClipboardTab(ITraderStorageMenu menu) { super(menu); }

    @Override
    public ResourceLocation tabKey() { return KEY; }

    private Consumer<String> copyResultConsumer = s -> {};
    private final IItemHandlerModifiable container = new LCItemStackHandler(1);
    private SettingsCopySlot slot;
    public EasySlot getSlot() { return this.slot; }

    @Override
    public Object createClientTab(Object screen) { return new SettingsClipboardClientTab(screen,this); }

    @Override
    public boolean canOpen(Player player) { return true; }

    @Override
    public void addStorageMenuSlots(Function<Slot, Slot> addSlot) {
        this.slot = new SettingsCopySlot(this.container,0, 95, 122);
        this.slot.setActive(false);
        addSlot.apply(this.slot);
    }
    @Override
    public void onTabOpen() { this.slot.setActive(true); }
    @Override
    public void onTabClose() {
        this.slot.setActive(false);
        //Kick the item out of the slot when leaving this tab
        ItemStack stackInSlot = this.container.getStackInSlot(0);
        if(!stackInSlot.isEmpty())
        {
            this.container.setStackInSlot(0,ItemStack.EMPTY);
            ItemHandlerHelper.giveItemToPlayer(this.menu.getPlayer(),stackInSlot);
        }
    }
    @Override
    public void onMenuClose() { this.menu.clearContainer(this.container); }

    public void setCopyResultConsumer(Consumer<String> consumer) { this.copyResultConsumer = Objects.requireNonNull(consumer); }

    public boolean canWriteSettingsToStack() { return this.container.getStackInSlot(0).is(LCTags.Items.SETTINGS_WRITABLE); }
    public boolean canReadSettingsFromStack() {
        ItemStack stack = this.container.getStackInSlot(0);
        return stack.has(ModDataComponents.SETTINGS_DATA) && stack.is(LCTags.Items.SETTINGS_READABLE);
    }

    public void copySettingsToStack(NodeSelections selections)
    {
        if(this.isClient())
            this.menu.SendMessage(this.builder().setTag("CopySettings",selections.write()));
        else
        {
            if(!this.canWriteSettingsToStack())
                return;
            TraderData trader = this.menu.getTrader();
            if(trader == null)
                return;
            ItemStack writableStack = this.container.getStackInSlot(0);
            ItemStack result = writableStack;
            SavedSettingData data = trader.saveSettings(this.menu.getPlayer(),selections);
            writableStack.set(ModDataComponents.SETTINGS_DATA,data);
            PrettyTextWriter writer = PrettyTextWriter.getForStack(writableStack);
            PrettyTextData lines = trader.writePrettySettings(data,DataContext.createNBT(this.menu.registryAccess()));
            result = writer.writeLinesToStack(this.menu.getPlayer(),result,lines);
            this.container.setStackInSlot(0,result);
        }
    }

    public void copySettingsDirectly(NodeSelections selections)
    {
        if(this.isClient())
            this.menu.SendMessage(this.builder().setTag("CopySettingsDirectly",selections.write()));
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
            this.menu.SendMessage(this.builder().setTag("ReadSettings",selections.write()));
        else
        {
            if(!this.canReadSettingsFromStack())
                return;
            TraderData trader = this.menu.getTrader();
            if(trader == null)
                return;
            ItemStack readableStack = this.container.getStackInSlot(0);
            SavedSettingData data = readableStack.getOrDefault(ModDataComponents.SETTINGS_DATA,SavedSettingData.EMPTY);
            trader.loadSettings(this.menu.getPlayer(),data,selections);
        }
    }

    public void loadSettingsDirectly(NodeSelections selections, String clipboard)
    {
        if(this.isClient())
        {
            this.menu.SendMessage(this.builder()
                    .setTag("ReadSettingsDirectly",selections.write())
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
            this.copySettingsToStack(NodeSelections.read(message.getTag("CopySettings")));
        if(message.contains("ReadSettings"))
            this.loadSettingsFromStack(NodeSelections.read(message.getTag("ReadSettings")));
        if(message.contains("CopySettingsDirectly"))
            this.copySettingsDirectly(NodeSelections.read(message.getTag("CopySettingsDirectly")));
        if(message.contains("ReadSettingsDirectly") && message.contains("Data"))
            this.loadSettingsDirectly(NodeSelections.read(message.getTag("ReadSettingsDirectly")),message.getString("Data"));
        if(message.contains("CopyDataResult"))
            this.copyResultConsumer.accept(message.getString("CopyDataResult"));
    }

}
