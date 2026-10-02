package io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCTags;
import io.github.lightman314.lightmanscurrency.api.helpers.JsonHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.data.io.JsonValueInput;
import io.github.lightman314.lightmanscurrency.api.helpers.data.io.JsonValueOutput;
import io.github.lightman314.lightmanscurrency.api.helpers.keys.DualKey;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.resource.NormalItemStorage;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IDisplayNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ISettingsStorageIONode;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.PermissionValue;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.*;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.world.menu.slots.EasyResourceSlot;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SettingsClipboardTab extends TraderStorageTab {

    public static final Identifier KEY = LCApi.id("settings_clipboard");

    public SettingsClipboardTab(TraderStorageMenu menu) { super(menu); }

    public static final TextEntry TOOLTIP = TextEntry.tooltip(LCApi.MODID,"trader.settings_clipboard");
    public static final TextEntry BUTTON_SETTINGS_COPY = TextEntry.button(LCApi.MODID,"trader_settings_clipboard.copy");
    public static final TextEntry BUTTON_SETTINGS_PASTE = TextEntry.button(LCApi.MODID,"trader_settings_clipboard.paste");

    private final ClipboardStorage storage = new ClipboardStorage();
    private final EasyResourceSlot slot = new EasyResourceSlot(this.storage,0,95,122);
    public final EasyResourceSlot getSlot() { return this.slot; }

    @Override
    public Identifier getKey() { return KEY; }

    @Override
    public void addMenuSlots(Consumer<Slot> builder) {
        builder.accept(this.slot);
        this.slot.setActive(false);
    }

    @Override
    public void onTabOpened(FancyPacketMap additional) { this.slot.setActive(true); }
    @Override
    public void onTabClosed() { this.slot.setActive(false); this.getMenu().clearContainer(this.storage); }
    @Override
    public void onMenuClosed() { this.getMenu().clearContainer(this.storage); }

    @Override
    public boolean canOpen() { return this.getPermission(LCPermissions.EDIT_SETTINGS); }

    @Override
    public int getTabSortPriority() { return 50; }

    public boolean canWriteToItemInSlot() {
        ItemStack item = this.storage.getStack(0);
        return !item.isEmpty() && item.is(LCTags.Items.SETTINGS_WRITABLE);
    }
    public boolean canReadFromItemInSlot() {
        ItemStack item = this.storage.getStack(0);
        return !item.isEmpty() && item.is(LCTags.Items.SETTINGS_READABLE) && item.has(LCDataComponents.TRADER_SETTINGS);
    }

    public List<ISettingsStorageIO> getSettings() { return ISettingsStorageIONode.getSettingsIO(this,this.getPlayer()); }

    public void loadDataFromItem(SettingsSelection selection) {
        if(this.isClient()) {
            this.send(selection.write("loadFromSlot"));
            return;
        }
        ItemStack item = this.storage.getStack(0);
        if(item.isEmpty() || !item.is(LCTags.Items.SETTINGS_READABLE) && !item.has(LCDataComponents.TRADER_SETTINGS))
            return;
        CopiedTraderSettings data = item.get(LCDataComponents.TRADER_SETTINGS);
        if(data == null)
            return;
        ValueInput input = data.getData(ProblemReporter.DISCARDING,this.registryAccess());
        this.loadSettingData(input,selection);
    }

    public void loadDataFromClipboard(String clipboard,SettingsSelection selection) {
        if(this.isClient()) {
            this.send(selection.write("selection").setString("loadFromClipboard",clipboard));
            return;
        }
        try {
            JsonObject json = GsonHelper.parse(clipboard);
            ValueInput input = JsonValueInput.create(ProblemReporter.DISCARDING,this.registryAccess(),json);
            this.loadSettingData(input,selection);
        } catch (JsonParseException ignored) { }
    }

    public void saveDataToItem(SettingsSelection selection) {
        if(this.isClient()) {
            this.send(selection.write("saveToSlot"));
            return;
        }
        ItemStack item = this.storage.getStack(0);
        if(item.isEmpty() || !item.is(LCTags.Items.SETTINGS_WRITABLE))
            return;
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,this.registryAccess());
        this.collectCurrentData(output,selection);
        if(!output.isEmpty()) {
            //Store the copied trader settings
            item.set(LCDataComponents.TRADER_SETTINGS,new CopiedTraderSettings(output));
            //Apply any item transformations afterward (i.e. book -> written book)
            ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING,this.registryAccess(),output.buildResult());
            item = SettingsItemTransformer.transformItemAfterWrite(item,input,IDisplayNode.getTraderName(this),this.getPlayer(),selection.onlySelected(this.getSettings()));
            //Put the modified item back into the slot
            this.storage.set(0,item);
        }
    }

    public void saveDataToClipboard(SettingsSelection selection) {
        if(this.isClient()) {
            this.send(selection.write("requestDataForClipboard"));
            return;
        }
        JsonValueOutput output = JsonValueOutput.createWithContext(ProblemReporter.DISCARDING,this.registryAccess());
        this.collectCurrentData(output,selection);
        String text = JsonHelper.PRETTY_GSON.toJson(output.buildResult());
        this.sendToClient(FancyPacketMap.map().setString("settingsClipboardContents",text));
    }

    private void collectCurrentData(ValueOutput output,SettingsSelection selection) {
        for(ISettingsStorageIO s : selection.onlySelected(this.getSettings()))
            s.encodeSettings(output.child(s.getSettingsKey().toString()));
    }

    private void loadSettingData(ValueInput input,SettingsSelection selection) {
        LoadContext context = new LoadContext(this.getPlayer(), this, selection);
        while(context.hasNext()) {
            ISettingsStorageIO s = context.getNext();
            input.child(s.getSettingsKey().toString()).ifPresent(entry -> s.decodeSettings(entry,context));
        }
    }

    @Override
    public void handleMessage(FancyPacketMap message) {
        if(message.contains("loadFromSlot"))
            this.loadDataFromItem(SettingsSelection.read("loadFromSlot",message));
        if(message.contains("loadFromClipboard"))
            this.loadDataFromClipboard(message.getString("loadFromClipboard"),SettingsSelection.read("selection",message));
        if(message.contains("saveToSlot"))
            this.saveDataToItem(SettingsSelection.read("saveToSlot",message));
        if(message.contains("requestDataForClipboard"))
            this.saveDataToClipboard(SettingsSelection.read("requestDataForClipboard",message));
    }

    private static class ClipboardStorage extends NormalItemStorage {
        public ClipboardStorage() { super(1); }
        @Override
        protected boolean isValid(int index,ItemStack stack) { return stack.is(LCTags.Items.SETTINGS_WRITABLE) || (stack.is(LCTags.Items.SETTINGS_READABLE) && stack.has(LCDataComponents.TRADER_SETTINGS)); }
    }

    private static class LoadContext implements SettingsLoadContext.Mutable {

        private final Player player;
        private final PlayerReference playerReference;
        private final SettingsClipboardTab tab;
        private final SettingsSelection selection;
        private Owner oldOwner;
        private List<PlayerReference> oldPermissionPlayers = List.of();
        private Map<Permission<?>,PermissionValue<?>> oldPermissionValueMap = Map.of();
        private LoadContext(Player player,SettingsClipboardTab tab,SettingsSelection selection) {
            this.player = player;
            this.playerReference = PlayerReference.of(player);
            this.tab = tab;
            this.oldOwner = Owner.getNull(tab);
            this.selection = selection;
        }

        private int nextIndex = 0;
        private final List<DualKey> keyHistory = new ArrayList<>();
        private List<ISettingsStorageIO> settingsCache = null;

        public ISettingsStorageIO getNext() { return this.getNext(true); }
        @Nullable
        private ISettingsStorageIO getNext(boolean update) {
            if(this.settingsCache == null)
                this.settingsCache = this.selection.onlySelected(this.tab.getSettings());
            if(this.nextIndex >= this.settingsCache.size())
                return null;
            ISettingsStorageIO result = this.settingsCache.get(this.nextIndex);
            if(update) {
                this.nextIndex += 1;
                this.keyHistory.addFirst(result.getSettingsKey());
            }
            return result;
        }
        public boolean hasNext() { return this.getNext(false) != null; }

        @Override
        public void definePreviousOwner(Owner owner) { this.oldOwner = owner; }
        @Override
        public void definePreviousPermissionMapMembers(List<PlayerReference> players) { this.oldPermissionPlayers = List.copyOf(players); }
        @Override
        public void definePreviousPermissionMap(Map<Permission<?>, PermissionValue<?>> permissionMap) { this.oldPermissionValueMap = Map.copyOf(permissionMap); }

        public void reloadPotentialSettings() {
            this.settingsCache = this.selection.onlySelected(this.tab.getSettings());
            //Don't bother with the lookup if we haven't even started
            if(this.nextIndex == 0)
                return;
            //Check if the current index has not changed
            //If so, we don't need to update anything
            int previousIndex = this.nextIndex--;
            if(previousIndex < this.settingsCache.size() && this.settingsCache.get(previousIndex).getSettingsKey().equals(this.keyHistory.getFirst()))
                return;
            //Otherwise find the index of the setting with our last key
            for(DualKey key : this.keyHistory) {
                for(int i = 0; i < this.settingsCache.size(); ++i) {
                    ISettingsStorageIO setting = this.settingsCache.get(i);
                    if(setting.getSettingsKey().equals(key)) {
                        this.nextIndex = i + 1;
                        return;
                    }
                }
            }
            //If we never find any of the settings within our key history, reset back to index 0
            this.nextIndex = 0;
        }

        @Override
        public boolean isAdminPlayer() { return LCApi.isInAdminMode(this.player); }

        @Override
        public <T> T getPermission(Permission<T> permission) {
            //Get the current value
            T currentValue = this.tab.getPermission(permission);
            //Obtain the old value and compare it to the current value if relevant.
            //If the player was a member or ally, get highest of the map permission and the current permission
            if(this.oldOwner.isMember(this.playerReference) || PlayerReference.isInList(this.oldPermissionPlayers,this.player))
                return permission.getHighest(this.getMapPermission(permission),currentValue);
            //If the player was an admin, then they have the highest permission level
            else if(this.oldOwner.isAdmin(this.playerReference))
                return permission.getMaxValue();
            else //If they were neither, then we don't need to compare the current value to the lowest/empty value
                return currentValue;
        }

        private <T> T getMapPermission(Permission<T> permission) {
            if(this.oldPermissionValueMap.containsKey(permission)) {
                try {
                    return (T)this.oldPermissionValueMap.get(permission).get();
                } catch (ClassCastException ignored) {}
            }
            return permission.getEmpty();
        }

    }

}
